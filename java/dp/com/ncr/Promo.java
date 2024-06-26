package com.ncr;

import clsFrequentShopper.*;
import com.ncr.ecommerce.ECommerceManager;
import com.ncr.gui.SelDlg;
import com.ncr.loyalty.LoyaltyService;
import com.ncr.loyalty.LoyaltyServiceInterface;
import com.ncr.loyalty.aym.AymLoyaltyService;
import com.ncr.loyalty.transaction.LoyaltyVariable;
import com.ncr.ssco.communication.manager.SscoPosManager;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

abstract class Promo extends Basis implements CONSTANT {
    static boolean active = false;
    static private int callType; /* 0 = book, 1 = read item, 2 = read tran */
    static private clsFrequentShopper objFrequentShopper;

    public static final int PROMO_STD = 1;
    public static final int PROMO_FS = 2;
    public static final int PROMO_CMC = 4;

    public static final int BEFORE_TOTAL_AREA = 3;
    public static final int AFTER_TOTAL_AREA = 4;
    public static final int BEFORE_TRAILER_AREA = 5;
    public static final int AFTER_TRAILER_AREA = 6;

    private static final Logger logger = Logger.getLogger(Promo.class);
    private static List<clsFsRewardData> detailsList = new ArrayList<clsFsRewardData>();
    private static List<ReceiptMessage> receiptMessages = new ArrayList<ReceiptMessage>();
    private static boolean noPrintPoints = false;   //NOPRINTPOINTS-CGA#A


    static boolean isEnabled(int mask) {
        mask &= Integer.parseInt(fso_line.substring(0, 2), 16);
        return mask > 0;
    }

    static boolean isCdgFillEnabled() {
        return fso_line.substring(2, 4).equals("01");
    }

    static boolean isActive() {
        return active;
    }

    static void initialize() {
        if (!isEnabled(PROMO_FS)) return;

        objFrequentShopper = new clsFrequentShopper();
        if (objFrequentShopper.initialize(ctl.reg_nbr, 0)) {
            if ((options[O_Scale] & 1) > 0)
                objFrequentShopper.setWeightsDecimalPlaces(2);
            objFrequentShopper.setAmountsDecimalPlaces(tnd[0].dec);
            objFrequentShopper.setReceiptLineLength(40);
            objFrequentShopper.setReceiptShortLineLength(36);
        } else {
            error("on initialization");
            objFrequentShopper = null;
        }
    }

    static void checkForNewFile() // monitor within ready state
    {
        if (objFrequentShopper == null) return;

        if (objFrequentShopper.checkForPromotionUpdates()) {
            logConsole(2, "Promotions Applied", null);
        }
    }

    static void terminate() {
        if (objFrequentShopper == null) return;

        if (!objFrequentShopper.terminate()) {
            error("on termination");
        }
        objFrequentShopper = null;
    }

    static void error(String details) {
        if (details != null) {
            logConsole(0, Mnemo.getInfo(45), details);
        }
        gui.clearLink(Mnemo.getInfo(45), 0x81);
    }

    static void startTransaction() {
        if (objFrequentShopper == null) return;

        active = true;
        detailsList.clear();
        receiptMessages.clear();
        String datim = editNum(ctl.date, 6) + " " + editNum(ctl.time, 6);
        int type = FS_TRANMODE_SALES;
        if (ctl.mode == M_SCHOOL) type = FS_TRANMODE_TRAINING;
        if (ctl.mode == M_RENTRY) type = FS_TRANMODE_REENTRY;
        logConsole(4, null, "startTransaction=" + editNum(ctl.tran, 4) + " " + datim);
        if (objFrequentShopper.startTransaction(ctl.tran, datim, type)) {
            objFrequentShopper.SetInitialPromotionVariableValue(501, ctl.reg_nbr);
            sellItemMap.clear();
            processSimpleRewards();
        } else error("on startTransaction");
    }

    static void identifyCustomer(boolean evaluate) {
        if (!active) return;

        clsFsCustomerData objFsCustomerData = new clsFsCustomerData();
        setCustomerData(objFsCustomerData);
        for (int ind = 0; ind < 10; ind++) {
            if (rCLS.find("CV" + ind, cus.getNumber()) < 1) continue;
            if (rCLS.block.items > 0) {
                objFrequentShopper.SetInitialPromotionVariableValue(rCLS.block.items, rCLS.block.total);
                logConsole(4, null, "PromoVar" + ind + " id="
                        + editNum(rCLS.block.items, 5) + " value=" + rCLS.block.total);
            }
        }
        if (objFrequentShopper.identifyCustomer(objFsCustomerData)) {
            if (evaluate) processSimpleRewards();
        } else error("on identifyCustomer");
        objFsCustomerData.delete();
    }

