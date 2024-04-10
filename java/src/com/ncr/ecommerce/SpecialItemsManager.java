package com.ncr.ecommerce;

import com.ncr.*;
import com.ncr.ecommerce.data.Basket;
import com.ncr.ecommerce.data.Item;
import com.ncr.ecommerce.data.SpecialItem;
import lombok.Getter;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.*;

import static com.ncr.Action.sc_checks;
import static com.ncr.Basis.editMoney;
import static com.ncr.Struc.*;
import static com.ncr.Table.*;

public class SpecialItemsManager {
    private static final String ENABLED = "special-sales.enabled";
    private static SpecialItemsManager instance = null;
    private static final Logger logger = Logger.getLogger(SpecialItemsManager.class);
    private final Map<String, SpecialItem> specialItems = new HashMap<String, SpecialItem>();
    private final List<Item> items = new ArrayList<Item>();
    @Getter
    private boolean enabled = false;

    public static SpecialItemsManager getInstance() {
        if (instance == null) {
            instance = new SpecialItemsManager();
        }
        return instance;
    }

    private SpecialItemsManager() {
    }

    public void init(Properties props) {
        enabled = Boolean.parseBoolean(props.getProperty(ENABLED, "false"));
        loadSpecialItems(props);
    }

    private void loadSpecialItems(Properties props) {
        for (Enumeration<?> e = props.propertyNames(); e.hasMoreElements(); ) {
            String name = (String)e.nextElement();
            String value = props.getProperty(name);

            if (name.startsWith("special.")) {
                String description = name.substring("special.".length());
                String[] tokens = value.split(";");
                SpecialItem specialItem = new SpecialItem(tokens[0], description, tokens[1]);
                specialItems.put(tokens[0], specialItem);
                logger.debug("Special Item: " + description + " " + tokens[0] + " " + tokens[1]);
            }
        }
    }

    public void sellSpecialItems(boolean negative) {
        if (!isEnabled()) return;

        Basket basket = ECommerceManager.getInstance().getBasket();
        if (basket == null) {
            logger.debug("Not an e-commerce basket");
        } else {
            for (Item item : basket.getItems()) {
                if (getSpecialItem(item) != null) items.add(item);
            }
        }
        if (items.isEmpty()) {
            logger.debug ("No special items to be dealt with");
            return;
        }
        logger.debug("Special items: " + items.size());
        for (Item item : items) {
            int result = sellSpecialItem(item, negative);
        }
        items.clear();
        QrCodeManager.getInstance().reset();
    }

    private int sellSpecialItem(Item item, boolean negative) {
        logger.debug("Enter");
        int ret, ic, sc;

        try {
            SpecialItem specialItem = getSpecialItem(item);
            ic = Integer.parseInt(specialItem.getTotalizer().substring(0, 2));
            sc = Integer.parseInt(specialItem.getTotalizer().substring(2));
        } catch (Exception ex) {
            logger.error("Error: ", ex);
            ic = 4; sc = 4;
        }
        if ((ret = sc_checks(ic, sc)) > 0)
            return ret;
        itm = new Itemdata();
        itm.cnt = item.getQty();
        itm.crd = item.getPrice().multiply(BigDecimal.valueOf(Math.pow(10, tnd[0].dec))).longValue();
        itm.crd *= negative ? -1 : 1;
        itm.number = item.getBarcode();
        Itmdc.IDC_write('D', sc, itm.dpt_nbr = 0, "", itm.cnt, itm.crd);
        Itmdc.IDC_write('i', sc, itm.dpt_nbr = 0, itm.number, itm.cnt, itm.crd);
        prtLine.init(' ').onto(20, Mnemo.getText(23)).upto(40, editMoney(0, tra.amt)).book(3);
        lREG.read(reg.find(ic, sc), LOCAL);
        itm.text = lREG.text;
        prtLine.init(lREG.text).upto(25, itm.number).upto(40, editMoney(0, itm.crd)).book(3);
        TView.append('D', 0x00, lREG.text, "", itm.number, editMoney(0, itm.crd), "");
        accumReg(4, sc, itm.cnt, itm.crd);
        tra.dsc_cnt = tra.cnt;
        tra.dsc_sls = tra.amt;
        tra.amt += itm.crd;
        GdTrans.rbt_distrib();
        logger.debug("Exit - ret: " + ret);
        return ret;
    }

    public SpecialItem getSpecialItem(Item item) {
        for (SpecialItem specialItem : specialItems.values()) {
            if (item.getBarcode().trim().equals(specialItem.getBarcode())) return specialItem;
        }
        return null;
    }

    public boolean storeSpecialItem(Itemdata itm) {
        if (!isEnabled()) return false;
        if (ECommerceManager.getInstance().isTransactionStarted()) return false;

        Item item = new Item(itm.number.trim(), new BigDecimal(itm.prpov > 0 ? itm.prpov : itm.price).divide(new BigDecimal(Math.pow(10, Struc.tnd[0].dec))));
        if (getSpecialItem(item) != null) {
            items.add(item);
            return true;
        }
        return false;
    }

    public long getTotalAmount(boolean negative) {
        long amount = 0;
        for (Item item : items) {
            long credit = item.getPrice().multiply(BigDecimal.valueOf(Math.pow(10, tnd[0].dec))).longValue();
            credit *= negative ? -1 : 1;
            amount += credit;
        }
        return amount;
    }

    public void clear() {
        items.clear();
    }
}
