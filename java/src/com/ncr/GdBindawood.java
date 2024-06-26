package com.ncr;

import com.ncr.common.data.TerminalInfo;
import com.ncr.common.utilities.CardReader;
import com.ncr.gui.SelDlg;
import com.ncr.loyalty.LoyaltyService;
import com.ncr.loyalty.LoyaltyServiceInterface;
import com.ncr.common.data.special.RedemptionItem;
import com.ncr.loyalty.aym.data.Variable;
import com.ncr.loyalty.data.PhoneCode;
import com.ncr.loyalty.transaction.*;
import com.ncr.ssco.communication.manager.SscoPosManager;
import com.ncr.struc.Customer;
import com.ncr.zatca.ZatcaManager;
import com.ncr.zatca.data.ZatcaStatus;
import lombok.Getter;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

public class GdBindawood extends Action {
    private static final Logger logger = Logger.getLogger(GdBindawood.class);
    private static final String INTERNATIONAL = "00";
    public static final String TYPE_MOBILE = "Mobile_Number";
    public static final String TYPE_LOYALTYID = "LoyaltyID";
    public static final int LOYALTY_DISABLED = 148;
    public static final int ENROLLMENT_ERROR = 149;
    public static final int IDENTIFICATION_ERROR = 150;
    public static final int ALREADY_IN_LOYALTY = 151;
    public static final int REDEMPTION_ERROR = 152;
    public static final int CARD_MATCH_ERROR = 153;
    public static final int OFFLINE_ERROR = 154;
    public static final int OFFLINE_WARNING = 155;
    public static final int POINTS_BALANCE = 23;
    public static final int IDENTIFICATION = 0;
    public static final int ENROLLMENT = 1;
    public static final int IDENT_CARD = 0;
    public static final int IDENT_MOBILE = 1;
    public static final String BASE_POINTS_MASK = "$base_points$";
    public static final String EARNED_POINTS_MASK = "$earned_points$";
    public static final String BONUS_POINTS_MASK = "$bonus_points$";
    public static final String REDEEMED_POINTS_MASK = "$redeemed_points$";
    public static final String STARTING_POINTS_MASK = "$starting_points$";
    public static final String FINAL_POINTS_MASK = "$final_points$";
    private static GdBindawood instance = null;
    private LoyaltyServiceInterface loyaltyService = LoyaltyService.getService();
    @Getter
    private boolean loyaltyActive = false;
    private String[] trailerLoyalty = new String[16];
    private static final String LOYALTY_TRAILER = "loyalty.trailer.";
    private static final String ERECEIPT_ENABLED = "e-receipts.enabled";
    private static final String ERECEIPT_TENDER_TYPE = "e-receipt.tender-type.";
    private static final String ERECEIPT_ASK_TO_PRINT_ENABLED = "e-receipts.ask-to-print.enabled";
    private static final String ERECEIPT_LOYALTY_ENABLING_VAR = "e-receipt.loyalty.enabling-variable";
    public static final String PROPERTIES = "conf/loyalty.properties";
    private Properties props = new Properties();
    private TerminalInfo terminalInfo;
    @Getter
    private boolean returnTransaction = false;

    private void loadProperties() {
        logger.debug("Enter");
        try {
            props.load(new FileInputStream(PROPERTIES));
        } catch (Exception e) {
            logger.error("Error: ", e);
        }
        logger.debug("Exit");
    }

    private GdBindawood() {
        loadProperties();
    }

    public static GdBindawood getInstance() {
        if (instance == null)
            instance = new GdBindawood();

        return instance;
    }

    public int action0(int spec) {
        logger.debug("Enter");
        terminalInfo = new TerminalInfo(
                editNum(ctl.sto_nbr, 4),
                editNum(ctl.reg_nbr, 3),
                editNum(ctl.ckr_nbr, 3),
                editNum(ctl.tran, 4));
        int sts = 0;
        if (loyaltyService.isLoyaltyTransaction()) {
            sts = ALREADY_IN_LOYALTY;
        } else {
            String accountIdValue = insertMobile();

            if (accountIdValue == null) {
                sts = 8;
            } else {
                String idType = TYPE_MOBILE;
                LoyaltyData loyaltyData = loyaltyService.customerEnrollment(accountIdValue, idType, terminalInfo);
                if (loyaltyData == null) {
                    sts = LOYALTY_DISABLED;
                } else {
                    sts = loyaltyActivation(loyaltyData, ENROLLMENT, ENROLLMENT_ERROR, 1);
                }
            }
        }
        logger.debug("Exit - sts: " + sts);
        return sts;
    }

