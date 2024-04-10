package com.ncr;

import com.ncr.gui.GdLabel;
import com.ncr.gui.JournalTable;
import com.ncr.gui.KeyPadButton;

import java.util.Iterator;
import java.util.Map;

public class JournalAndDetail {

    private static JournalAndDetail journalAndDetail = new JournalAndDetail();

    private JournalAndDetail() {
    }

    public static JournalAndDetail getInstance() {
        return journalAndDetail;
    }

    private Itemdata panelDetailItemdata = new Itemdata();
    private Itemdata panelDetailItemdataLast = new Itemdata();
    private int voidCurrentEntryId = -1;
    private int voidedRow = -1;

    public void addCustomerLine(Itemdata itemdata) {
        if (GdPos.panel.journalTable != null && GdPos.panel.journalTable.supportModel(JournalTable.MODEL_NEW)) {
            itemdata.setItemType(Itemdata.CUSTOMER);
            GdPos.panel.journalTable.addToModel(JournalTable.MODEL_NEW, itemdata);
        }
        if (GdPos.panel.journalTable2Screen != null
                && GdPos.panel.journalTable2Screen.supportModel(JournalTable.MODEL_NEW)) {
            itemdata.setItemType(Itemdata.CUSTOMER);
            GdPos.panel.journalTable2Screen.addToModel(JournalTable.MODEL_NEW, itemdata);
        }
        if (GdPos.panel.touchMenu != null && TouchMenuParameters.getInstance().isSelfService()) {
            GdPos.panel.touchMenu.addJournalLine(false, -1, itemdata.number.trim(), itemdata.text.trim(), new Long(0),
                    new Long(0), new Long(0), (Struc.tra.IsResume() == false), false);

            GdPos.panel.touchMenu.setJournalVisible(true);
            GdPos.panel.touchMenu.setTotalVisible(true);
            GdPos.panel.touchMenu.setActionBarVisible(true);
        }
    }

    public void addFiscalCodeLine(Itemdata itemdata) {
        if (GdPos.panel.journalTable != null && GdPos.panel.journalTable.supportModel("NewStyle")) {
            itemdata.setItemType(Itemdata.FISCALCODE);
            GdPos.panel.journalTable.addToModel("NewStyle", itemdata);
        }
    }


    public void addLine(String line) {
        if (GdPos.panel.journalTable != null && GdPos.panel.journalTable.supportModel(JournalTable.MODEL_OLD)) {
            GdPos.panel.journalTable.addToModel(JournalTable.MODEL_OLD, line);
        }
    }

    public void addLaserJournalLine(String line) {
        if (GdPos.panel.journalTable != null && GdPos.panel.journalTable.supportModel(JournalTable.MODEL_LASER)) {
            GdPos.panel.journalTable.addToModel(JournalTable.MODEL_LASER, line);
        }

    }

    public void addItemLine(Itemdata itemdata) {
        if (GdPos.panel.journalTable != null && GdPos.panel.journalTable.supportModel(JournalTable.MODEL_NEW)) {
            itemdata.setItemType(Itemdata.ITEM);
            UtilLog4j.logInformation(this.getClass(), itemdata.dump());
            GdPos.panel.journalTable.addToModel(JournalTable.MODEL_NEW, itemdata);
        }
        if (GdPos.panel.journalTable2Screen != null
                && GdPos.panel.journalTable2Screen.supportModel(JournalTable.MODEL_NEW)) {
            itemdata.setItemType(Itemdata.ITEM);
            UtilLog4j.logInformation(this.getClass(), itemdata.dump());
            GdPos.panel.journalTable2Screen.addToModel(JournalTable.MODEL_NEW, itemdata);
        }
    }

    public void addDiscountLine(Itemdata itemdata) {
        if (GdPos.panel.journalTable != null && GdPos.panel.journalTable.supportModel(JournalTable.MODEL_NEW)) {
            itemdata.setItemType(Itemdata.DISCOUNT);
            UtilLog4j.logInformation(this.getClass(), itemdata.dump());
            GdPos.panel.journalTable.addToModel(JournalTable.MODEL_NEW, itemdata);
        }
        if (GdPos.panel.journalTable2Screen != null
                && GdPos.panel.journalTable2Screen.supportModel(JournalTable.MODEL_NEW)) {
            itemdata.setItemType(Itemdata.DISCOUNT);
            UtilLog4j.logInformation(this.getClass(), itemdata.dump());
            GdPos.panel.journalTable2Screen.addToModel(JournalTable.MODEL_NEW, itemdata);
        }
    }

