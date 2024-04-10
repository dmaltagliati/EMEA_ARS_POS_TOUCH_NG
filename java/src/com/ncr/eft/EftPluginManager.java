package com.ncr.eft;

import com.ncr.*;
import com.ncr.common.data.AdditionalInfo;
import com.ncr.ecommerce.ECommerce;
import com.ncr.ssco.communication.manager.SscoPosManager;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.util.*;

import static com.ncr.GdTndrs.tnd_wridc;
import static com.ncr.Itmdc.IDC_write;

public class EftPluginManager extends Action {
    private static final Logger logger = Logger.getLogger(EftPluginManager.class);
    public static final String CARD_NUMBER = "CARD_NUMBER";
    public static final String AUTHORIZATION_CODE = "AUTHORIZATION_CODE";
    public static final String TERMINAL_NUMBER = "TERMINAL_NUMBER";

    private static EftPluginManager instance = null;
    private final static String EFT_PLUGINS_PROPERTIES = "conf/eftPlugins.properties";
    private final static String EFT_TENDERS_PROPERTIES = "conf/eftTenders.properties";
    private final static String EFT_PROPERTIES = "conf/eft.properties";
    public final static String EFT_TENDER_IDS = "JKNOMQTUVWXYZ";

    private HashMap<String, Integer> eftTenders = new HashMap<String, Integer>();
    private Properties eftProperties = new Properties();
    private Map<String, EftPlugin> plugins = new HashMap<String, EftPlugin>();

    public static EftPluginManager getInstance() {
        logger.debug("Getting the instance of Plugin Manager");
        if (instance == null)
            instance = new EftPluginManager();

        return instance;
    }

    public void init() {
        try {
            eftProperties.load(new FileInputStream(EFT_PROPERTIES));
        } catch (Exception e) {
            logger.error("Error loading EFT properties", e);
        }
        logger.debug("Initializing Plugin Manager");
        for (EftPlugin eftPlugin : plugins.values()) {
            eftPlugin.init(eftProperties);
        }
    }

    private EftPluginManager() {
    }

    private void loadEftTerminalProperties() {
        String packageName = EftPluginManager.class.getPackage().getName();
        Properties eftPluginProperties = new Properties();

        logger.debug("Loading plugins");

        try {
            eftPluginProperties.load(new FileInputStream(EFT_PLUGINS_PROPERTIES));

            for (Object key : eftPluginProperties.keySet()) {
                String className = (String) key;
                logger.debug("Plugin name: [" + className + "]");
                if (eftPluginProperties.getProperty(className, "false").equals("true")) {
                    Class<EftPlugin> eftClass = (Class<EftPlugin>) Class.forName(packageName + "." + className);
                    EftPlugin eftPlugin = eftClass.newInstance();
                    plugins.put(eftPlugin.getTenderId(), eftPlugin);
                    logger.info("Plugin " + className + " enabled");
                }
            }
        } catch (Exception e) {
            logger.error("Error creating plugins", e);
        }
    }

    public void loadPluginParameters(String key, int line, String txt) {
        logger.info("Loading params: " + key + " line: " + line + " record: " + txt);
        if (plugins.isEmpty()) loadEftTerminalProperties();

        EftPlugin eftPlugin = getPlugin("" + key.charAt(3));
        if (eftPlugin != null) {
            eftPlugin.loadEftTerminalParams(line, txt);
        }
    }

    public boolean isPluginEnabled(String pluginName) {
        return plugins.containsKey(pluginName);
    }

    public EftPlugin getPlugin(String pluginName) {
        return plugins.get(pluginName);
    }

    public void printAdditionalReceiptVouchers() {
        eftPluginManager.printVouchers(EftPlugin.ERR_RECEIPT_TYPE);
        eftPluginManager.printVouchers(EftPlugin.NEW_RECEIPT_TYPE);
    }

    public void printVouchers(String type) {
        for (EftPlugin eftPlugin : plugins.values()) {
            eftPlugin.printVouchers(type);
            if (type == EftPlugin.NEW_RECEIPT_TYPE) {
                eftPlugin.resetVouchers();
            }
        }
    }

    public void writeInfo(String tenderId, Transact tra, Itemdata itm) {
        if (ECommerce.isResumeInstashop()) return;
        EftPlugin eftPlugin = getPlugin(tenderId);
        if (eftPlugin != null) {
            logger.info("Adding additional info to tender");
            itm.getAdditionalInfos().add(new AdditionalInfo(AUTHORIZATION_CODE, eftPlugin.getAuthorizationCode()));
            itm.getAdditionalInfos().add(new AdditionalInfo(TERMINAL_NUMBER, eftPlugin.getTerminalId()));
            itm.getAdditionalInfos().add(new AdditionalInfo(CARD_NUMBER, eftPlugin.getCardNumber()));
            logger.info("Handling idc and receipt for plugin " + eftPlugin.getClass().getSimpleName());
            for (int index = 0; index < eftPlugin.FLAGS_SIZE; index++) {
                if (eftPlugin.isIdcEnabled(index)) {
                    writeIdc(eftPlugin, index, tra, itm);
                }
                if (eftPlugin.isReceiptEnabled(index)) {
                    writeReceipt(eftPlugin, index);
                }
            }
        }
    }

