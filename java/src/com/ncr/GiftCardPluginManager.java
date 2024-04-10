package com.ncr;

import com.ncr.common.data.special.RedemptionItem;
import com.ncr.common.data.special.RefundItem;
import com.ncr.giftcard.GiftCardPluginInterface;
import com.ncr.giftcard.psh.data.CustomTender;
import com.ncr.ssco.communication.manager.SscoPosManager;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.ncr.GdPsh.MNEMO_ERROR_BASE;

public class GiftCardPluginManager extends Action {
    private static final Logger logger = Logger.getLogger(GiftCardPluginManager.class);
    private static GiftCardPluginManager instance = null;

    private final static String PROP_FILENAME = "conf/gift-card-plugins.properties";
    private final static Properties props = new Properties();
    public static final String GIFTCARD_TENDER_TYPES = "LS";

    public final static String NEW_RECEIPT_TYPE = "new-receipt";
    public final static String SAME_RECEIPT_TYPE = "same-receipt";
    private Map<String, GiftCardPluginInterface> plugins = new HashMap<String, GiftCardPluginInterface>();

    public static GiftCardPluginManager getInstance() {
        if (instance == null)
            instance = new GiftCardPluginManager();
        return instance;
    }

    public void init() {
        loadGiftCardProperties();
    }

    private GiftCardPluginManager() {
    }

    private void loadGiftCardProperties() {
        logger.debug("Enter");

        String packageName = GiftCardPluginManager.class.getPackage().getName();
        try {
            props.load(new FileInputStream(PROP_FILENAME));

            for (Object key : props.keySet()) {
                String className = (String) key;
                if (props.getProperty(className, "false").equals("true")) {
                    Class<?> c = Class.forName(packageName + "." + className);
                    Method method = c.getDeclaredMethod("getInstance");
                    GiftCardPluginInterface giftCard = (GiftCardPluginInterface) method.invoke(null, null);
                    plugins.put(giftCard.getTenderId(), giftCard);
                    logger.info("Plugin " + className + " enabled");
                }
            }
        } catch (Exception e) {
            logger.error("Error creating plugins", e);
        }
        logger.debug("Exit loadGiftCardProperties");

    }

    public GiftCardPluginInterface getPlugin(String pluginId) {
        if (!GIFTCARD_TENDER_TYPES.contains(pluginId)) return getEnabledPlugin();
        return plugins.get(pluginId);
    }

    public int sellGiftCard(Itemdata itm) {
        logger.debug("Enter SellGiftCard " + this.getClass().getName());
        int res;

        if (itm.giftCardTopup) {
            res = reloadGiftCard(itm);
        } else {
            res = activationGiftCard(itm, ctl);
        }
        logger.debug("Exit SellGiftCard with Response code = " + res);
        return res;
    }

    public int activationGiftCard(Itemdata itm, Terminal ctl) {
        logger.debug("Enter activationGiftCard ");
        int res = 0;
        GiftCardPluginInterface plugin = getPlugin(itm.gCard + "");
        if (plugin != null) {
            if (itm.qty != 1) {
                logger.error("Error multiple GiftCard item");
                return 87;
            }
            res = plugin.activationGiftCard(itm, ctl);
            logger.debug("GifCard Plugin: " + plugin.getClass().getName());
        }
        logger.debug("Response status = " + res);
        logger.debug("Exit activationGiftCard with Response code = " + res);
        return res;
    }

    public RefundItem refundGifCard(Itemdata itm, Terminal ctl) {
        logger.debug("Enter refundGifCard");
        int sts = 0;
        GiftCardPluginInterface plugin = getPlugin(itm.gCard + "");
        if (plugin != null) {
            sts = plugin.refundGiftCard(itm, ctl);
        }
        logger.debug("Exit ReloadGiftCard with Response code = " + sts);

        return new RefundItem(itm.amt, sts);
    }

    public int reloadGiftCard(Itemdata itm) {
        logger.debug("Enter ReloadGiftCard");
        int res = 0;
        GiftCardPluginInterface plugin = getPlugin(itm.gCard + "");
        if (plugin != null) {
            res = plugin.reloadGiftCard(itm);
        }
        logger.debug("Response status = " + res);
        logger.debug("Exit ReloadGiftCard with Response code = " + res);
        return res;
    }