    static void sellItem() {
        if (!isActive()) return;

        clsFsItemData objFsItemData = new clsFsItemData();
        setItemData(objFsItemData);
        if (objFrequentShopper.sellItem(objFsItemData)) {
            processSimpleRewards();
        } else error("on sellItem");
        objFsItemData.delete();
    }

    static void updateItems(long value) // manual discounts/credits on items
    {
        if (!isActive()) return;

        long dscnt = itm.sit > 0 ? value : 0;
        if (clsFrequentShopper.updateTotals(0, dscnt, value)) {
            processSimpleRewards();
        } else error("on updateItems");
    }

    static void updateTotals(long value) // manual discounts/credits on total
    {
        if (!isActive()) return;

        if (clsFrequentShopper.updateTotals(0, value, value)) {
            processSimpleRewards();
        } else error("on updateTotals");
    }

    static void setTransactionAmount(long total) // transition from itemization to tenderization
    {
        if (!isActive()) return;

        long value = objFrequentShopper.getTransactionTotal();
        if (value != total) {
            logConsole(0, "getTransactionTotal=" + value, null);
            error("totals mismatch");
            updateTotals(total - value);
        }
    }

    static void setPromovar(long promovar, long value) {
        setPromovar(promovar, value, true);
    }

    static void setPromovar(long promovar, long value, boolean evaluate) {
        logger.debug("ENTER setPromotion " + promovar + " ,value " + value);

        if (!isActive()) {
            logger.debug("EXIT setPromotion - no active");
            return;
        }

        boolean success = objFrequentShopper.SetInitialPromotionVariableValue(promovar, value);
        logger.info("success: " + success);

        if (evaluate) processSimpleRewards();
        logger.debug("EXIT setPromotion");
    }

    static long getPromovar(long promo) {
        return clsFrequentShopper.getPromotionVariableValue(promo, true, 5);
    }

    static void bookComplexRewards() {
        if (!isActive()) return;
        processComplexRewards(0);
    }

    static void payTender() {
        if (!isActive()) return;

        clsFsTenderData objFsTenderData = new clsFsTenderData();
        setTenderData(objFsTenderData);
        if (objFrequentShopper.payTender(objFsTenderData)) {
            processSimpleRewards();
        } else error("on payTender");
        objFsTenderData.delete();
    }

    static void endTransaction() {
        if (!active) return;

        processEotRewards();
        if (!objFrequentShopper.endTransaction())
            error("on endTransaction");
        active = false;
    }

    static void cancelTransaction() {
        if (!active) return;

        if (!objFrequentShopper.cancelTransaction())
            error("on cancelTransaction");
        active = false;
    }

    static void readItemDiscounts() {
        if (!isActive()) return;
        processComplexRewards(1);
    }

    static void readTranDiscounts() {
        if (!isActive()) return;

        processComplexRewards(2);
    }

    static String[] getItemRewardLines(boolean first, int item, boolean simple) {
        if (!isActive()) return null;

        String text[] = null;
        clsFsRewardData objFsRewardData = new clsFsRewardData();
        boolean rc = first ? objFrequentShopper.getFirstReward(objFsRewardData, ++item)
                : objFrequentShopper.getNextReward(objFsRewardData, ++item);
        for (; rc; rc = objFrequentShopper.getNextReward(objFsRewardData, item)) {
            if (objFsRewardData.getSimpleFlag() ^ simple) continue;
            if (objFsRewardData.isMoneyReward()) continue;
            if (objFsRewardData.isPointReward()) continue;
            if (objFsRewardData.isPromoPointReward()) continue;
            int ind = objFsRewardData.getReceiptLinesCount();
            if (ind > 0) {
                text = new String[ind];
                while (ind-- > 0) text[ind] = objFsRewardData.getReceiptLine(ind);
                break;
            }
        }
        objFsRewardData.delete();
        return text;
    }

