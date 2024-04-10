package com.ncr.common.utilities;

import com.ncr.GdPrice;
import com.ncr.Itemdata;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static com.ncr.Action.group;
import static com.ncr.Basis.event;
import static com.ncr.Basis.input;
import static com.ncr.FmtIo.editTxt;
import static com.ncr.Struc.*;
import static com.ncr.Table.lPLU;
import static com.ncr.Table.rMNT;

public class AutoSaleManager {
    private static final Logger logger = Logger.getLogger(AutoSaleManager.class);
    private static final String AUTO_SALE_PROPERTIES = "conf/auto-sale.properties";
    private static final String ENABLED = "enabled";
    private static final String TENDER_ITEMS = "tender-items.";
    private static final String TENDER_THRESHOLDS = "tender-thresholds.";
    private static final String TENDER_AUTO_INCREMENT = "tender-auto-increment.";
    private final Properties props = new Properties();
    private static AutoSaleManager instance = null;
    private boolean enabled = false;
    private String item = "";

    public static AutoSaleManager getInstance() {
        if (instance == null) {
            instance = new AutoSaleManager();
        }
        return instance;
    }

    private AutoSaleManager() {
        loadProperties();
    }

    private void loadProperties() {
        try {
            props.load(new FileInputStream(AUTO_SALE_PROPERTIES));
            enabled = Boolean.parseBoolean(props.getProperty(ENABLED, "false"));
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
        }
    }

    public void addCommission(Itemdata itm) {
        logger.debug("Enter");
        item = "";

        if (!enabled) {
            logger.debug("Not enabled");
            return;
        }
        if ("false".equals(props.getProperty(TENDER_AUTO_INCREMENT + itm.tnd, "false"))) {
            logger.debug ("Not incrementing amount: " + itm.amt);
            return;
        }

        item = getItem(itm);
        if (item.isEmpty()) return;

        itm.com = getItemPrice(editTxt(item, 16));
        itm.amt = itm.pos += itm.com;
        logger.debug("Exit. New amount: " + itm.amt);
    }

    static int getItemPrice (String key) {
        int price;
        int sts = lPLU.find(key, rMNT.recno > 0);
        if (sts < 1) {
            return -1;
        }
        try {
            price = lPLU.skip(70).scanNum(8);;
        } catch (NumberFormatException e) {
            lPLU.error(e, false);
            return -1;
        }
        return price;
    }

    private String getItem(Itemdata pri) {
        String item = "";

        String array = props.getProperty(TENDER_ITEMS + pri.tnd, "");
        List<String> items = Arrays.asList(array.split(";"));
        array = props.getProperty(TENDER_THRESHOLDS + pri.tnd, "");
        List<String> thresholds = Arrays.asList(array.split(";"));

        if (items.isEmpty()) {
            logger.debug("No automatic sale for this tender");
        } else {
            for (int index = 1; index < thresholds.size(); index++) {
                if (Long.parseLong(thresholds.get(index)) > pri.amt) {
                    item = items.get(index - 1);
                    break;
                }
            }
        }
        return item;
    }

    public void sellItem(Itemdata itemdata) {
        logger.debug("Enter");

        if (!enabled) {
            logger.debug("Not enabled");
            return;
        }
        //String item = getItem(itemdata);
        if (item.isEmpty()) return;

        int sts = 0;
        Itemdata temp = itm.copy();
        int next = event.nxt;
        input.prompt = "";
        input.reset(item);

        if ((sts = group[5].action2(0)) == 0) {
            if (event.spc == 0) {
                //TODO: Check if this is needed
                sts = group[5].action7(1);
            }
        }
        itm = temp.copy();
        event.nxt = next;

        logger.debug("Exit. sts: " + sts);
    }
}