    private void writeReceipt(EftPlugin eftPlugin, int index) {
        logger.info("Writing receipt for plugin" + eftPlugin.getClass().getSimpleName() + " index: " + index);
        switch (index) {
            case EftPlugin.CARD_NUM_INDEX:
                prtLine.init(Mnemo.getText(31)).onto(20, eftPlugin.getCardNumber()).book(3);
                break;
            case EftPlugin.TERMIINAL_ID_INDEX:
                prtLine.init(Mnemo.getText(108)).onto(20, eftPlugin.getTerminalId()).book(3);
                break;
            case EftPlugin.AUTH_CODE_INDEX:
                prtLine.init(Mnemo.getText(80)).onto(20, eftPlugin.getAuthorizationCode()).book(3);
                break;
            case EftPlugin.TRANSACTION_ID_INDEX:
                break;
            default:
                break;
        }
    }

    private void writeIdc(EftPlugin eftPlugin, int index, Transact tra, Itemdata itm) {
        logger.info("Writing idc for plugin" + eftPlugin.getClass().getSimpleName() + " index: " + index);
        switch (index) {
            case EftPlugin.CARD_NUM_INDEX:
                Itmdc.IDC_write('N', tra.tnd, 1, eftPlugin.getCardNumber(), itm.cnt, itm.pos);
                break;
            case EftPlugin.TERMIINAL_ID_INDEX:
                Itmdc.IDC_write('N', tra.tnd, 4, eftPlugin.getTerminalId(), itm.cnt, itm.pos);
                break;
            case EftPlugin.AUTH_CODE_INDEX:
                Itmdc.IDC_write('N', tra.tnd, 6, eftPlugin.getAuthorizationCode(), itm.cnt, itm.pos);
                break;
            case EftPlugin.TRANSACTION_ID_INDEX:
                Itmdc.IDC_write('N', tra.tnd, 5, eftPlugin.getReceiptNumber(), itm.cnt, itm.pos);
                break;
            default:
                break;
        }
    }

    public int pay(String tenderId, Itemdata itm, Terminal ctl, LinIo oplLine) {
        int sts = 0;

        if (EFT_TENDER_IDS.contains(tenderId)) {
            EftPlugin eftPlugin = getPlugin(tenderId);
            if (eftPlugin != null) {
                logger.info("Handling transaction for plugin " + eftPlugin.getClass().getSimpleName() + " amount: " + itm.pos);

                oplLine.init(Mnemo.getInfo(74)).show(2);
                sts = eftPlugin.pay(itm, ctl, oplLine);

                if (sts == EftPlugin.ERR_OK) {
                    itm.tnd = getEftTender(eftPlugin.getCardType(), itm.tnd);
                }
            } else {
                logger.warn("Plugin not enabled for tender " + tenderId);
            }
        }

        logger.info("Exit, returning: " + sts + " tender: " + itm.tnd + " number: " + itm.number);
        return sts;
    }

    public void settle() {
        logger.debug("Enter");
        for (EftPlugin eftPlugin : plugins.values()) {
            if (eftPlugin.isSettlementCapable() && eftPlugin.isSettlementEnabledAtEod()) {
                logger.info("Doing settlement for " + eftPlugin);
                boolean sts = eftPlugin.settle();
                logger.info("Result: " + sts);
            }
        }
        logger.debug("Exit");
    }

    public void stop(Terminal ctl) {
        for (EftPlugin eftPlugin : plugins.values()) {
            eftPlugin.stop(ctl);
        }
    }

    public void readExtendedTender() {
        logger.debug("Enter");
        Properties eftTendersProps = new Properties();
        try {
            eftTendersProps.load(new FileInputStream(EFT_TENDERS_PROPERTIES));
            for (String s : eftTendersProps.stringPropertyNames()) {
                String name = eftTendersProps.getProperty(s);
                int tnd = Integer.parseInt(s.split("\\.")[1]);
                if (name.contains(";")) {
                    String[] tokens = name.split(";");
                    for (String token : tokens) {
                        token = token.trim().toUpperCase();
                        eftTenders.put(token, tnd);
                        logger.info("param " + s + " : tender = " + tnd + ", value = " + token);
                    }
                } else {
                    eftTenders.put(name, tnd);
                    logger.info("param " + s + " : tender = " + tnd + ", value = " + name);
                }
            }
            logger.debug("test getExtTenderNumber(\"AMZ-TEST-23\") = " + getEftTender("AMZ-TEST-23", 100));
            logger.debug("test getExtTenderNumber(\"xxx\") = " + getEftTender("xxx", 100));
        } catch (Exception e) {
            logger.error("Exception: ", e);
        }
        logger.debug("Exit");
    }