    private static void setCustomerData(clsFsCustomerData x) {
        int ind, pnts = 0;
        String text = cus.getName();

        if ((ind = text.indexOf(' ')) >= 0) {
            x.setTitle(text.substring(0, ind));
            text = text.substring(ind).trim();
        }
        if ((ind = text.lastIndexOf(' ')) >= 0) {
            x.setFirstName(text.substring(0, ind).trim());
            text = text.substring(ind + 1);
        }
        x.setLastName(text);

        text = cus.getCity();
        if ((ind = text.indexOf(' ')) >= 0) {
            x.setMainAddressZip(text.substring(0, ind));
            text = text.substring(ind).trim();
        }
        x.setMainAddressTown(text);
        x.setMainAddressStreet(cus.getAdrs().trim());
        x.setCustomerId(cus.getNumber());
        x.setEmployeeFlag(tra.spf2 == M_EMPDSC);
        x.setMembershipLevel(cus.getBranch() / 10);
        x.setCardStatus(0);
        x.setCategory(cus.getBranch());
        x.setCompanyName(cus.getNam2().trim());
        // Initialize the customer activity values
        if (rCLS.find("C00", cus.getNumber()) > 0) {
            text = editNum(rCLS.date, 6) + " " + editNum(rCLS.time, 6);
            x.setLastActivityDate(text);
            x.setTransactionCount(rCLS.block.trans);
        }
        clsFsAccumulationData objFsAccumulationData = new clsFsAccumulationData();
        if (rCLS.find("CP0", cus.getNumber()) > 0) {
            objFsAccumulationData.setRewardCount(rCLS.block.items);
            objFsAccumulationData.setRewardAmount(rCLS.block.total);
        }
        for (ind = 5; ind < 9; ind++) {
            if (rCLS.find("CP" + ind, cus.getNumber()) > 0) pnts += rCLS.block.items;
        }
        objFsAccumulationData.setPromoPointsBalance(pnts);
        objFsAccumulationData.setPointsBalance(tra.pnt);
        x.setAccumulationData(objFsAccumulationData);
        objFsAccumulationData.delete();
        logConsole(4, null, "CustomerId=" + x.getCustomerId()
                + " EmployeeFlag=" + x.getEmployeeFlag());
        logConsole(4, null, "MembershipLevel=" + x.getMembershipLevel()
                + " Category=" + x.getCategory()
                + " CardStatus=" + x.getCardStatus());
        logConsole(4, null, "CompanyName=" + x.getCompanyName());
        logConsole(4, null, "Title=" + x.getTitle()
                + " FirstName=" + x.getFirstName()
                + " LastName=" + x.getLastName());
        logConsole(4, null, "MainAddressStreet=" + x.getMainAddressStreet());
        logConsole(4, null, "MainAddressZip=" + x.getMainAddressZip()
                + " MainAddressTown=" + x.getMainAddressTown());
        logConsole(4, null, "TransactionCount=" + x.getTransactionCount()
                + " LastActivityDate=" + x.getLastActivityDate());
    }

    private static void setItemData(clsFsItemData x) {
        String sDpt = editKey(itm.dpt_nbr, 4);

        logger.info("entryId: " + itm.index + 1);
        x.setEntryId(itm.index + 1);
        logger.info("department: " + Short.parseShort(sDpt.replace('*', '0')));
        x.setDepartment(Short.parseShort(sDpt.replace('*', '0')));
        logger.info("item code: " + itm.number);
        if (itm.number.startsWith(" ")) {
            String number = itm.number.trim();
            if (isCdgFillEnabled() && number.length() >= 7 && number.length() <= 12) {
                number = leftFill(number, 12, '0');
                number = cdgSetup(number + "0", ean_weights, 10);
            }
            x.setItemCode(number);
        }
        logger.info("mxm code: " + (short) itm.mmt);
        x.setMixMatchCode((short) itm.mmt);
        logger.info("family code: " + (short) itm.sit);
        x.SetFamilyCode((short) itm.sit);
        logger.info("extended price: " + itm.qty * set_price(itm, itm.prpos));
        x.setExtendedPrice(itm.qty * set_price(itm, itm.prpos));

        if (itm.prm > 0) {
            x.setWeightSold(itm.dec);
            x.setPricePerUnitWeight(itm.prpos);
        } else x.setQuantitySold(itm.qty);

        boolean flag = itm.sit > 0;

        if (GdSarawat.isEnableDsc()) {
            if (itm.prchange || (itm.spf1 & M_RETURN) > 0 || (itm.spf1 & M_TRRTRN) > 0
                    || ECommerceManager.getInstance().noDscOnBasket()) {   //NODSC-CGA#A
                logger.info("no discount");

                x.setItemCode("9999999999999");
                x.setDepartment(Short.MAX_VALUE);
            }
        }
        if (itm.coupon) {
            x.setItemCode(itm.eanupc.trim().substring(0, 6));
        }
        x.SetUserTotalFlag((itm.flag & F_XPROMO) == 0);

        flag = false;
        for (int ind = M_EXPNSE; ind > M_TRRTRN; ind >>= 1) flag ^= (itm.spf1 & ind) > 0;
        logger.info("return flag: " + flag);
        x.setReturnFlag(flag);
        flag = false;
        for (int ind = M_VOID; ind > 0; ind >>= 1) flag ^= (itm.spf1 & ind) > 0;
        logger.info("void flag: " + flag);
        x.setVoidFlag(flag);

        addSellItemAttribute(itm.index +1 , itm.copy());
    }