    public RedemptionItem redemptionGiftCard(Itemdata itm, Transact tra) {
        int res = 0;
        logger.debug("Enter RedemptionGiftCard");

        GiftCardPluginInterface plugin = getPlugin(tnd[itm.tnd].getType() + "");// getEnabledPlugin();
        if (plugin == null)
            res = 7;
        else {
            if (itm.amt - itm.com > tra.bal) {
                res = MNEMO_ERROR_BASE + 3;
            } else {
                SscoPosManager sscoPosManager = SscoPosManager.getInstance();
                if (sscoPosManager.isEnabled()) {
                    if (sscoPosManager.skipGiftCardAmount()) {
                        logger.debug("Skipping");
                        itm.amt = tra.bal;
                    } else {
                        logger.debug("Not Skipping");
                        sscoPosManager.sendDataNeeded("InsertAmount");
                        sscoPosManager.waitForDataneededClose();
                        if (sscoPosManager.getInsertedCode() == null)
                            res = MNEMO_ERROR_BASE + 3;
                        else {
                            long amount = Long.parseLong(sscoPosManager.getInsertedCode());
                            if (amount > itm.amt) res = MNEMO_ERROR_BASE + 3;
                            else itm.amt = amount;
                        }
                    }
                }
                if (res == 0) {
                    res = plugin.redemptionGiftCard(itm, tra);
                }
            }
        }
        logger.debug("Exit. itm.amt: " + itm.amt + " res: " + res);
        return new RedemptionItem(itm.amt, res);
    }

    public int reconciliationGiftCard() {
        GiftCardPluginInterface plugin = getEnabledPlugin();
        if (plugin != null) return plugin.reconciliationGiftCard();
        return 0;
    }

    public int confirmTransaction(Itemdata itm) {
        return 0;
    }

    public int cancelTransaction(Itemdata itm, String transactionType) {
        return 0;
    }

    public int cancelTransaction(Itemdata itm) {
        return 0;
    }

    public int cancelGiftCard(Itemdata itm) {
        return getPlugin(itm.gCard + "").cancelGiftCard(itm);
    }

    public int cancelAll() {
        GiftCardPluginInterface plugin = getEnabledPlugin();
        if (plugin != null) return plugin.cancelAll();
        return 0;
    }

    public boolean isGiftCard(Itemdata itm) {
        if (!isEnabled()) return false;
        GiftCardPluginInterface plugin = getPlugin(itm.gCard + "");
        return plugin != null && plugin.isGiftCard(itm);
    }

    public boolean isEnabled() {
        return plugins != null && getEnabledPlugin() != null;
    }

    public int cancelRedemption(Itemdata itm) {
        return 0;
    }

    public int readSerial32(Itemdata plu) {
        return getPlugin(plu.gCard + "").readSerial32(plu);
    }

    public CustomTender getCustomTender(int tenderId) {
        CustomTender customTender = null;
        GiftCardPluginInterface plugin = getEnabledPlugin();
        if (plugin != null) {
            customTender = plugin.getCustomTender(tenderId);
        }
        return customTender;
    }

    public int getCustomTenderByGCSerialNumber(String gCSerialNumber) {
        GiftCardPluginInterface plugin = getEnabledPlugin();
        if (plugin != null) {
            return plugin.getCustomerTenderByGCSerialNumber(gCSerialNumber);
        }
        return 0;
    }

    public void addPayment(Itemdata itm) {
        GiftCardPluginInterface plugin = getEnabledPlugin();
        if (plugin != null) {
            plugin.addPayment(itm);
        }
    }

    public int confirmAllGiftCard() {
        GiftCardPluginInterface plugin = getEnabledPlugin();
        if (plugin != null) return plugin.confirmAllGiftCard();
        return 0;
    }

    public GiftCardPluginInterface getEnabledPlugin() {
        for (GiftCardPluginInterface plugin : plugins.values()) {
            if (plugin.isEnabled()) return plugin;
        }
        return null;
    }

    public String getTenderId() {
        return null;
    }

    public void resetAll() {
        GiftCardPluginInterface plugin = getEnabledPlugin();
        if (plugin != null) plugin.resetAll();
    }

    public boolean isAnyPluginEnabled() {
        return !plugins.isEmpty();
    }

    public int action1(int spec) {
        logger.info("call Reconciliation");
        prtTitle(120);
        dspLine.init(Mnemo.getText(81)).show(1);  //please wait
        int ret = reconciliationGiftCard();
        dspLine.init("").show(1);

        if (ret == 0) {
            gui.clearLink(Mnemo.getMenu(121), 1); //completed
            Struc.prtLine.init(Mnemo.getMenu(121)).book(3);
        } else {
            gui.clearLink(Mnemo.getInfo(157), 1); //error
            Struc.prtLine.init(Mnemo.getInfo(157)).book(3);
        }

        GdRegis.prt_trailer(2);
        return 0;
    }

    public String mask(String serial) {
        if (serial == null || serial.isEmpty()) {
            return "";
        }
        return serial.substring(0, serial.length() - 3) + "***";
    }

    public void printSummaries(String type) {
        for (GiftCardPluginInterface plugin : plugins.values()) {
            if (plugin.isEnabled()) {
                plugin.printSummaries(type);
            }
        }
    }

    public int cancelPayGiftCard(Itemdata itm, Transact tra) {
        return getPlugin(itm.gCard + "").cancelPayGiftCard(itm, tra);
    }
}