    public void addCouponMessageLine(Itemdata itemdata) {
        if (GdPos.panel.journalTable != null && GdPos.panel.journalTable.supportModel(JournalTable.MODEL_NEW)) {
            itemdata.setItemType(Itemdata.COUPON_MESSAGE);
            UtilLog4j.logInformation(this.getClass(), itemdata.dump());
            GdPos.panel.journalTable.addToModel(JournalTable.MODEL_NEW, itemdata);
        }
    }

    // MMS-ECOUPONING#A BEGIN
    public void addCouponLine(Itemdata itemdata) {
        if (GdPos.panel.journalTable != null && GdPos.panel.journalTable.supportModel(JournalTable.MODEL_NEW)) {
            itemdata.setItemType(Itemdata.COUPON);
            UtilLog4j.logInformation(this.getClass(), itemdata.dump());
            GdPos.panel.journalTable.addToModel(JournalTable.MODEL_NEW, itemdata);
        }
        if (GdPos.panel.journalTable2Screen != null
                && GdPos.panel.journalTable2Screen.supportModel(JournalTable.MODEL_NEW)) {
            itemdata.setItemType(Itemdata.COUPON);
            UtilLog4j.logInformation(this.getClass(), itemdata.dump());
            GdPos.panel.journalTable2Screen.addToModel(JournalTable.MODEL_NEW, itemdata);
        }
    }
    // MMS-ECOUPONING#A END

    public void addSubtotalLine(Itemdata itemdata) {
        if (GdPos.panel.journalTable != null && GdPos.panel.journalTable.supportModel(JournalTable.MODEL_NEW)) {
            itemdata.setItemType(Itemdata.SUBTOTAL);
            UtilLog4j.logInformation(this.getClass(), itemdata.dump());
            GdPos.panel.journalTable.addToModel(JournalTable.MODEL_NEW, itemdata);
        }
        if (GdPos.panel.journalTable2Screen != null
                && GdPos.panel.journalTable2Screen.supportModel(JournalTable.MODEL_NEW)) {
            itemdata.setItemType(Itemdata.SUBTOTAL);
            UtilLog4j.logInformation(this.getClass(), itemdata.dump());
            GdPos.panel.journalTable2Screen.addToModel(JournalTable.MODEL_NEW, itemdata);
        }
    }

    public void addTenderLine(Itemdata itemdata) {
        if (GdPos.panel.journalTable != null && GdPos.panel.journalTable.supportModel(JournalTable.MODEL_NEW)) {
            itemdata.setItemType(Itemdata.TENDER);
            UtilLog4j.logInformation(this.getClass(), itemdata.dump());
            GdPos.panel.journalTable.addToModel(JournalTable.MODEL_NEW, itemdata);
        }
        if (GdPos.panel.journalTable2Screen != null
                && GdPos.panel.journalTable2Screen.supportModel(JournalTable.MODEL_NEW)) {
            itemdata.setItemType(Itemdata.TENDER);
            UtilLog4j.logInformation(this.getClass(), itemdata.dump());
            GdPos.panel.journalTable2Screen.addToModel(JournalTable.MODEL_NEW, itemdata);
        }
    }