    private static void setTenderData(clsFsTenderData x) {
        x.setEntryId(itm.index + 1);
        x.setTenderID((char) itm.tnd);
        x.setTenderAmount(itm.amt);
        x.setVoidFlag(itm.mark != ' ');
    }

    public static void processSimpleRewards() {
        clsFsRewardData objFsRewardData = new clsFsRewardData();

        callType = 0;
        while (objFrequentShopper.getReward(objFsRewardData)) {
            if (objFsRewardData.getSimpleFlag()) {
                processReward(objFsRewardData);
            }
        }
        objFsRewardData.delete();
    }

    static void fillDetailList(int type) {
        clsFsRewardData objFsRewardData = new clsFsRewardData();
        detailsList.clear();

        if (objFrequentShopper.getFirstReward(objFsRewardData, FS_DUMMY_ENTRY_ID)) do {
            if (objFsRewardData.getRewardType() == FS_REWARDTYPE_POSIDC_REWDETAIL) {
                detailsList.add(objFsRewardData);
            }
            objFsRewardData = new clsFsRewardData();
        } while (objFrequentShopper.getNextReward(objFsRewardData, FS_DUMMY_ENTRY_ID));

    }

    static void processComplexRewards(int type) {
        clsFsRewardData objFsRewardData = new clsFsRewardData();

        fillDetailList(type);
        callType = type;
        if (objFrequentShopper.getFirstReward(objFsRewardData, FS_DUMMY_ENTRY_ID)) do {
            if (objFsRewardData.getSimpleFlag()) continue;
            if (objFsRewardData.getEntryId() > 0 ^ type == 1)
                if (type > 0) continue;
            processReward(objFsRewardData);
        } while (objFrequentShopper.getNextReward(objFsRewardData, FS_DUMMY_ENTRY_ID));
        objFsRewardData.delete();
    }

    static void processEotRewards() {
        clsFsRewardData objFsRewardData = new clsFsRewardData();

        callType = 0;
        while (objFrequentShopper.getEotReward(objFsRewardData)) {
            if (objFsRewardData.getSimpleFlag())
                processReward(objFsRewardData);
        }
        objFsRewardData.delete();
        clsFsTransactionData x = new clsFsTransactionData();
        objFrequentShopper.getTransactionData(x);
        logConsole(4, null, "PointsBalance=" + x.getPointsBalance()
                + " Earned=" + x.getPointsEarned()
                + " Redeemed=" + x.getPointsRedeemed());
        logConsole(4, null, "PromoPointsBalance=" + x.getPromoPointsBalance()
                + " Earned=" + x.getPromoPointsEarned()
                + " Redeemed=" + x.getPromoPointsRedeemed());
        logConsole(4, null, "RewardAmount=" + x.getRewardAmount()
                + " Count=" + x.getRewardCount());
        x.delete();
    }