    public int action1(int spec) {
        logger.debug("Enter");
        int sts = 0;
        if (loyaltyService.isLoyaltyTransaction()) {
            sts = ALREADY_IN_LOYALTY;
        } else if (spec == 0 && !SscoPosManager.getInstance().isEnabled() && !input.isEmpty()) {
            sts = 8;
        } else {
            if (!input.isEmpty()) event.nxt = event.alt;
            String accountIdValue = "";

            if (spec > 0 || !input.isEmpty()) {
                accountIdValue = (spec == 1 && input.isEmpty())? insertMobile() : input.pb;
                if (accountIdValue == null) {
                    sts = 8;
                } else {
                    String idType = (spec == 1 ? TYPE_MOBILE : TYPE_LOYALTYID);
                    if (SscoPosManager.getInstance().isEnabled()) {
                        if (accountIdValue.length() < 16) {
                            idType = TYPE_MOBILE;
                            accountIdValue = applyPhoneLogic(accountIdValue);
                            if (accountIdValue == null) {
                                logger.debug("Error in phone number: " + accountIdValue);
                                sts = IDENTIFICATION_ERROR;
                            }
                        } else {
                            idType = TYPE_LOYALTYID;
                        }
                    }
                    if (sts == 0) {
                        LoyaltyData loyaltyData = loyaltyService.customerIdentification(accountIdValue, idType);
                        if (loyaltyData == null) {
                            sts = LOYALTY_DISABLED;
                        } else {
                            sts = loyaltyActivation(loyaltyData, IDENTIFICATION, IDENTIFICATION_ERROR, spec);
                            //DMA-AYM_COUPON#A BEG
                            if (sts == 0){
                                AymCouponManager.getInstance().fetch(accountIdValue);
                            }
                            //DMA-AYM_COUPON#A END
                        }
                    }
                }
            }
        }
        logger.debug("Exit - sts: " + sts);
        return sts;
    }

    private String applyPhoneLogic(String accountId) {
        String modifiedAccountId = accountId;
        for (PhoneCode phoneCode : loyaltyService.getCountryCodes()) {
            if (accountId.startsWith(phoneCode.getPhoneCode())) {
                if (accountId.charAt(phoneCode.getPhoneCode().length()) == '0') {
                    modifiedAccountId = accountId.substring(0, phoneCode.getPhoneCode().length()) + accountId.substring(phoneCode.getPhoneCode().length() + 1);
                }
                logger.debug("AccountId: " + modifiedAccountId + "Matching Phone Code: " + phoneCode);
                if (modifiedAccountId.length() > phoneCode.getPhoneCode().length() + phoneCode.getRangeMax()) {
                    modifiedAccountId = null;
                }
            }
        }
        return modifiedAccountId;
    }

    public int action2(int spec) {
        logger.debug("Enter");
        int sts = 0;
        if (!loyaltyService.isLoyaltyTransaction()) {
            sts = 7;
        } else {
            GdPos.panel.clearLink(Mnemo.getDiag(POINTS_BALANCE).trim() + " " + cus.getPnt(), 0x81);
        }

        logger.debug("Exit - sts: " + sts);
        return sts;
    }

    public boolean isEreceiptEnabled() {
        return "true".equals(props.getProperty(ERECEIPT_ENABLED, "false"));
    }

    public boolean isEreceiptAskToPrintEnabled() {
        return "true".equals(props.getProperty(ERECEIPT_ASK_TO_PRINT_ENABLED, "false"));
    }