    public int getEftTender(String tenderName, int defaultValue) {
        return getEftTender(tenderName, null, defaultValue);
    }

    public int getEftTender(String tenderName, String cardNumber, int defaultValue) {
        logger.debug("Enter");
        Integer tnd;
        if (tenderName != null) {
            tenderName = tenderName.trim().toUpperCase();
            tnd = eftTenders.get(tenderName);
            if (tnd == null) {
                logger.warn("Cannot find tender name = " + tenderName);
                if (cardNumber != null) {
                    for (String pattern : eftTenders.keySet()) {
                        if (pattern.length() > 0) {
                            if (cardNumber.matches(pattern)) return eftTenders.get(pattern);
                        }
                    }
                }
                logger.warn("Returning default tender = " + defaultValue);
                tnd = defaultValue;
            }
        } else {
            logger.warn("Cannot find tender name = null");
            logger.warn("Returning default tender: " + defaultValue);
            tnd = defaultValue;
        }
        logger.debug("Exit, tender returned: " + tnd);
        return tnd;
    }

    public int action0(int spec) {
        logger.info("Enter Marshall action0, spec: " + spec);
        int sts = 0;

        dspLine.init(Mnemo.getMenu(102));

        if ((sts = sc_checks(2, 6)) > 0)
            return sts;
        if (spec == 0) {
            if(input.key == input.CLEAR)
                dspLine.init(Mnemo.getInfo(53));

            return 0;
        }

        Set<String> keys = plugins.keySet();
        if (!keys.isEmpty()) {
            Iterator<Map.Entry<String, EftPlugin>> iterator = plugins.entrySet().iterator();
            Map.Entry<String, EftPlugin> actualValue = iterator.next();
            EftPlugin eftPlugin = actualValue.getValue();
            if (spec == 0) {
                logger.debug("Performing ECR void");
                if(input.key == input.CLEAR)
                    dspLine.init(Mnemo.getInfo(53));

                if (!eftPlugin.isAmountRequiredOnVoid())
                    event.nxt = event.alt;
                return 0;
            } else if (spec == 2) {
                logger.debug("After amount inserted");
                if (eftPlugin.isDateRequiredOnVoid())
                    event.nxt = event.alt;
            }

            sts = eftPlugin.voidPayment(spec, input.pb);
            logger.info("Void invoked, sts: " + sts);

            if (spec == 3 && sts == 0) {
                tra.mode = 0;
                tra.code = 31;

                if (!tra.isActive()) {
                    logger.info("call set_tra_top");
                    GdRegis.set_tra_top();
                }

                prtTitle(102);

                logger.debug("Original itm.tnd: " + itm.tnd);
                itm.tnd = eftPluginManager.getEftTender(eftPlugin.getCardType(), itm.tnd);
                logger.debug("Remapped itm.tnd: " + itm.tnd);

                tra.amt = -eftPlugin.getAuthorizedAmount();
                logger.info("ECR amount: " + tra.amt);

                IDC_write('H', trx_pres(), tra.spf3, tra.number, tra.cnt, tra.rate);
                IDC_write('z', tra.tnd, 1, "000", 1, tra.amt);
                tnd_wridc('T', tra.tnd, 0, 1, tra.amt);

                GdTrans.tra_finish();
            }
        } else {
            sts = 8;
        }
        return sts;
    }

    public int action1(int spec) {
        logger.debug("Enter, spec: " + spec);
        boolean settlementSucceeded = false;
        boolean atLeastOnePlugin = false;

        for (EftPlugin eftPlugin : plugins.values()) {
            if (eftPlugin.isSettlementCapable()) {
                logger.info("Doing settlement for " + eftPlugin);
                prtTitle(105);

                dspLine.init(Mnemo.getText(81)).show(1);

                boolean sts = eftPlugin.settle();
                dspLine.init("").show(1);

                if (sts) {
                    gui.clearLink(Mnemo.getMenu(106), 1); //completed
                    Struc.prtLine.init(Mnemo.getMenu(106)).book(3);
                } else {
                    gui.clearLink(Mnemo.getInfo(134), 1); //error
                    Struc.prtLine.init(Mnemo.getInfo(134)).book(3);
                }
                settlementSucceeded = settlementSucceeded | sts;
                atLeastOnePlugin = true;
                GdRegis.prt_trailer(2);
            }
        }

        if (SscoPosManager.getInstance().isEnabled() && atLeastOnePlugin) {
            SscoPosManager.getInstance().eftSettleResponse(settlementSucceeded);
        }
        logger.debug("Exit");
        return 0;
    }
}