    static void processReward(clsFsRewardData x) {
        if (callType == 0) {
            logConsole(4, "RewardId=" + x.getRewardId()
                            + " RewardSimpleFlag=" + x.getSimpleFlag()
                    , "RewardType=" + x.getRewardType()
                            + " PromotionCode=" + x.getPromotionCode());
            logConsole(4, null, "EntryId=" + x.getEntryId()
                    + " RewardDepartment=" + x.getRewardDepartment());
            logConsole(4, null, "ReplacementFlag=" + x.getReplacementFlag()
                    + " ReversalFlag=" + x.getReversalFlag());
            logConsole(4, null, "ExtendedValue=" + x.getExtendedValue()
                    + " OldExtendedValue=" + x.getOldExtendedValue());
            logConsole(4, null, "RewardedQuantity=" + x.GetRewardedQuantity()
                    + " RewardedAmount=" + x.GetRewardedAmount());
            logConsole(4, null, "CashierLines=" + x.getCashierLinesCount()
                    + " ReceiptLines=" + x.getReceiptLinesCount()
                    + " CustDispLines=" + x.getCustDispLinesCount());
        }
        Itemdata sav = itm;
        itm = new Itemdata();
        if (x.isMoneyReward()) frequentShopperAmount(x);
        else if (x.isPointReward()) frequentShopperPoints(x);
        else if (x.isPromoPointReward()) frequentShopperPoints(x);
        else switch (x.getRewardType()) {
                case FS_REWARDTYPE_CASHIER_DISPLAY_MESSAGE:
                    frequentShopperCashierDisplayMessage(x);
                    break;
                //CUSTOMER-DISPLAY-CGA#A BEG
                case FS_REWARDTYPE_CUSTDISP_MESSAGE:
                    frequentShopperCustomerDisplayMessage(x);
                    break;
                //CUSTOMER-DISPLAY-CGA#A END
                case FS_REWARDTYPE_CUSTOMER_RECEIPT_MESSAGE:
                case FS_REWARDTYPE_CUSTOMER_RECEIPT_MESSAGE_AFTER_TRAILER:
                    if (x.GetUserDefinedData().trim().equals("DSCSUM") && getPromovar(Long.valueOf(5000)) == 0 && getPromovar(Long.valueOf(5100)) == 0) {
                        break;
                    }

                    frequentShopperCustomerReceiptMessage(x);
                    break;
                case FS_REWARDTYPE_PROMO_VARIABLE:
                case FS_REWARDTYPE_PROMO_VARIABLE_PER_DOLLAR:
                case FS_REWARDTYPE_PROMO_VARIABLE_PER_WT:
                case FS_REWARDTYPE_PROMO_VARIABLE_MULTIPLIER:
                case FS_REWARDTYPE_PROMO_VARIABLE_PER_TRX_DOLLAR:
                case FS_REWARDTYPE_PROMO_VARIABLE_PER_MINORD_DOLLAR:
                    frequentShopperSetPromotionVariable(x);
                    break;
                case FS_REWARDTYPE_CHOICE_OKCANCEL:
                    frequentShopperChoiceOkCancel(x);
                    break;
                case FS_REWARDTYPE_CHOICE_MULTIPLE:
                    frequentShopperChoiceMultiple(x);
                    break;
                case FS_REWARDTYPE_POSIDC_REWDETAIL:
                    //frequentShopperRewardDetail(x);
                    break;
            }
        itm = sav;
        if (callType > 0) return;
        for (int ind = 0; ind < x.getCustDispLinesCount(); ind++) {
            String msg = x.getCustDispLine(ind);
            cusLine.init(msg).show(10 + ind);
        }
    }

    private static boolean prepAccumulation(clsFsRewardData objFsRewardData) {
        int nbr = (int) objFsRewardData.getEntryId();

        if (tra.vItems.isEmpty()) {
            error("Reward early");
            return false;
        }
        if (tra.tnd > 0) {
            error("Reward late");
            return false;
        }
        if (nbr == 0) {
            if (objFsRewardData.getSimpleFlag()) {
                GdPos.panel.display(2, Mnemo.getInfo(8));
                error("To transaction level");
                return false;
            }
            if ((nbr = objFsRewardData.getRewardDepartment()) > 0) {
                dlu = itm;
                int sts = GdPrice.src_dpt(nbr = keyValue(editNum(nbr, 4)));
                if (sts > 0) {
                    GdPos.panel.display(2, Mnemo.getInfo(sts));
                    error(Mnemo.getText(6) + editKey(nbr, 4) + "????");
                    return false;
                }
                itm = dlu;
            }
        } else itm = tra.vItems.getElement(nbr - 1).copy();
        nbr = (int) objFsRewardData.getPromotionCode();
        itm.cmp_nbr = keyValue(editNum(nbr, 4));
        itm.flag = objFsRewardData.getSimpleFlag() ? 0 : 1;
        itm.rew_qty = (int) objFsRewardData.GetRewardedQuantity();
        itm.promo = editNum(objFsRewardData.getRewardType(), 4)
                + leftFill(String.valueOf(objFsRewardData.getPromotionCode()), 12, ' ');
        if (itm.rew_qty == 0) {
            itm.rew_qty = (int) objFsRewardData.GetRewardedWeight();
            if (itm.rew_qty != 0) itm.flag |= 4;
        }
        itm.rew_amt = objFsRewardData.GetRewardedAmount();
        if (objFsRewardData.getReversalFlag()) {
            itm.flag |= 2;
            itm.rew_qty = -itm.rew_qty;
            itm.rew_amt = -itm.rew_amt;
        }
        return true;
    }