    public void updateItemDetail(Itemdata itemdata) {
        GdPos.panel.status("Inquiry", itemdata.isInquiryItem());
        if (GdPos.panel.panelDetail != null) {
            panelDetailItemdataLast = panelDetailItemdata.copy();
            panelDetailItemdata = itemdata.copy();

            GdPos.panel.panelDetail.removeAll();
            Struc.dspBmap = "";

            if (itemdata.isItem() || itemdata.isDiscount() || itemdata.isCustomer() || itemdata.isInquiryItem()) {
                Map labelsMap = (Map) ArsXmlParser.getInstance().getPanelElement("ItemDetail", "Label");
                GdLabel gdLabel;

                Map buttonsMap = (Map) ArsXmlParser.getInstance().getPanelElement("ItemDetail", "Button");

                if (itemdata.isItem()) {
                    if (!itemdata.isVoided()) {
                        // inibisco la possibilit� di stornare un articolo
                        // se sono nei pagamenti
                        // if (!GdTrans.isTotalKeyPressed()) { //MMS-MANTIS-21139#D
                        if (!totalPressed) { // MMS-MANTIS-21139#A
                            KeyPadButton keyPadButton = (KeyPadButton) buttonsMap.get("Void");

                            GdPos.panel.panelDetail.add(keyPadButton);
                        }
                    }
                } else if (itemdata.isInquiryItem()) {
                    KeyPadButton keyPadButton = (KeyPadButton) buttonsMap.get("Sell");

                    GdPos.panel.panelDetail.add(keyPadButton);
                }

                if (itemdata.isVoided()) {
                    gdLabel = (GdLabel) labelsMap.get("Voided");
                    GdPos.panel.panelDetail.add(gdLabel);
                }

                gdLabel = (GdLabel) labelsMap.get("ItemCode");
                gdLabel.setText(itemdata.number.trim());
                GdPos.panel.panelDetail.add(gdLabel);
                gdLabel = (GdLabel) labelsMap.get("ItemDescription");
                gdLabel.setText(itemdata.text.trim());
                GdPos.panel.panelDetail.add(gdLabel);
                if (itemdata.dpt > 0) {
                    if (Action.lDPT.read(itemdata.dpt, FmtIo.LOCAL) > 0) {
                        gdLabel = (GdLabel) labelsMap.get("Department");
                        gdLabel.setText(LinIo.editKey(Action.lDPT.key, 4));
                        GdPos.panel.panelDetail.add(gdLabel);
                        gdLabel = (GdLabel) labelsMap.get("DepartmentDescription");
                        gdLabel.setText(Action.lDPT.text.trim());
                        GdPos.panel.panelDetail.add(gdLabel);
                    }
                }

                int discount = 0;
                Iterator iterator = itemdata.promotionListGui.iterator();

                while (iterator.hasNext()) {
                    Itemdata promotionItemdata = (Itemdata) iterator.next();

                    if (promotionItemdata.isDiscount()) {
                        discount += promotionItemdata.amt;
                    } else if (promotionItemdata.isMessage()) {
                        gdLabel = (GdLabel) labelsMap.get("ReceiptMessage");
                        gdLabel.setText(promotionItemdata.text.trim());
                        GdPos.panel.panelDetail.add(gdLabel);
                    }
                }

                if (discount != 0) {
                    gdLabel = (GdLabel) labelsMap.get("Promotion");
                    GdPos.panel.panelDetail.add(gdLabel);
                }

                if (itemdata.IsWeightItem()) {
                    gdLabel = (GdLabel) labelsMap.get("ItemPrice");
                    gdLabel.setText(Action.editMoney(0, itemdata.amt + discount));
                    GdPos.panel.panelDetail.add(gdLabel);
                    gdLabel = (GdLabel) labelsMap.get("ItemPricePerKg");
                    if (itemdata.prlbl != 0) {
                        gdLabel.setText(Action.editMoney(0, itemdata.prlbl));
                    } else {
                        gdLabel.setText(Action.editMoney(0, itemdata.price));
                    }
                    GdPos.panel.panelDetail.add(gdLabel);
                } else if (itemdata.isCustomer()) {
                    gdLabel = (GdLabel) labelsMap.get("ItemPrice");
                    gdLabel.setText(Action.editMoney(0, itemdata.price));
                    GdPos.panel.panelDetail.add(gdLabel);
                    gdLabel = (GdLabel) labelsMap.get("SaldoCassaforte");
                    GdPos.panel.panelDetail.add(gdLabel);

                } else {
                    gdLabel = (GdLabel) labelsMap.get("ItemPrice");
                    int price = itemdata.price + (discount / (itemdata.qty > 0 ? itemdata.qty : 1));

                    if (itemdata.spf1 == Struc.M_RETURN) {
                        price *= -1;
                    }
                    gdLabel.setText(Action.editMoney(0, price));
                    GdPos.panel.panelDetail.add(gdLabel);
                }
            }

            if (itemdata.isItem() || itemdata.isDiscount()) {
                Struc.dspBmap = "ITM_" + itemdata.number.trim() + ";DPT_" + LinIo.editKey(itemdata.dpt_nbr, 4);
            } else if (itemdata.isCustomer()) {
                Struc.dspBmap = "CUSTOMER";
            } else if (itemdata.isTender()) {
                Struc.dspBmap = "TND_" + LinIo.editNum(itemdata.tnd, 4);
            } else {
                Struc.dspBmap = "";
            }

            GdPos.panel.dspPicture(Struc.dspBmap);

            GdPos.panel.panelDetail.repaint();
        }
    }

