package com.ncr.ecommerce;

import com.ncr.Itemdata;
import com.ncr.Mnemo;
import com.ncr.Struc;
import com.ncr.ecommerce.data.Item;
import com.ncr.ssco.communication.manager.SscoPosManager;
import lombok.Getter;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.ncr.Action.group;
import static com.ncr.Basis.input;
import static com.ncr.Basis.panel;
import static com.ncr.Struc.itm;

public class QrCodeManager {
    private static QrCodeManager instance = null;
    private static final Logger logger = Logger.getLogger(QrCodeManager.class);
    @Getter
    private List<Item> items = new ArrayList<Item>();

    public static QrCodeManager getInstance() {
        if (instance == null) {
            instance = new QrCodeManager();
        }
        return instance;
    }

    private QrCodeManager() {
    }

    public void handleQrCode() {
        logger.debug("Enter");
        List<String> unsoldItems = new ArrayList<String>();
        int sts = 0;
        String[] itmList = input.qrcode.split("\\r?\\n");
        //String[] itmList = input.qrcode.split("xx");
        logger.info("input.qrcode: " + input.qrcode);

        items = buildQrCodeItemList(itmList, unsoldItems);
        input.qrcode = "";

        int stItm = itm.spf1;

        logger.info("Item list size: " + itmList.length);
        logger.info("itm spf1: " + itm.spf1);
        for (Item item : items) {
            sellQrCodeItem(unsoldItems, stItm, item);
        }
        if (SscoPosManager.getInstance().isUsed() && !unsoldItems.isEmpty()) {
            String separator = "";
            StringBuilder message = new StringBuilder();
            for (String item : unsoldItems) {
                message.append(separator).append(item);
                separator = " - ";
            }
            SscoPosManager.getInstance().sendDataNeeded("QrCodeUnsoldItems", message.toString());
        }

        logger.debug("Exit");
    }

    private List<Item> buildQrCodeItemList(String[] itmList, List<String> unsoldItems) {
        List<Item> qrCodeItemList = new ArrayList<Item>();

        for (String itmCurrent : itmList) {
            if(!itmCurrent.contains(";")) {
                qrCodeItemList.add(new Item(itmCurrent, BigDecimal.ZERO));
            }
            else {
                String code = itmCurrent.split(";")[0];
                try {
                    BigDecimal price = new BigDecimal(itmCurrent.split(";")[1]);
                    qrCodeItemList.add(new Item(code, price));
                } catch (Exception e) {
                    logger.error("Error parsing QR", e);
                    unsoldItems.add(code);
                }
            }
        }
        return qrCodeItemList;
    }

    private void sellQrCodeItem(List<String> unsoldItems, int stItm, Item item) {
        int sts;
        itm.spf1 = stItm;

        input.pb = item.getCode();
        itm.prpov = item.getPrice().multiply(new BigDecimal(Math.pow(10, Struc.tnd[0].dec))).intValue();
        itm.qrcode = true;

        logger.info("input pb: " + input.pb);
        logger.info("itm prprov: " + itm.prpov);

        input.num = input.pb.trim().length();

        if ((sts = group[5].action2(0)) != 0) {
            logger.info("sts: " + sts);
            if (SscoPosManager.getInstance().isUsed()) {
                unsoldItems.add(input.pb.trim());
            } else {
                panel.clearLink(Mnemo.getInfo(sts), 1);
            }
        }
    }

    public void reset() {
        items.clear();
    }
}