    public boolean queryUserForElectronicReceipt() {
        boolean eReceiptRequested = false;
        boolean printReceipt = true;
        ReceiptPrintManager receiptPrintManager = ReceiptPrintManager.getInstance();
        receiptPrintManager.setEnabled(true);

        if (loyaltyService.isLoyaltyTransaction()) {
            logger.debug("Loyalty active. No need to ask for mobile number");
            Map<String, LoyaltyVariable> loyaltyVariables = loyaltyService.getLoyaltyTransaction().getLoyaltyCustomer().getVariables();
            String variableKey = getProps().getProperty(ERECEIPT_LOYALTY_ENABLING_VAR);
            if (variableKey != null && loyaltyVariables.containsKey(variableKey)) {
                logger.debug("E-RECEIPT variable value: " + loyaltyVariables.get(variableKey).getValue());
                printReceipt = "true".equals(loyaltyVariables.get(variableKey).getValue());
            } else {
                if (!GdPsh.getInstance().getLstUtilities().isEmpty()) return true;
                if (!isEreceiptAskToPrintEnabled()) return true;
                logger.debug("Asking for physical receipt");
                printReceipt = receiptPrintManager.ask(Mnemo.getMenu(127), "OkCancelReceiptPrint");
            }

            eReceiptRequested = true;
        } else {
            if (receiptPrintManager.ask(Mnemo.getMenu(126), "OkCancelReceiveEReceipt")) {
                logger.debug("Customer wants electronic receipt");
                String mobileNumber = insertMobile();
                logger.debug("Mobile inserted: " + mobileNumber);
                if (mobileNumber != null && !mobileNumber.isEmpty()) {
                    eReceiptRequested = true;
                    loyaltyService.getLoyaltyTransaction().setEReceiptRecipient(mobileNumber);
                    if (!GdPsh.getInstance().getLstUtilities().isEmpty()) return true;
                    if (!isEreceiptAskToPrintEnabled()) return true;
                    logger.debug("Asking for physical receipt");
                    printReceipt = receiptPrintManager.ask(Mnemo.getMenu(127), "OkCancelReceiptPrint");
                }
            }
        }
        loyaltyService.getLoyaltyTransaction().setEReceiptRequested(eReceiptRequested);
        return printReceipt;
    }

    private int loyaltyActivation(LoyaltyData loyaltyData, int idcType, int errorCode, int spec) {
        int sts;
        if (loyaltyData != null) {
            if (loyaltyData instanceof LoyaltyCustomer) {
                LoyaltyCustomer loyaltyCustomer = (LoyaltyCustomer) loyaltyData;
                if (idcType == ENROLLMENT && loyaltyCustomer.isOffline()) return OFFLINE_ERROR;
                setCustomerData(cus, loyaltyCustomer);
                input.reset(loyaltyCustomer.isOffline() ? loyaltyCustomer.getAccountIdValue() : loyaltyCustomer.getCard());
                sts = GdCusto.getInstance().action1(spec);
                if (sts == 0) {
                    startLoyaltyTransaction(loyaltyCustomer, idcType == ENROLLMENT);
                    Itmdc.IDC_write('c', idcType, spec, tra.number, tra.cnt, 0l);
                }
                if (idcType == IDENTIFICATION && loyaltyCustomer.isOffline()) {
                    dspLine.init(Mnemo.getInfo(OFFLINE_WARNING));
                }
                loyaltyService.setOffline(loyaltyCustomer.isOffline());
                loyaltyStatus();
            } else {
                LoyaltyError loyaltyError = (LoyaltyError) loyaltyData;
                logger.info("Loyalty error: " + loyaltyError.getResponseCode() + " - " + loyaltyError.getResponseMessage());
                String error = rightFill(loyaltyError.getResponseCode() + " - " + loyaltyError.getResponseMessage(), 40, ' ');
                GdPos.panel.display(1, error.substring(0, 20));
                GdPos.panel.display(2, error.substring(20, 40));
                sts = errorCode;
            }
        } else {
            sts = errorCode;
        }
        return sts;
    }