    public void restoreLastItemDetail() {
        updateItemDetail(panelDetailItemdataLast);
    }

    public void sellCurrent() {
        int sts = 0;

        if (panelDetailItemdata.qty > 1) {
            Action.input.reset(String.valueOf(panelDetailItemdata.qty));
            sts = GdPos.panel.eventExecute(0x0010);
        }

        if (sts == 0) {
            if (panelDetailItemdata.number.trim().length() > 0) {
                Action.input.reset(panelDetailItemdata.number.trim());
                sts = GdPos.panel.eventExecute(ConIo.ENTER);
            } else {
                Action.input.reset(LinIo.editKey(panelDetailItemdata.dpt_nbr, 4));
                sts = GdPos.panel.eventExecute(0x05);
                if (sts == 0) {
                    Action.input.reset(String.valueOf(panelDetailItemdata.amt));
                    sts = GdPos.panel.eventExecute(ConIo.ENTER);
                }
            }
        }
    }

    public void voidCurrent() {
        voidCurrentEntryId = -1;
        int sts = GdPos.panel.eventExecute(0x00b9);

        if (sts == 0) {
            if (panelDetailItemdata.qty > 1) {
                Action.input.reset(String.valueOf(panelDetailItemdata.qty));
                sts = GdPos.panel.eventExecute(0x0010);
            }
        }
        if (sts == 0) {
            voidCurrentEntryId = (int) panelDetailItemdata.entryId;
            UtilLog4j.logInformation(this.getClass(), "voidCurrentEntryId=" + voidCurrentEntryId);

			if (panelDetailItemdata.number.trim().length() > 0) {
                Action.input.reset(panelDetailItemdata.number.trim());
                sts = GdPos.panel.eventExecute(ConIo.ENTER);
            } else {
                Action.input.reset(LinIo.editKey(panelDetailItemdata.dpt_nbr, 4));
                sts = GdPos.panel.eventExecute(0x05);
                if (sts == 0) {
                    Action.input.reset(String.valueOf(panelDetailItemdata.amt));
                    sts = GdPos.panel.eventExecute(ConIo.ENTER);
                }
            }

        }
        voidCurrentEntryId = -1;

    }

    public Itemdata getPanelDetailItemdata() {
        return panelDetailItemdata;
    }

    public boolean isVoidCurrentInProgress() {
        return voidCurrentEntryId > 0;
    }

    public long getVoidCurrentEntryId() {
        return voidCurrentEntryId;
    }

    // RAN-MANTIS-19200#A BEGIN
    public void setVoidCurrentEntryId(long voidCurrentEntryId) {
        this.voidCurrentEntryId = (int) voidCurrentEntryId;
    }

    public void resetVoidCurrentEntryId() {
        setVoidCurrentEntryId(-1);
    }
    // RAN-MANTIS-19200#A END

    public boolean isVoidedRow() {
        return voidedRow >= 0;
    }

    public void setVoidedRow(int currentRow) {
        this.voidedRow = currentRow;

    }

    public int getVoidedRow() {
        return voidedRow;
    }

    public void resetVoidedRow() {
        setVoidedRow(-1);
    }

    // MMS-MANTIS-21139#A BEGIN
    private boolean totalPressed = false;

    public void setTotalPressed(boolean totalPressed) {
        this.totalPressed = totalPressed;
    }
    // MMS-MANTIS-21139#A END

}