    private static void frequentShopperAmount(clsFsRewardData objFsRewardData) {
        if (!prepAccumulation(objFsRewardData)) return;
        long previous = objFsRewardData.getOldExtendedValue();
        itm.amt = objFsRewardData.getExtendedValue() - previous;
        itm.text = objFsRewardData.getReceiptAmountLineDescr().trim();

        if (callType > 0) {
            if (callType == 1) /* item related */ {
                GdTrans.itm_trans(true);
                if (SscoPosManager.getInstance().isUsed()) {
                    SscoPosManager.getInstance().addCurrentItemPromotion(itm.copy());
                }
            } else /* transaction level */ {
                itm.spf3 = sc_value(M_TOTRBT);
                itm.crd = itm.amt;
                if (SscoPosManager.getInstance().isUsed()) {
                    SscoPosManager.getInstance().addTransactionalReward(itm.copy());
                }
                GdTrans.rbt_distrib();
            }
        } else /* simple */ {
            if (!objFsRewardData.getSimpleFlag()) return;

            tra.vItems.getElement(itm.index).crd += itm.amt;
            tra.vItems.getElement(itm.index).crdDescription = itm.text;
            itm.spf2 = M_REBATE;

            if (SscoPosManager.getInstance().isUsed()) {
                SscoPosManager.getInstance().addCurrentItemPromotion(itm.copy());
            }
            GdSales.crd_line();
        }
        String udd = objFsRewardData.GetUserDefinedData().trim();

        logger.info("userDefinedData: " + udd);

        if (udd.equals("MEAL")) {
            //tra.tld_amt += objFsRewardData.getExtendedValue();
            itm.meal = 1;
        }
        if (udd.length() > 0) {
            GdSpinneys.getInstance().checkCoupon(udd, objFsRewardData.getReversalFlag());
        }
        //SPINNEYS-20180215-CGA#A END
    }

    private static void frequentShopperPoints(clsFsRewardData objFsRewardData) {
        if (callType > 0) return;
        if (!prepAccumulation(objFsRewardData)) return;

        long previous = objFsRewardData.getOldExtendedValue();
        itm.pnt = (int) (objFsRewardData.getExtendedValue() - previous);
        if (objFsRewardData.getReceiptLinesCount() > 0) {
            String msg = objFsRewardData.getReceiptLine(0);
            itm.text = msg.substring(0, msg.lastIndexOf(' ') + 1).trim();
        }
        int sc = objFsRewardData.isPromoPointReward() ? 5 : 1;
        if (objFsRewardData.getEntryId() < 1) {
            if (itm.dpt_nbr == 0) itm.number = "ST              ";
            else itm.number = "SD          " + editKey(itm.dpt_nbr, 4);
            itm.amt = itm.rew_amt;
            GdTrans.pts_trans(++sc);
        } else {
            tra.vItems.getElement(itm.index).pnt += itm.pnt;
            GdSales.pnt_line(sc);
        }
    }

    private static void frequentShopperSetPromotionVariable(clsFsRewardData objFsRewardData) {
        if (callType > 0) return;
        logConsole(4, null, "PromoVarID=" + objFsRewardData.GetRewardPromoVarID());
        if (objFsRewardData.getUpdateHostWithCustomOffer() < 1) return;
        if (!prepAccumulation(objFsRewardData)) return;
        itm.amt = objFsRewardData.getExtendedValue();
        if (objFsRewardData.getReplacementFlag())
            itm.amt -= objFsRewardData.getOldExtendedValue();
        itm.cnt = (int) objFsRewardData.GetRewardPromoVarID();
        loyaltyVariableUpdate(objFsRewardData.GetRewardPromoVarID(), itm.amt, objFsRewardData.GetUserDefinedData());
        Itmdc.IDC_write('G', itm.cnt % 10, 1, itm.number, itm.cnt, itm.amt);
        Itmdc.IDC_write('K', trx_pres(), itm.flag, itm.promo, itm.rew_qty, itm.rew_amt);
    }

    private static void loyaltyVariableUpdate(long promovar, long value, String udd) {
        LoyaltyServiceInterface loyaltyService = LoyaltyService.getService();

        if (loyaltyService.isLoyaltyEnabled()) {
            if (udd.trim().isEmpty() || !udd.trim().endsWith("_")) udd = LoyaltyService.PROMO_HEADER;

            String variableValue = loyaltyService.getVariable(udd + promovar);
            long oldValue = !variableValue.isEmpty() && StringUtils.isNumeric(variableValue) ? Long.parseLong(variableValue) : 0;
            loyaltyService.setVariable(udd + promovar, String.valueOf(oldValue + value));
        }
    }