    private void setCustomerData(Customer cus, LoyaltyCustomer loyaltyCustomer) {
        cus.setPnt(loyaltyCustomer.getPoints().getStartingPoints().intValue());
        if (loyaltyCustomer.getFirstName() != null) cus.setName(loyaltyCustomer.getFirstName() + " ");
        if (loyaltyCustomer.getLastName() != null) cus.setName(cus.getName() + loyaltyCustomer.getLastName());
        cus.setMobile(loyaltyCustomer.getMobile());
        cus.setCusId(loyaltyCustomer.getCard());
        cus.setBranch(Integer.parseInt(loyaltyService.getBranch()));
    }

    public void initialize_() {
        if (loyaltyService.isLoyaltyEnabled() && !loyaltyActive) {
            loyaltyActive = loyaltyService.initialize(tnd[0].dec);
            if (!loyaltyActive) {
                logger.warn("Loyalty service disabled");
                if (!SscoPosManager.getInstance().isEnabled()) GdPos.panel.clearLink(Mnemo.getInfo(LOYALTY_DISABLED), 1);
            }
            loyaltyStatus();
        }
    }

    public void startLoyaltyTransaction(LoyaltyCustomer loyaltyCustomer, boolean enrollment) {
        if (loyaltyActive) {
            startTransaction(loyaltyCustomer, enrollment);
            Promo.identifyCustomer(false);
            setPromovars();
            Promo.processSimpleRewards();
        }
    }

    public void startTransaction(LoyaltyCustomer loyaltyCustomer, boolean enrollment) {
        logger.debug("Check Transaction Type");
        if ((tra.spf1 & M_TRRTRN) > 0) {
            setReturnTransaction(true);
        }
        logger.debug("Is a Return transaction? " + returnTransaction);
        String uniqueId = "20" + ctl.date
                + editNum(ctl.sto_nbr, 3)
                + editNum(ctl.reg_nbr, 3)
                + editNum(ctl.tran, 4);
        terminalInfo = new TerminalInfo(
                editNum(ctl.sto_nbr, 4),
                editNum(ctl.reg_nbr, 3),
                editNum(ctl.ckr_nbr, 3),
                editNum(ctl.tran, 4));
        loyaltyService.startTransaction(uniqueId, loyaltyCustomer, terminalInfo, returnTransaction, enrollment);
    }

    private void setPromovars() {
        if (loyaltyService.isLoyaltyEnabled()) {
            Map<String, LoyaltyVariable> loyaltyVariables = loyaltyService.getLoyaltyTransaction().getLoyaltyCustomer().getVariables();
            for (String loyaltyVariable : loyaltyVariables.keySet()) {
                for (String header : getPromoHeaders()) {
                    if (loyaltyVariable.startsWith(header)) {
                        try {
                            Promo.setPromovar(Long.parseLong(loyaltyVariable.substring(header.length())),
                                    Long.parseLong(loyaltyVariables.get(loyaltyVariable).getValue()), false);
                        } catch (Exception e) {
                            logger.error("Error converting promovar: ", e);
                        }
                    }
                }
            }
        }
    }

    public void endTransaction() {
        if (loyaltyActive) {
            setTransactionData();
            LoyaltyData loyaltyData = loyaltyService.endTransaction(tra.mode > M_GROSS);

            if (loyaltyData instanceof LoyaltyCustomer) {
                LoyaltyPoints loyaltyPoints = ((LoyaltyCustomer) loyaltyData).getPoints();
                printPointRecap(loyaltyPoints);
                logger.debug("Loyalty transaction ended");
            } else {
                logger.debug("Non loyalty transaction ended");
            }
        }
    }

    private void printPointRecap(LoyaltyPoints loyaltyPoints) {
        String[] resPoints = setTrailerArrayValues(loyaltyPoints);
        prtBlock(ELJRN + 3, resPoints, 0, resPoints.length);
    }

    private String[] setTrailerArrayValues(LoyaltyPoints loyaltyPoints) {
        String[] res = new String[16];
        for (int index = 0; index < 16; index++) {
            String trailer =  props.getProperty(LOYALTY_TRAILER + index);
            if (trailer != null) {
                if (trailer.contains(EARNED_POINTS_MASK)) {
                    if (trailer.charAt(0) != '0' || loyaltyPoints.getEarnedPoints().intValue() > 0) {
                        res[index] = trailer.replace(EARNED_POINTS_MASK, leftFill(String.valueOf(loyaltyPoints.getEarnedPoints().intValue()), 6, ' ')).substring(2);
                    }
                }
                if (trailer.contains(BASE_POINTS_MASK)) {
                    if (trailer.charAt(0) != '0' || loyaltyPoints.getBasePoints().intValue() > 0) {
                        res[index] = trailer.replace(BASE_POINTS_MASK, leftFill(String.valueOf(loyaltyPoints.getBasePoints().intValue()), 6,  ' ')).substring(2);
                    }
                }
                if (trailer.contains(BONUS_POINTS_MASK)) {
                    if (trailer.charAt(0) != '0' || loyaltyPoints.getBonusPoints().intValue() > 0) {
                        res[index] = trailer.replace(BONUS_POINTS_MASK, leftFill(String.valueOf(loyaltyPoints.getBonusPoints().intValue()), 6,  ' ')).substring(2);
                    }
                }
                if (trailer.contains(REDEEMED_POINTS_MASK)) {
                    if (trailer.charAt(0) != '0' || loyaltyPoints.getRedeemedPoints().intValue() > 0) {
                        res[index] = trailer.replace(REDEEMED_POINTS_MASK, leftFill(String.valueOf(loyaltyPoints.getRedeemedPoints().intValue()), 6,  ' ')).substring(2);
                    }
                }
                if (trailer.contains(STARTING_POINTS_MASK)) {
                    if (trailer.charAt(0) != '0' || loyaltyPoints.getStartingPoints().intValue() > 0) {
                        res[index] = trailer.replace(STARTING_POINTS_MASK, leftFill(String.valueOf(loyaltyPoints.getStartingPoints().intValue()), 6,  ' ')).substring(2);
                    }
                }
                if (trailer.contains(FINAL_POINTS_MASK)) {
                    if (trailer.charAt(0) != '0' || loyaltyPoints.getStartingPoints().add(loyaltyPoints.getEarnedPoints().subtract(loyaltyPoints.getRedeemedPoints())).intValue() > 0) {
                        res[index] = trailer.replace(FINAL_POINTS_MASK, leftFill(String.valueOf(loyaltyPoints.getStartingPoints().add(loyaltyPoints.getEarnedPoints().subtract(loyaltyPoints.getRedeemedPoints())).intValue()), 6,  ' ')).substring(2);
                    }
                }
            }

        }
        return res;
    }

    private void addItem(Itemdata itm) {
        Item item = new Item(
                itm.number,
                itm.qty,
                new BigDecimal(itm.amt + itm.crd).divide(new BigDecimal(Math.pow(10, tnd[0].dec))),
                new Date(),
                itm.text,
                itm.eXline,
                "",
                new BigDecimal(itm.price).divide(new BigDecimal(Math.pow(10, tnd[0].dec))),
                new LoyaltyDiscount(itm.crdDescription, new BigDecimal(itm.crd).divide(new BigDecimal(Math.pow(10, tnd[0].dec)))),
                itm.index);
        if (itm.serial.length() > 0) {
            item.getVariables().add(new Variable(LoyaltyService.SERIAL_NO_VARIABLE, itm.serial));
        }
        loyaltyService.addItem(item);
    }

    private void addTender(Itemdata itm) {
        Tender tender = new Tender(new BigDecimal(itm.amt).divide(new BigDecimal(Math.pow(10, tnd[0].dec))), mapTenderType(itm));
        tender.getAdditionalInfos().addAll(itm.getAdditionalInfos());
        loyaltyService.addTender(tender);
    }

    private String mapTenderType(Itemdata itm) {
        String type = getProps().getProperty(ERECEIPT_TENDER_TYPE + itm.tnd, "");
        if (type.isEmpty()) {
            type = getProps().getProperty(ERECEIPT_TENDER_TYPE + tnd[itm.tnd].getType(), "Cash");
        }
        return type;
    }

    private void addVat(Itemdata itm) {
        Vat vat = new Vat(
                new BigDecimal(itm.dsc).divide(new BigDecimal(Math.pow(10, tnd[0].dec))),
                new BigDecimal(itm.price).divide(BigDecimal.TEN),
                new BigDecimal(itm.amt).divide(new BigDecimal(Math.pow(10, tnd[0].dec))),
                new BigDecimal(itm.pos).divide(new BigDecimal(Math.pow(10, tnd[0].dec)))
        );
        loyaltyService.addVat(vat);
    }