    private static void frequentShopperChoiceOkCancel(clsFsRewardData objFsRewardData) {
        if (objFsRewardData.getReversalFlag()) return;
        if (!objFsRewardData.getSimpleFlag())
            if (callType == 0) return;
        String txt = objFsRewardData.GetChoiceTitle().trim();
        int sts = GdPos.panel.clearLink(txt, 3);
        if (sts > 0) objFrequentShopper.SetChoiceAnswer(sts);
    }

    private static void frequentShopperChoiceMultiple(clsFsRewardData objFsRewardData) {
        int ind, sts;

        if (objFsRewardData.getReversalFlag()) return;
        if (!objFsRewardData.getSimpleFlag())
            if (callType == 0) return;
        for (; ; GdPos.panel.clearLink(Mnemo.getInfo(sts), 0x81)) {
            input.prompt = Mnemo.getText(15);
            input.init(0x00, 1, 1, 0);
            stsLine.init(objFsRewardData.GetChoiceTitle()).show(1);
            SelDlg dlg = new SelDlg(Mnemo.getText(22));
            for (ind = 0; ind++ < objFsRewardData.GetChoiceCount(); ) {
                dlg.add(9, editNum(ind, 1), " " + objFsRewardData.GetChoiceText(ind));
            }
            dlg.show("LBS");
            if ((sts = dlg.code) < 1) {
                if (input.key == 0) input.key = input.CLEAR;
                if (input.num < 1 || input.key != input.ENTER) sts = 5;
                else sts = input.adjust(input.pnt);
            }
            if (input.key == input.CLEAR) return;
            if (sts > 0) continue;
            sts = input.scanNum(input.num);
            if (sts > 0 && sts < ind) break;
            sts = 46;
        }
        objFrequentShopper.SetChoiceAnswer(sts);
    }

    public static void writePromoDetails(String promoCode) {
        tra.vItems_k.clear();
        for (clsFsRewardData reward : detailsList) {
            if (reward.getPromotionCode() == Long.parseLong(promoCode.substring(4).trim())) {
                Itmdc.IDC_write('k', trx_pres(), itm.flag, "" + reward.getEntryId(), (int) reward.GetSoldQuantity(), reward.GetSoldAmount());
            }
        }
    }

    private static void frequentShopperRewardDetail(clsFsRewardData objFsRewardData) {
        if (callType > 0) return;
        if (objFsRewardData.getReversalFlag()) return;

        logger.debug("ID: " + objFsRewardData.getEntryId());
        Itmdc.IDC_write('k', trx_pres(), itm.flag, "" + objFsRewardData.getEntryId(), (int) objFsRewardData.GetSoldQuantity(), objFsRewardData.GetSoldAmount());
    }

    private static void frequentShopperCashierDisplayMessage(clsFsRewardData objFsRewardData) {
        if (callType > 0) return;
        if (objFsRewardData.getReversalFlag()) return;
        for (int ind = 0; ind < objFsRewardData.getCashierLinesCount(); ind++) {
            String msg = objFsRewardData.getCashierLine(ind);
            if (SscoPosManager.getInstance().isUsed()) {
                if (objFsRewardData.GetUserDefinedData().contains("SSCO_SUP")) {
                    SscoPosManager.getInstance().sendDataNeeded("CashierDisplayMessageStore", msg);
                    SscoPosManager.getInstance().waitForDataneededClose();
                } else if (objFsRewardData.GetUserDefinedData().contains("SSCO_MSG")) {
                    SscoPosManager.getInstance().sendDataNeeded("CashierDisplayMessage", msg);
                    SscoPosManager.getInstance().waitForDataneededClose();
                }
            } else {
                gui.clearLink(msg, 0x81);
            }
        }
    }

    private static void frequentShopperCustomerReceiptMessage(clsFsRewardData objFsRewardData) {
        if (callType > 0) return;
        if (objFsRewardData.getReversalFlag()) return;
        for (int ind = 0; ind < objFsRewardData.getReceiptLinesCount(); ind++) {
            String msg = replaceMacro(objFsRewardData.getReceiptLine(ind));
            if (objFsRewardData.GetReceiptArea() == BEFORE_TOTAL_AREA) {
                prtLine.init(msg).book(3);
            } else {
                ReceiptMessage message = new ReceiptMessage(objFsRewardData.GetReceiptArea(), objFsRewardData.GetReceiptPosition(), objFsRewardData.getReceiptLine(ind));
                receiptMessages.add(message);
            }
        }
    }