    private void updateTransactionTotal(long amt) {
        loyaltyService.updateTransactionTotal(new BigDecimal(amt).divide(new BigDecimal(Math.pow(10, tnd[0].dec))));
    }

    public void setTransactionData() {
        for (int ind = 0; ind < tra.vItems.size(); ind++) {
            Itemdata ptr = tra.vItems.getElement(ind);
            if ("SCT".indexOf(ptr.id) < 0)
                continue;
            switch (ptr.id) {
                case 'S':
                    getDiscount(ind, ptr);
                    addItem(ptr);
                    break;
                case 'T':
                    addTender(ptr);
                    break;
                default:
                    break;
            }
        }

        for (int ind = 0; ind < vat.length; ind++) {
            int rec = reg.find(7, 11 + ind);
            if (rec < 1) continue;
            Sales sls = reg.sales[rec - 1][0];
            if (sls.isZero()) continue;
            itm = new Itemdata();
            itm.dsc = roundBy(sls.total * vat[ind].rate * 10 / (1000 + vat[ind].rate), 10);
            itm.amt = sls.total - itm.dsc;
            itm.pos = sls.total;
            itm.price = vat[ind].rate;
            addVat(itm);
        }
        ZatcaStatus zatcaStatus = ZatcaManager.getInstance().getCurrentStatus();
        if (zatcaStatus != null) loyaltyService.getLoyaltyTransaction().setQrCode(zatcaStatus.getQr());
        updateTransactionTotal(tra.amt);
    }

    private void getDiscount(int ind, Itemdata ptr) {
        int innerIndex = ind + 1;
        long discount = 0;
        while (innerIndex < tra.vItems.size()) {
            Itemdata dsc = tra.vItems.getElement(innerIndex);
            if (dsc.id == 'C') {
                discount += dsc.amt;
                innerIndex++;
            } else break;
        }
        ptr.crd = discount;
    }

    public RedemptionItem redeemReward(long amt) {
        int sts = 0;
        if (!loyaltyService.isLoyaltyTransaction()) {
            sts = 7;
        } else if (loyaltyService.isOffline()) {
            sts = OFFLINE_ERROR;
        } else {
            LoyaltyCustomer loyaltyCustomer = loyaltyService.getLoyaltyTransaction().getLoyaltyCustomer();
            boolean result = true;
            String customer = loyaltyCustomer.getAccountIdValue();
            if (loyaltyCustomer.getAccountIdValue().equals(loyaltyCustomer.getMobile())) {
                if (SscoPosManager.getInstance().isEnabled()) {
                    SscoPosManager.getInstance().sendDataNeeded("InsertLoyaltyCard");
                    SscoPosManager.getInstance().waitForDataneededClose();
                    customer = SscoPosManager.getInstance().getInsertedCode();
                    if (customer == null) result = false;
                } else {
                    dspLine.init(Mnemo.getMenu(83)).show(1);
                    result = CardReader.readCard(Mnemo.getMenu(23), 83, 1, 16, 0, 0);
                    customer = input.pb;
                }
            }
            if (SscoPosManager.getInstance().isEnabled() && result) {
                SscoPosManager.getInstance().sendDataNeeded("InsertLoyaltyAmount");
                SscoPosManager.getInstance().waitForDataneededClose();
                if (SscoPosManager.getInstance().getInsertedCode() == null)
                    result = false;
                else {
                    Long amount = Long.parseLong(SscoPosManager.getInstance().getInsertedCode());
                    if (amount > amt) result = false;
                    else amt = amount;
                }
            }
            if (result) {
                if (customer.equals(loyaltyCustomer.getCard())) {
                    LoyaltyData loyaltyData = loyaltyService.rewardsRedemption(new BigDecimal(amt).divide(new BigDecimal(Math.pow(10, tnd[0].dec))));
                    sts = rewardActivation(loyaltyData);
                } else {
                    sts = CARD_MATCH_ERROR;
                }
            } else {
                sts = REDEMPTION_ERROR;
            }
        }
        return new RedemptionItem(amt, sts);
    }

    private int rewardActivation(LoyaltyData loyaltyData) {
        int sts = 0;
        if (loyaltyData != null) {
            if (loyaltyData instanceof Reward) {
                loyaltyService.getLoyaltyTransaction().getRewards().add((Reward) loyaltyData);
            } else {
                LoyaltyError loyaltyError = (LoyaltyError) loyaltyData;
                logger.info("Loyalty error: " + loyaltyError.getResponseCode() + " - " + loyaltyError.getResponseMessage());
                String error = rightFill(loyaltyError.getResponseCode() + " - " + loyaltyError.getResponseMessage(), 40, ' ');
                GdPos.panel.display(1, error.substring(0, 20));
                GdPos.panel.display(2, error.substring(20, 40));
                sts = REDEMPTION_ERROR;
            }
        } else {
            sts = REDEMPTION_ERROR;
        }
        return sts;
    }

    public void setLoyaltyActive(boolean loyaltyActive) {
        this.loyaltyActive = loyaltyActive;
    }

    public void setReturnTransaction(boolean returnTransaction) {
        this.returnTransaction = returnTransaction;
    }

    public void prompt() {
        if (isLoyaltyActive()) {
            dspLine.init(Mnemo.getMenu(119)).show(1);
        }
    }

    private String insertMobile() {
        logger.debug("Enter");

        int code = selectPhoneCode(loyaltyService.getCountryCodes(), 5);
        logger.debug("country code: " + code);

        if (code < 0 || code > 9999) return null;

        StringBuffer phoneNumber = new StringBuffer(INTERNATIONAL + code);

        int min = 0, max = 0;
        boolean trimLeadingZeroes = false;

        for (PhoneCode phoneCode : loyaltyService.getCountryCodes()) {
            if (phoneCode.getPhoneCode().equals(INTERNATIONAL + code)) {
                min = phoneCode.getRangeMin();
                max = phoneCode.getRangeMax();
                trimLeadingZeroes = phoneCode.isTrimLeadingZeroes();
                break;
            }
        }

        logger.debug("min: " + min + " max: " + max + " trim: " + trimLeadingZeroes);

        dspLine.init("").show(1);
        boolean result = acceptNbr(Mnemo.getMenu(87), 92, min, max, max, 0);
        logger.debug("result insert phone number: " + result);

        dspLine.init("").show(1);

        if (!result) return null;

        String number = input.pb;
        while (trimLeadingZeroes && number.charAt(0) == '0') {
            number = number.substring(1);
        }
        phoneNumber.append(number);
        logger.debug("full phoneNumber: " + phoneNumber);
        return phoneNumber.toString();
    }

    private int selectPhoneCode(List<PhoneCode> codesList, int len) {
        logger.debug("Enter - codeList.size: " + codesList.size());

        int code = 0;
        SelDlg dlg = new SelDlg(Mnemo.getText(22));

        for (PhoneCode phoneCode : codesList) {
            dlg.add(8, phoneCode.getPhoneCode(), " " + phoneCode.getCountry());
        }

        input.reset("");
        input.prompt = Mnemo.getMenu(91); //INTERNATIONAL CODE
        input.init(0x00, len, len, 0);
        input.key = input.CLEAR;

        dlg.show("MNU");

        if (dlg.code > 0)
            return dlg.code;
        if (input.key == 0)
            input.key = input.CLEAR;
        if (input.num < 1) {
            if (input.key == input.ENTER) input.reset(codesList.get(0).getPhoneCode());
            else return -1;
        }

        if ((code = input.adjust(input.pnt)) > 0)
            return code;

        if (input.num > len) return -1;
        code = input.scanNum(input.num);
        if (code < 1)
            return -1;
        logger.debug("Exit returning " + code);
        return code;
    }

    public Properties getProps() {
        return props;
    }

    private List<String> getPromoHeaders() {
        String value = props.getProperty("promotion.headers", LoyaltyService.PROMO_HEADER);
        String[] tokens = value.split(";");
        return Arrays.asList(tokens);
    }
}