    public static void frequentShopperPrintCustomerReceiptMessage(int area) {
        for (ReceiptMessage message : receiptMessages) {
            if (message.getArea() == area) {
                prtLine.init(message.getText()).book(3);
            }
        }
    }

    private static String replaceMacro(String receiptMessage) {
        String newMessage;
        try {
            String pVar = receiptMessage.substring(receiptMessage.indexOf('#') + 1, receiptMessage.lastIndexOf('#'));
            long value = getPromovar(Long.valueOf(pVar));

            newMessage = receiptMessage.substring(0, receiptMessage.indexOf('#')) + editPrice(Math.abs(value));
        } catch (Exception e) {
            newMessage = receiptMessage;
        }
        return newMessage;

    }

    private static void frequentShopperCustomerDisplayMessage(clsFsRewardData objFsRewardData) {
        if (callType > 0) return;
        if (objFsRewardData.getReversalFlag()) return;

        for (int ind = 0; ind < objFsRewardData.getReceiptLinesCount(); ind++) {
            String msg = objFsRewardData.getReceiptLine(ind);

            if (ind < 2) {
                Action.cusDisplay(ind, msg);
            }
        }
    }

    static boolean isNoPrintPoints() {
        return fso_line.substring(4, 6).equals("01");
    }

    private static long internalPromotionCode = 20000;

    public static void manualRewardDiscoutAmount(long promotionType, String description, long discountAmount,
                                               int priority, long entryId, String userDefinedData) {

        clsFsVirtualPromotionData objVirtualPromotionData = new clsFsVirtualPromotionData();
        objVirtualPromotionData.SetPromotionCode(internalPromotionCode++ * 10);
        objVirtualPromotionData.SetVirtualPromotionType(promotionType);
        objVirtualPromotionData.SetSimpleFlag(true);
        objVirtualPromotionData.SetRewardType(FS_REWARDTYPE_AMOUNT_OFF);
        objVirtualPromotionData.SetProcessingPriority((short) priority);
        objVirtualPromotionData.SetRewardExtendedValue(discountAmount);
        objVirtualPromotionData.SetDescription(description);
        objVirtualPromotionData.SetEntryID(entryId);
        if (userDefinedData != null && userDefinedData.length() > 0) {
            objVirtualPromotionData.SetUserDefinedData(userDefinedData);
        }
        if (clsFrequentShopper.SetVirtualPromotion(objVirtualPromotionData)) {
            processSimpleRewards();
        }
    }

    public static void manualRewardDiscoutPercent(long promotionType, String description, long discountPercentage,
                                                int priority, long entryId, String userDefinedData, int qty) {

        clsFsVirtualPromotionData objVirtualPromotionData = new clsFsVirtualPromotionData();

        objVirtualPromotionData.SetPromotionCode(internalPromotionCode++ * 10);
        objVirtualPromotionData.SetVirtualPromotionType(promotionType);
        objVirtualPromotionData.SetSimpleFlag(true);
        objVirtualPromotionData.SetRewardType(FS_REWARDTYPE_PERCENT_OFF);
        objVirtualPromotionData.SetProcessingPriority((short) priority);
        // lo sconto deve essere passato cosi': 16% -> 1600
        objVirtualPromotionData.SetRewardExtendedValue(discountPercentage);
        objVirtualPromotionData.SetDescription(description);
        objVirtualPromotionData.SetEntryID(entryId);
        objVirtualPromotionData.SetQuantity(qty);
        if (userDefinedData != null && userDefinedData.length() > 0) {
            objVirtualPromotionData.SetUserDefinedData(userDefinedData);
        }
        if (clsFrequentShopper.SetVirtualPromotion(objVirtualPromotionData)) {
            processSimpleRewards();

        }
    }

    public static HashMap<String, Itemdata> sellItemMap = new HashMap();

    Itemdata getSellItemMap(long entryId) {
        Itemdata itemdata = (Itemdata) sellItemMap.get(String.valueOf(entryId));

        if (itemdata == null) {
            itemdata = new Itemdata();
        }
        return itemdata;
    }

    private static void addSellItemAttribute(long entryId, Itemdata itemdata) {
        sellItemMap.put(String.valueOf(entryId), itemdata);
    }

}


