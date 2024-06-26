package com.ncr;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.*;

import com.ncr.common.utilities.ReferenceManager;
import com.ncr.giftcard.GiftCardPlugin;
import com.ncr.giftcard.GiftCardPluginInterface;
import com.ncr.gui.ModDlg;
import com.ncr.giftcard.psh.data.CustomTender;
import com.ncr.ssco.communication.entities.DataNeeded;
import com.ncr.ssco.communication.entities.pos.SscoCustomer;
import com.ncr.ssco.communication.manager.SscoPosManager;
import com.ncr.ssco.communication.requestprocessors.LoyaltyRequestProcessor;
import com.ncr.ssco.communication.requestprocessors.ProcessorConstants;
import com.ncr.struc.Customer;
import com.philoshopic.smash.ARSBAPI.LogController;
import com.philoshopic.smash.ARSBAPI.PrepayXLController;
import com.philoshopic.smash.ARSBAPI.SmashController;
import org.apache.log4j.Logger;

public class GdPsh extends GiftCardPlugin {
    private static final Logger logger = Logger.getLogger(GdPsh.class);
    private static GdPsh instance = null;
    public final static int OK = 0;
    public final static int MNEMO_ERROR_BASE = 86;
    public final static int MNEMO_MNEMO_BASE = 89;
    public final static int GIFT_EMPTY = 103;
    public final static int MNEMO_DIAGS_BASE = 20;
    private final static int SYNCERROR_BUY = 0;
    private final static int SYNCERROR_TOPUP = 1;
    private final static int SYNCERROR_PAY = 2;
    private final static int SYNCERROR_POINTS = 3;
    private final static int PSH_API_V__1_4_CUSTOMER_REPLY_LENGTH = 5;
    private final static int PSH_API_V_2_1_CUSTOMER_REPLY_LENGTH = 8;
    private final static int PSH_API_V_2_1_B_CUSTOMER_REPLY_LENGTH = 9;
    private final static int PSH_API_V_2_2_CUSTOMER_REPLY_LENGTH = 10;
    private final static String TLOG_OK = "0;OK";
    public final static String NEW_RECEIPT_TYPE = "new-receipt";
    public final static String SAME_RECEIPT_TYPE = "same-receipt";

    public static boolean readingGCSerial = false;
    private final static ArrayList<Itemdata> soldGiftcards = new ArrayList<Itemdata>();
    private final static ArrayList<Itemdata> topups = new ArrayList<Itemdata>();
    private final static ArrayList<Itemdata> payments = new ArrayList<Itemdata>();
    private final static ArrayList<Itemdata> utilities = new ArrayList<Itemdata>();  //PSH-ENH-20151120-CGA#A
    private final static ArrayList<Itemdata> prizes = new ArrayList<Itemdata>();
    private final static Properties prop = new Properties();
    private final static String PROP_FILENAME = "conf/philoshopic.properties";
    private final static String ACCOUNT_NUMBER_REQUIRED = "accountNumberRequired";
    private static boolean enabled = false;
    private static boolean topUpOnReturnEnabled = false;
    private static String par_httpPrimaryServer = "";
    private static String par_httpSecondaryServer = "";
    private static String par_storeid = "";
    private static String par_secret = "";

    //DMA-TLOG_UPLOADING#A BEG
    private static String par_httpPrimaryServerTlog = "";
    private static String par_httpSecondaryServerTlog = "";
    private static boolean tlogSyncingEnabled = false;
    private static boolean customerUpdateEnabled = false;
    //DMA-TLOG_UPLOADING#A END

    //PSH-ENH-20151120-CGA#A BEG
    private static String checkAmountGiftCard = "";
    private static String par_httpPrimaryServerUtility = "";
    private static String par_httpSecondaryServerUtility = "";
    private static String par_storeidUtility = "";
    private static String par_secretUtility = "";
    //PSH-ENH-20151120-CGA#A END
    private static boolean printEnabled = false;
    private static boolean printSerialEnabled = false;
    private static boolean isEnableUtilityWithoutPr = false;   //UTILITY-WITHOUTPR-CGA#A
    private static boolean keyboardEnabled = true;
    private static String prefix = "";
    private static String prefixToEan = "";
    private static final String GIFTCARD = "GIFTCARD";
    private static final String LOYALTY = "LOYALTY";
    private static final String SMASH_ACCOUNT = "ACCOUNT_NUMBER";
    private static final String MOBILE_NUMBER = "MOBILE_NUMBER";
    private static final String CREDIT = "CREDIT";
    private static final String DEBIT = "DEBIT";
    private static final String REPLY_OK_VALUE = "0";
    private static final String REPLY_NOT_FOUND_VALUE = "64";
    private static final String REQUEST_MOBILE = "-1";
    private static final String REQUEST_CARDDIG = "-2";
    private static final String REQUEST_CPIN = "-3";
    private static final String REQUEST_SMSPIN = "-4";
    private static final int RETRIES = 5;
    private static SmashController controller;
    private static LogController logController; //DMA-TLOG_UPLOADING#A
    private static PrepayXLController ppController;  //PSH-ENH-20151120-CGA#A
    private static String lastTransactionID = "";
    private static final HashMap<Integer, String> messages = new HashMap<Integer, String>();
    private static String uniqueTransactionId = ""; // AMZ-2017-003#ADD
    private static boolean pxlEnabled = false;  // AMZ-2017-003-004#ADD
    private static boolean smashEnabled = false; // AMZ-2017-003-004#ADD
    private static boolean barcodeEnabled = false; // AMZ-2017-003-004#ADD
    private static boolean priceAskDisabled = false; // AMZ-2017-003-004#ADD
    private static boolean newBarcode = false;
    private final static int PTR_BCS_Code128_Parsed = 123;
    private final static int PTR_BCS_Code128 = 110;
    private final static String PAR_NEW_BARCODE = "newBarcode";
    private final static String PROP_PXL_FILENAME = "conf/prepayXL.properties"; // AMZ-2017-003-004#ADD
    private final static String MASK_START_PV = "PV";//PSH-ENH-20211215-CGA#A
    private static final String PAR_SELF_SELL = "printSelfSellItemEnabled";//PSH-ENH-20211215-CGA#A
    private static final String SELF_SELL_ALL = "all";//PSH-ENH-20211215-CGA#A
    private static final String tenderMaskProps = "accountType.";
    private static final ArrayList<CustomTender> accountList = new ArrayList<CustomTender>();

    private static final String ENABLE_LOYALTY_ID = "loyalty-id.enable";
    private static final String LOYALTY_PATTERN = "loyalty.pattern";
    private static final String BRANCH_PROP = "branch.code";

    public static GdPsh getInstance() {
        if (instance == null)
            instance = new GdPsh();
        return instance;
    }

    private GdPsh() {
    }

    public void setUTID() {
        /*
        A unique POS transaction ID will be generated having the following structure:
        SSSSRRRTTTTYYYYMMDDHHmmss
        Where:
        SSSS – is the store number
        RRR – is the register number (terminal)
        TTTT – is the transaction number
        YYYYMMDDHHmmss – is the date and time of the transaction
        This code will be used for Philoshopic communication and printed at the end of receipt as a
        Code128 barcode.
         */
        uniqueTransactionId = //
                "20" + ctl.date + editNum(ctl.sto_nbr, 3) + editKey(ctl.reg_nbr, 3) + editNum(ctl.tran, 4);

        logger.debug("uniqueTransactionId = " + uniqueTransactionId);
    }

    public String getUniqueTransactionId() {
        return uniqueTransactionId;
    }

    public void printBarcode() {
        logger.info("ENTER printBarcode");
        if (!barcodeEnabled) {
            logger.info("EXIT printBarcode() - par_isEnabledBarcode: disabled");
            return;
        }
        if (ctl.uniqueId.length() == 0) {
            logger.info("EXIT printBarcode() - uniqueTransactionId.length(): " + ctl.uniqueId.length());
            return;
        }
        logger.info("EXIT printBarcode() - call DevIo.tpmLabel(2, uniqueTransactionId);");
        DevIo.tpmLabel(2, ctl.uniqueId, isNewBarcode() ? PTR_BCS_Code128_Parsed : PTR_BCS_Code128);
    }

    /**
     * @return true if all Philoshopic functions are enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    public boolean isTopUpOnReturnEnabled() {
        return topUpOnReturnEnabled;
    }

    public boolean isSmashEnabled() {
        return isEnabled() && smashEnabled;
    }

    public boolean isPxlEnabled() {
        return isEnabled() && pxlEnabled;
    }

    static boolean priceAskDisabled() {
        return priceAskDisabled;
    }

    static boolean isNewBarcode() {
        return newBarcode;
    }

    static PrepayXLController getPpController() {
        return ppController;
    }

    public static boolean isEnabledPrintAllGiftItem() {
        logger.debug("printSelfSellItemEnabled = " + prop.getProperty(PAR_SELF_SELL, SELF_SELL_ALL));
        return (prop.getProperty(PAR_SELF_SELL, SELF_SELL_ALL).trim().equalsIgnoreCase(SELF_SELL_ALL));
    }

    public void readPregpar(String txt, int ind) throws Exception {
        if (ind != 0) {
            throw new Exception("Bad PSHP line number in p_regpar, must be PSHP0");
        }
        enabled = (Integer.parseInt(txt.substring(0, 2)) == 1);
        keyboardEnabled = (Integer.parseInt(txt.substring(2, 4)) == 1);
        prefix = txt.substring(4, 12);
        prefixToEan = txt.substring(12, 20);
        checkAmountGiftCard = txt.substring(20, 22); //PSH-ENH-20151120-CGA#A
        printEnabled = txt.charAt(22) == '1';
        printSerialEnabled = txt.charAt(23) == '1';
        isEnableUtilityWithoutPr = txt.charAt(24) == '1';  //UTILITY-WITHOUTPR-CGA#A
        Integer.parseInt(prefix); // Check against errors
        prefixToEan = "" + Integer.parseInt(prefixToEan); // Check against errors & remove zeroes
        readConfig();
        if (enabled) {
            logger.debug("PSHP:Enabled");
            logger.debug("PSHP:Primary Server endpoint " + par_httpPrimaryServer);
            logger.debug("PSHP:Secondary Server endpoint " + par_httpSecondaryServer);
            logger.debug("PSHP:Keyboard " + (keyboardEnabled ? "enabled" : "disabled"));
            logger.debug("PSHP:topup card prefix " + prefix);
            logger.debug("PSHP:topup item map on EAN " + prefixToEan);
            logger.debug("PSHP:Store number " + par_storeid);
            logger.debug("PSHP:Primary Server endpoint utility " + par_httpPrimaryServerUtility);
            logger.debug("PSHP:Secondary Server endpoint utility " + par_httpSecondaryServerUtility);
            logger.debug("PSHP:Primary Server endpoint tlog " + par_httpPrimaryServerTlog);
            logger.debug("PSHP:Secondary Server endpoint tlog " + par_httpSecondaryServerTlog);
            logger.debug("PSHP:Store number utility " + par_storeidUtility);
            logger.debug("PSHP:check Amount Gift card " + checkAmountGiftCard);
            // AMZ-2017-003-004#BEG
            logger.debug("PSHP:SMASH enabled " + smashEnabled);
            logger.debug("PSHP:Barcode printing enabled " + barcodeEnabled);
            logger.debug("PSHP:PXL enabled " + pxlEnabled);
            logger.debug("PSHP:no ask price to cashier " + priceAskDisabled);
            logger.debug("PSHP:utility without properties " + isEnableUtilityWithoutPr);
            // AMZ-2017-003-004#END
        }
        controller = new SmashController(par_httpPrimaryServer, par_httpSecondaryServer, par_storeid, par_secret);
        ppController = new PrepayXLController(par_httpPrimaryServerUtility, par_httpSecondaryServerUtility, par_storeidUtility, par_secretUtility);
        logController = new LogController(par_httpPrimaryServerTlog, par_httpSecondaryServerTlog, par_storeid, par_secret);
        loadMessages();
    }

    /**
     * read from config file philoshopic.properties
     */
    static void readConfig() throws Exception {
        try {
            prop.load(new FileInputStream(PROP_FILENAME));

            par_httpPrimaryServer = prop.getProperty("httpServer.primary.endpoint");
            if (par_httpPrimaryServer == null) {
                throw new Exception("missing or malformed httpServer.primary.endpoint in file " + PROP_FILENAME);
            }

            par_httpSecondaryServer = prop.getProperty("httpServer.secondary.endpoint");
            if (par_httpSecondaryServer == null) {
                throw new Exception("missing or malformed httpServer.secondary.endpoint in file " + PROP_FILENAME);
            }

            par_storeid = prop.getProperty("storeId");
            if (par_storeid == null) {
                throw new Exception("missing or malformed storeId in file " + PROP_FILENAME);
            }

            par_secret = prop.getProperty("secret");
            if (par_secret == null) {
                throw new Exception("missing or malformed httpServer in file " + PROP_FILENAME);
            }

            par_httpPrimaryServerUtility = prop.getProperty("httpServer.primary.endpoint.utility");
            if (par_httpPrimaryServerUtility == null) {
                throw new Exception("missing or malformed httpServer.primary.endpoint.utility in file " + PROP_FILENAME);
            }

            par_httpSecondaryServerUtility = prop.getProperty("httpServer.secondary.endpoint.utility");
            if (par_httpSecondaryServerUtility == null) {
                throw new Exception("missing or malformed httpServer.secondary.endpoint.utility in file " + PROP_FILENAME);
            }

            par_storeidUtility = prop.getProperty("storeId.utility");
            if (par_storeidUtility == null) {
                throw new Exception("missing or malformed storeId.utility in file " + PROP_FILENAME);
            }

            par_secretUtility = prop.getProperty("secret.utility");
            if (par_secretUtility == null) {
                throw new Exception("missing or malformed httpServer.utility in file " + PROP_FILENAME);
            }

            par_httpPrimaryServerTlog = prop.getProperty("httpServer.primary.endpoint.tlog");
            if (par_httpPrimaryServerTlog == null) {
                throw new Exception("missing or malformed httpServer.primary.endpoint.tlog in file " + PROP_FILENAME);
            }

            par_httpSecondaryServerTlog = prop.getProperty("httpServer.secondary.endpoint.tlog");
            if (par_httpPrimaryServerTlog == null) {
                throw new Exception("missing or malformed httpServer.secondary.endpoint.tlog in file " + PROP_FILENAME);
            }
            try {
                tlogSyncingEnabled = Boolean.parseBoolean(prop.getProperty("tlogSyncingEnabled"));
            } catch (Exception e) {
                throw new Exception("missing or malformed tlogSyncingEnabled parameter in file " + PROP_FILENAME);
            }
            try {
                customerUpdateEnabled = Boolean.parseBoolean(prop.getProperty("customerUpdateEnabled"));
            } catch (Exception e) {
                throw new Exception("missing or malformed customerUpdateEnabled parameter in file " + PROP_FILENAME);
            }
            try {
                smashEnabled = Boolean.parseBoolean(prop.getProperty("isEnabled"));
            } catch (Exception e) {
                throw new Exception("missing or malformed isEnabled parameter in file " + PROP_FILENAME);
            }
            try {
                topUpOnReturnEnabled = Boolean.parseBoolean(prop.getProperty("topoup-on-return.enable"));
            } catch (Exception e) {
                throw new Exception("missing or malformed customerUpdateEnabled parameter in file " + PROP_FILENAME);
            }
            try {
                barcodeEnabled = Boolean.parseBoolean(prop.getProperty("barcodeEnabled"));
            } catch (Exception e) {
                throw new Exception("missing or malformed barcodeEnabled parameter in file " + PROP_FILENAME);
            }
            try {
                newBarcode = Boolean.parseBoolean(prop.getProperty(PAR_NEW_BARCODE));
            } catch (Exception e) {
                throw new Exception("missing or malformed newBarcode parameter in file " + PROP_FILENAME);
            }

            readAccountNumberList();

            try {
                prop.load(new FileInputStream(PROP_PXL_FILENAME));
            } catch (Exception e) {
                if (isEnableUtilityWithoutPr()) {
                    return;
                }
                throw new Exception("missing or malformed noAskPrice parameter in file " + PROP_PXL_FILENAME);
            }

            try {
                pxlEnabled = Boolean.parseBoolean(prop.getProperty("isEnabled", "false"));
            } catch (Exception e) {
                throw new Exception("missing or malformed isEnabled parameter in file " + PROP_PXL_FILENAME);
            }
            try {
                priceAskDisabled = Boolean.parseBoolean(prop.getProperty("noAskPrice", "false"));
            } catch (Exception e) {
                throw new Exception("missing or malformed noAskPrice parameter in file " + PROP_PXL_FILENAME);
            }
        } catch (final Exception e) {
            throw new Exception("malformed or missing file " + PROP_FILENAME + ", " + e.getMessage());
        }
    }

    private static void readAccountNumberList() {
        Set<Object> listKeys = prop.keySet();
        for (Object key : listKeys) {
            if (key.toString().startsWith(tenderMaskProps)) {
                String tender = prop.getProperty(key.toString());
                String id = key.toString().substring(tenderMaskProps.length());
                String[] str = tender.split(";");
                accountList.add(new CustomTender(Integer.parseInt(id), str[0], Boolean.parseBoolean(str[1])));
            }
        }
    }

    public static boolean isEnableUtilityWithoutPr() {
        return isEnableUtilityWithoutPr;
    }

    /**
     * Info Giftcard topup
     */
    @Override
    public int action0(int spec) {
        if (!enabled) return 7; // unavailable

        Itemdata itm = new Itemdata();
        itm.accountType = GIFTCARD;

        int res = readSerial32(itm);
        if (res == -1) return OK;
        if (res > 0) return res;

        res = pshTotalGiftCardBalance(itm);
        if (res > 0) return res;

        GdPos.panel.clearLink(Mnemo.getDiag(MNEMO_DIAGS_BASE + 2).trim() + " " + editMoney(0, itm.amt), 0x81);
        return OK;
    }

    /**
     * Info Loyalty points
     */
    @Override
    public int action1(int spec) {
        logger.debug("Enter");

        if (!isSmashEnabled()) {
            logger.debug("SMASH controller disabled. Exiting");
            return 7;
        }

        if (cus.getNumber() == null || cus.getNumber().trim().isEmpty()) {
            logger.debug("Customer number is null or empty. Exiting");
            return 7;
        }
        logger.info("Customer number: " + cus.getNumber());

        int res = pshCustomerPointsBalance(cus);
        if (res > 0) {
            logger.debug("Exit with error: " + res);
            return res;
        }

        GdPos.panel.clearLink(Mnemo.getDiag(MNEMO_DIAGS_BASE + 3).trim() + " " + cus.getPnt(), 0x81);
        logger.debug("Exit. Ok");
        return OK;
    }

    public static int readCodePrepay(String descr) {
        logger.debug("ENTER readCodePrepay");
        logger.debug("Searching item prepayXL: " + descr.trim());

        try {
            String[] prc = ppController.getBarcode(descr.trim()).split(";");

            if ("0".equals(prc[0])) {
                itm.utilityCode = prc[1];
                itm.utilityName = descr.trim();
                try {
                    itm.utilityMaxPrice = Integer.parseInt(prc[2]);
                } catch (Exception e) {
                    itm.utilityMaxPrice = 0;
                    logger.error("Error, setting utilityMaxPrice to 0: " + e);
                }

                logger.info("Server getBarcode : itm.utilityCode = " + itm.utilityCode);
                logger.info("Server getBarcode : itm.utilityMaxPrice = " + itm.utilityMaxPrice);
                if (itm.utilityCode.length() > 0) {
                    logger.debug("EXIT readCodePrepay");
                    return OK;
                }
            } else {
                logger.debug("Server getBarcode : ERROR: " + prc[0]);
            }
        } catch (Exception e) {
            logger.error("Error: " + e);
        }
        logger.info("Server failure : Searching barcode in local properties file");

        Properties prepayXl = new Properties();
        String description = "";

        itm.utilityName = descr.trim();

        try {
            prepayXl.load(new FileInputStream("conf/prepayXL.properties"));

            if (prepayXl.containsValue(descr.trim())) {
                logger.debug("value found in the properties file");

                Set<Object> listKeys = prepayXl.keySet();
                for (Object key : listKeys) {
                    description = prepayXl.getProperty((String) key).trim();
                    logger.info("value: " + description);

                    if (description.equals(descr.trim())) {
                        logger.info("key found");
                        logger.debug("EXIT readCodePrepay - return: " + key);

                        String[] fields = ((String) key).split("\\.");
                        itm.utilityCode = fields[1];

                        if (descr.startsWith("dir") && (prepayXl.getProperty(fields[0] + "." + fields[1] + ".max")) != null) {
                            itm.utilityMaxPrice = Integer.parseInt(prepayXl.getProperty(fields[0] + "." + fields[1] + ".max").trim());
                        }

                        break;
                    }
                }
            } else {
                logger.info("value not found in the properties file, read default value");
                itm.utilityCode = prepayXl.getProperty("default.code").trim();

                if (descr.startsWith("dir")) {
                    itm.utilityMaxPrice = Integer.parseInt(prepayXl.getProperty("default.max").trim());
                }
            }
        } catch (Exception e) {
            logger.error("Error: " + e);
            return 7;
        }

        logger.debug("EXIT readCodePrepay - ean not found");
        return OK;
    }

    static int insertMobile() {
        logger.debug("Enter");

        ConIo newInput = new ConIo(20);
        for (; ; ) {
            ModDlg dlg = new ModDlg(Mnemo.getText(85));
            dlg.block = false;
            dlg.input = newInput;

            ConIo mtio = Motor.input;
            Motor.input = dlg.input;

            dlg.input.prompt = Mnemo.getText(15);
            newInput.init(0x00, 20, 0, 0);

            oplToggle(2, Mnemo.getText(15));
            dlg.show("PSH");

            input.reset("");
            Motor.input = mtio;
            oplToggle(0, null);

            if (dlg.code != 0) {
                return 2;
            }
            if (dlg.input.key == ConIo.CLEAR) {
                logger.debug("Exit. Abort operation without user message panel");

                return -1;
            }
            if (dlg.input.num < 1) {
                continue;
            }
            if (dlg.input.key == ConIo.ENTER) {
                if (keyboardEnabled) {
                    break;
                }
                logger.debug("Exit. Utility server error");

                return MNEMO_ERROR_BASE + 13;
            }
            if (dlg.input.key == 0x4d4d) {
                break;
            }
            if (dlg.input.key == 0x4f4f) {
                break;
            }
        }

        cus.setMobile(newInput.pb);

        logger.info("mobile: " + cus.getMobile());
        logger.debug("Exit. Ok");
        return OK;
    }

    public int readSerial32(Itemdata plu) {
        if (!plu.giftCardSerial.isEmpty()) {
            return OK;
        }
        if (SscoPosManager.getInstance().isEnabled()) {
            String dataNeededType = plu.accountType != null && !plu.accountType.isEmpty() ? "InsertSerialNumber." + plu.accountType : "InsertSerialNumber";

            SscoPosManager.getInstance().sendDataNeeded(dataNeededType);
            SscoPosManager.getInstance().waitForDataneededClose();
            String insertedCode = SscoPosManager.getInstance().getInsertedCode();
            logger.debug("Inserted code: " + insertedCode);
            if (insertedCode == null || insertedCode.isEmpty()) return 7;
            plu.giftCardSerial = insertedCode;
        } else {
            ConIo newInput = new ConIo(20);
            for (; ; ) {
                ModDlg dlg = new ModDlg(Mnemo.getDiag(MNEMO_DIAGS_BASE));
                dlg.block = false;
                dlg.input = newInput;
                ConIo mtio = Motor.input;
                Motor.input = dlg.input;
                dlg.input.prompt = Mnemo.getText(MNEMO_MNEMO_BASE);
                newInput.init(0x10, 255, 0, 0);
                oplToggle(2, Mnemo.getText(MNEMO_MNEMO_BASE));
                readingGCSerial = true;
                dlg.show("PSH");
                readingGCSerial = false;
                input.reset("");
                Motor.input = mtio;
                oplToggle(0, null);
                if (dlg.code != 0) {
                    return 2;
                }
                if (dlg.input.key == ConIo.CLEAR) {
                    return -1; // abort operation without user message panel
                }
                if (dlg.input.num < 1) {
                    continue;
                }
                if (dlg.input.key == ConIo.ENTER) {
                    if (keyboardEnabled) {
                        break;
                    }
                    return MNEMO_ERROR_BASE + 4;
                }
                if (dlg.input.key == 0x4d4d) {
                    break;
                }
                if (dlg.input.key == 0x4f4f) {
                    break;
                }
            }
            plu.giftCardSerial = newInput.pb;
        }
        return OK;
    }

    static String readAdditionalInfo(String dialogText, String type) {
        if (SscoPosManager.getInstance().isEnabled()) {
            SscoPosManager.getInstance().sendDataNeeded("InsertAdditionalInfo" + type);
            SscoPosManager.getInstance().waitForDataneededClose();
            String insertedCode = SscoPosManager.getInstance().getInsertedCode();
            logger.debug("Inserted code: " + insertedCode);
            return insertedCode;
        } else {
            for (; ; ) {
                ModDlg dlg = new ModDlg(dialogText);
                dlg.block = false;
                dlg.input.prompt = Mnemo.getText(MNEMO_MNEMO_BASE + 1);
                oplToggle(2, Mnemo.getText(MNEMO_MNEMO_BASE + 1));
                dlg.input.reset("");
                dlg.input.init(0, 30, 1, 0);
                dlg.show("PSH");
                oplToggle(0, null);
                if (dlg.code != 0) {
                    return null;
                }
                if (dlg.input.key == ConIo.CLEAR) {
                    return null; // abort operation
                }
                if (dlg.input.num < 1) {
                    continue;
                }
                if (dlg.input.key == ConIo.ENTER) {
                    return dlg.input.pb;
                }
                if (dlg.input.key == 0x4d4d) {
                    return dlg.input.pb;
                }
                if (dlg.input.key == 0x4f4f) {
                    return dlg.input.pb;
                }
            }
        }
    }

    public int pointsRedemption(Itemdata itm) {
        if (!isSmashEnabled()) {
            logger.debug("Smash controller disabled. exiting");
            return 7;
        }

        int ret = pshPointsRedemption(itm);
        if (ret == 0) {
            prizes.add(itm.copy());
        }

        return ret;
    }

    public void setCashierId(int posId, int cashierId) {
        if (!enabled) return;

        logger.info("Smash Controller: call function setCashierId [" + posId + "] [" + cashierId + "]");
        controller.setCashierID(String.valueOf(posId), String.valueOf(cashierId));

        if (isPxlEnabled()) {
            logger.info("PrepayXL Controller: call function setCashierId [" + posId + "] [" + cashierId + "]");
            ppController.setCashierID(String.valueOf(posId), String.valueOf(cashierId));
        }
    }

    @Override
    public int redemptionGiftCard(Itemdata itm, Transact tra) {
        int res = 0;

        if (!isEnabled()) {
            return 7; // unavailable
        }
        if (tra.amt < 0) {
            if (!isTopUpOnReturnEnabled()) {
                return 7;
            }
            if (itm.amt - itm.com < tra.bal) {
                return MNEMO_ERROR_BASE + 3;
            }
            if (tra.getReferenceNumber().isEmpty()) {
                if ((res = ReferenceManager.getInstance().insertReferenceNumber()) >= 0) return res;
                if ((res = ReferenceManager.getInstance().insertReferenceDate()) >= 0) return res;
            }
        } else {
            if (itm.amt - itm.com > tra.bal) {
                return MNEMO_ERROR_BASE + 3;
            }
        }

        if (itm.accountNumberRequired) {
            String[] tenderTypes = cus.getAllowedTenderTypes().split(",");
            List<String> tenderTypesList = Arrays.asList(tenderTypes);
            if (cus.getNumber() != null && !cus.getNumber().isEmpty()
                    && tenderTypesList.contains(itm.accountType)
                    && SMASH_ACCOUNT.equals(cus.getAccountInputType())) {
                itm.giftCardSerial = cus.getNumber();
            }
            res = readSerial32(itm);
        }

        if (res > 0) return res;
        if (res < 0) return 5;

        List<String> items = buildItemsList(itm.accountType);
        res = pshPayGiftCard(itm, items);
        if (res > 0) return res;

        itm.gCardPayment = true;
        tra.gctnd += itm.amt;
        payments.add(itm.copy());

        return OK;
    }

    private List<String> buildItemsList(String accountType) {
        List<String> items = new ArrayList<String>();
        for (int ind = 0; ind < tra.vItems.size(); ind++) {
            itm = tra.vItems.getElement(ind);
            if (itm.id == 'S') {
                long gross = itm.amt + itm.crd + itm.dsc;
                long tax = roundBy(gross * vat[itm.vat].rate * 10 / (1000 + vat[itm.vat].rate), 10);
                String item = itm.number + ";" + itm.ptyp + ";" + itm.text + ";" +
                        itm.cnt + ";" + moneyForServer(accountType, itm.price) + ";" + moneyForServer(accountType, -itm.crd - itm.dsc) + ";" +
                        moneyForServer(accountType, tax) + ";" + moneyForServer(accountType, gross);
                items.add(item);
            }
        }
        return items;
    }

    public int cancelPayGiftCard(Itemdata itm, Transact tra) {
        if (!enabled) return 7;

        for (Itemdata payment : payments) {
            if (payment.giftCardSerial.trim().compareTo(itm.giftCardSerial.trim()) != 0)
                continue;
            if (payment.number.trim().compareTo(itm.number.trim()) != 0)
                continue;
            if (payment.amt != itm.amt)
                continue;
            int ret = cancelPayGiftCard(payment);
            if (ret == 0) {
                payments.remove(payment);
                itm.utilityEnglishText = payment.utilityEnglishText;
                itm.gCardDsc = payment.gCardDsc;
                return OK;
            }
            return ret;
        }
        return OK;
    }

    public int cancelRedemption(Itemdata itm) {
        if (!isSmashEnabled()) {
            logger.debug("Smash controller disabled. exiting");
            return 7;
        }

        for (Itemdata prize : prizes) {
            if (prize.prpnt != itm.prpnt) continue;
            if (!prize.number.trim().equals(itm.number.trim())) continue;
            if (prize.amt != itm.amt) continue;
            
            int ret = pshCancelRedemption(prize);
            if (ret == OK) {
                prizes.remove(prize);
                itm.redemptionDsc = prize.redemptionDsc;
            }
            return ret;
        }
        return OK;
    }

    private static int pshPayGiftCard(Itemdata itm, List<String> items) {
        if (SscoPosManager.getInstance().isEnabled()) {
            SscoPosManager.getInstance().sendDataNeeded("PleaseWait");
        }

        String[] ret;
        if (tra.amt < 0) {
            ret = pshDoTransaction(CREDIT, itm.accountType, itm.giftCardSerial, -itm.amt, itm, items, tra.getReferenceNumber() + "," + tra.getReferenceDate() + ",");
        } else {
            ret = pshDoTransaction(DEBIT, itm.accountType, itm.giftCardSerial, itm.amt, itm, items, "");
        }
        int result = 0;
        if (!REPLY_OK_VALUE.equals(ret[0])) {
            if (ret[0].equals(String.valueOf(GIFT_EMPTY))) {
                result = GIFT_EMPTY;
            } else {
                result = MNEMO_ERROR_BASE;
            }
        } else if (itm.amt == 0) {
            result = GIFT_EMPTY;
        } else {
            setGiftCardData(itm, ret);
        }
        return result;
    }

    public void printText(String text) {
        logger.debug("Enter");
        int pos = 0;

        String[] lines = text.split("<br />");

        for (String line : lines) {
            if (line.contains("<h") || line.contains("</h")) {
                String[] rowBold = line.split("<h");

                int x = 0;
                if (rowBold[0].length() > 0 && !rowBold[0].contains("</h")) {
                    prtLine.init(rowBold[0]).book(3);
                    x = 1;
                } else if (rowBold[0].length() == 0) {
                    x = 1;
                }

                for (; x < rowBold.length; x++) {
                    if ((pos = rowBold[x].indexOf("</h")) >= 0) {
                        if (rowBold[x].charAt(1) == '>') {
                            text = rowBold[x].substring(2, pos);
                        } else {
                            text = rowBold[x].substring(0, pos);
                        }

                        prtDwide(3, text);

                        if (pos + 5 < rowBold[x].length()) {
                            prtLine.init(rowBold[x].substring(pos + 5)).book(3);
                        }
                    } else {
                        text = rowBold[x].substring(2);
                        prtDwide(3, text);
                    }
                }
            } else if (line.contains("<b>")) {
                String[] rowBold = line.split("<b>");

                int x = 0;
                if (rowBold[0].length() > 0 && !rowBold[0].contains("</b>")) {
                    prtLine.init(rowBold[0]).book(3);
                    x = 1;
                } else if (rowBold[0].length() == 0) {
                    x = 1;
                }

                for (; x < rowBold.length; x++) {
                    if ((pos = rowBold[x].indexOf("</b>")) >= 0) {
                        text = rowBold[x].substring(0, pos);
                        prtDwide(3, text);

                        if (pos + 5 < rowBold[x].length()) {
                            prtLine.init(rowBold[x].substring(pos + 5)).book(3);
                        }
                    } else {
                        text = rowBold[x];
                        prtDwide(3, text);
                    }
                }
            } else {
                prtLine.init(line).book(3);
            }
        }
        logger.debug("Exit");
    }

    private static int pshTotalGiftCardBalance(Itemdata itm) {
        String[] reply = pshGetAccountBalance(itm.accountType, itm.giftCardSerial);
        if (!REPLY_OK_VALUE.equals(reply[0])) {
            return MNEMO_ERROR_BASE;
        } else {
            if (reply[1].equals("null")) reply[1] = "0";
            if (reply[2].equals("null")) reply[2] = "0";

            itm.amt = Long.parseLong(reply[1].replace(",", "").replace(".", "").trim());

            return OK;
        }
    }

    private static int pshCustomerPointsBalance(Customer cus) {
        return pshCustomerCheck(cus);
    }

    private static int pshCancelRedemption(Itemdata itm) {
        String[] ret = pshCancelTransaction(DEBIT, itm.accountType, cus.getNumber(), itm.prpnt, itm.redemptionTransaction);
        if (!REPLY_OK_VALUE.equals(ret[0])) {
            return MNEMO_ERROR_BASE + 5;
        }
        return OK;
    }

    private static int cancelPayGiftCard(Itemdata itm) {
        String[] ret = pshCancelTransaction(DEBIT, itm.accountType, itm.giftCardSerial, itm.amt, itm.gCardTransaction);
        if (!REPLY_OK_VALUE.equals(ret[0])) {
            return MNEMO_ERROR_BASE;
        }
        return OK;
    }

    private static int pshSellGiftCard(Itemdata itm) {
        if (itm.qty != 1) {
            return MNEMO_ERROR_BASE + 1;
        }
        String[] ret = pshDoActivation(GIFTCARD, itm.giftCardSerial, itm.price);
        if (!REPLY_OK_VALUE.equals(ret[0])) {
            return MNEMO_ERROR_BASE;
        }
        setGiftCardData(itm, ret);
        return OK;
    }

    private static int pshTopupGiftCard(Itemdata itm) {
        if (itm.qty != 1) {
            return MNEMO_ERROR_BASE + 1;
        }
        String[] ret = pshDoTransaction(CREDIT, GIFTCARD, itm.giftCardSerial, itm.price, null, null, "");
        if (!REPLY_OK_VALUE.equals(ret[0])) {
            return MNEMO_ERROR_BASE;
        }
        setGiftCardData(itm, ret);
        return OK;
    }

    private static void setGiftCardData(Itemdata itm, String[] ret) {
        try {
            itm.gCardTransaction = ret[2];
            if (Integer.parseInt(ret[3]) == 99 && ret[4].length() > 0) {
                itm.utilityEnglishText = ret[4];
            } else {
                itm.gCardDsc = messages.get(Integer.parseInt(ret[3]));
            }
            itm.gCardBal = ret[1];
        } catch (Exception e) {
            logger.warn("PSHP: Error parsing response. Setting defaults.");
            itm.gCardTransaction = lastTransactionID;
            itm.utilityEnglishText = "";
            itm.gCardDsc = "";
            itm.gCardBal = "";
        }
    }

    public int cancelAll() {
        ArrayList<Itemdata> vendutoLoop = new ArrayList<Itemdata>(soldGiftcards);
        for (Itemdata itm : vendutoLoop) {
            int res = pshCancelSellGiftCard(itm);
            if (res != 0) {
                Itmdc.IDC_write('Z', sc_value(tra.spf1), 0, itm.giftCardSerial, SYNCERROR_BUY, itm.price);
            } else {
                soldGiftcards.remove(itm);
            }
        }
        ArrayList<Itemdata> ricaricheLoop = new ArrayList<Itemdata>(topups);
        for (Itemdata itm : ricaricheLoop) {
            int res = pshCancelSellTopupGiftCard(itm);
            if (res != 0) {
                Itmdc.IDC_write('Z', sc_value(tra.spf1), 0, itm.giftCardSerial, SYNCERROR_TOPUP, itm.price);
            } else {
                topups.remove(itm);
            }
        }
        ArrayList<Itemdata> pagamentiLoop = new ArrayList<Itemdata>(payments);
        for (Itemdata itm : pagamentiLoop) {
            int res = cancelPayGiftCard(itm);
            if (res != 0) {
                Itmdc.IDC_write('Z', sc_value(tra.spf1), 0, itm.giftCardSerial, SYNCERROR_PAY, itm.amt);
            } else {
                payments.remove(itm);
            }
        }
        ArrayList<Itemdata> premiLoop = new ArrayList<Itemdata>(prizes);
        for (Itemdata itm : premiLoop) {
            int res = pshCancelRedemption(itm);
            if (res != 0) {
                Itmdc.IDC_write('Z', sc_value(tra.spf1), 0, itm.giftCardSerial, SYNCERROR_PAY, itm.amt);
            } else {
                prizes.remove(itm);
            }
        }
        return OK;
    }

    @Override
    public void resetAll() {
        soldGiftcards.clear();
        topups.clear();
        payments.clear();
        prizes.clear();
        utilities.clear();
    }

    public int cancelGiftCard(Itemdata itm) {
        if (itm.giftCardTopup) {
            for (Itemdata itmloop : topups) {
                if (itmloop.giftCardSerial.trim().compareTo(itm.giftCardSerial.trim()) != 0)
                    continue;
                if (itmloop.number.trim().compareTo(itm.number.trim()) != 0)
                    continue;
                if (itmloop.price != itm.price)
                    continue;
                int ret = pshCancelSellTopupGiftCard(itmloop);
                if (ret == 0) {
                    topups.remove(itmloop);
                    itm.gCardDsc = itmloop.gCardDsc;
                    itm.utilityEnglishText = itmloop.utilityEnglishText;
                    return OK;
                }
                return ret;
            }
        } else {
            for (Itemdata itmloop : soldGiftcards) {
                if (itmloop.giftCardSerial.trim().compareTo(itm.giftCardSerial.trim()) != 0) continue;
                if (itmloop.number.trim().compareTo(itm.number.trim()) != 0) continue;
                if (itmloop.price != itm.price) continue;

                int ret = pshCancelSellGiftCard(itmloop);
                if (ret == OK) {
                    soldGiftcards.remove(itmloop);
                    itm.gCardDsc = itmloop.gCardDsc;
                    itm.utilityEnglishText = itmloop.utilityEnglishText;
                }
                return ret;
            }
        }
        return MNEMO_ERROR_BASE + 2;
    }

    private static int pshCancelSellGiftCard(Itemdata itm) {
        String[] ret = pshCancelActivation(GIFTCARD, itm.giftCardSerial, itm.price, itm.gCardTransaction);
        if (!REPLY_OK_VALUE.equals(ret[0])) {
            return MNEMO_ERROR_BASE;
        }
        return OK;
    }

    private static int pshCancelSellTopupGiftCard(Itemdata itm) {
        String[] ret = pshCancelTransaction(CREDIT, GIFTCARD, itm.giftCardSerial, itm.price, itm.gCardTransaction);
        if (!REPLY_OK_VALUE.equals(ret[0])) {
            return MNEMO_ERROR_BASE;
        }
        return OK;
    }

    @Override
    public int activationGiftCard(Itemdata itm, Terminal ctl) {
        int ret = pshSellGiftCard(itm);
        if (ret == OK) {
            soldGiftcards.add(itm.copy());
        }
        return ret;
    }

    @Override
    public int reloadGiftCard(Itemdata itm) {
        int ret = pshTopupGiftCard(itm);
        if (ret == OK) {
            topups.add(itm.copy());
        }
        return ret;
    }

    @Override
    public int reconciliationGiftCard() {
        return OK;
    }

    @Override
    public int confirmTransaction(Itemdata itm) {
        return OK;
    }

    @Override
    public int cancelTransaction(Itemdata itm, String transactionType) {
        return OK;
    }

    @Override
    public int cancelTransaction(Itemdata itm) {
        return OK;
    }

    @Override
    public boolean isGiftCard(Itemdata itm) {
        return ((LEGACY_TENDER_ID + PSH_TENDER_ID).indexOf(itm.gCard) >= 0) && isEnabled();
    }

    @Override
    public String getTenderId() {
        return PSH_TENDER_ID;
    }

    public boolean isUtility(Itemdata itm) {
        logger.debug("Enter. GiftCard: " + itm.gCard);
        logger.debug("Exit. Returning: " + (itm.gCard == '2' && isPxlEnabled()));
        return  isPxlEnabled() && itm.gCard == '2';
    }

    @Override
    public CustomTender getCustomTender(int tenderId) {
        CustomTender accountFound = null;
        for (CustomTender account : accountList) {
            if (account.getTenderId() == tenderId) {
                accountFound = account;
                break;
            }
        }
        return accountFound;
    }

    @Override
    public int getCustomerTenderByGCSerialNumber(String gcSerialNumber) {
        return 0;
    }

    @Override
    public int confirmAllGiftCard() {
        return OK;
    }

    static int readDescriptionUtility(String code) {
        logger.debug("Enter. Item code: "  + code.trim());
        Properties prepayXlProps = new Properties();

        try {
            prepayXlProps.load(new FileInputStream("conf/prepayXL.properties"));

            String key = "product." + code.trim() + ".name";
            String max = "product." + code.trim() + ".max";

            if (prepayXlProps.containsKey(key)) {
                plu.utilityName = prepayXlProps.getProperty(key).trim();

                if (prepayXlProps.containsKey(max)) {
                    plu.utilityMaxPrice = Integer.parseInt(prepayXlProps.getProperty(max).trim());
                }
            } else {
                logger.debug("Exit. Code not found, returning error");
                return 7;
            }
        } catch (Exception e) {
            logger.info("Exception: " + e.getMessage());
            return 7;
        }

        logger.debug("Exit. Utility name: " + plu.utilityName);
        return OK;
    }

    ArrayList<Itemdata> getLstUtilities() {
        return utilities;
    }

    static int srvBuyUtility(boolean online) {
        logger.debug("Enter");

        if (itm.qty != 1) {
            return MNEMO_ERROR_BASE + 1;
        }

        String[] ret = srvDoPrepayXLTransactionActivation(online);
        logger.info("Result from server: " + ret[0]);

        if (!REPLY_OK_VALUE.equals(ret[0])) {
            logger.debug("Exit. Error message to display");
            return MNEMO_ERROR_BASE + 12;
        }

        try {
            itm.utilityTransaction = ret[ret.length - 1].replaceAll("(\\r|\\n)", "").trim();
            itm.utilityEnglishText = ret[1];
            itm.utilityArabicText = ret[2];
            itm.utilitySerial = ret[3];
            itm.utilityPin = ret[4];
        } catch (Exception e) {
            logger.error("Exception: ", e);
            logger.debug("Exit. Returning error message");

            itm.utilityTransaction = lastTransactionID;
            return 97;
        }

        logger.info("description item: " + itm.utilityName);
        logger.info("english text: " + itm.utilityEnglishText);
        logger.info("arabic text: " + itm.utilityArabicText);
        logger.info("utility transaction: " + itm.utilityTransaction);

        logger.debug("Exit. Ok");
        return OK;
    }

    private static String[] srvDoPrepayXLTransactionActivation(boolean online) {
        logger.debug("Enter. Online: " + online);

        String transactionId = "";
        String message = "";
        String[] reply = {};

        logger.info("item description: " + itm.utilityName);

        try {
            logger.info("call function beginTransaction");
            if (online) {
                message = ppController.beginTransaction(itm.utilityName, String.valueOf(itm.price), cus.getMobile(), ctl.uniqueId);
            } else {
                message = ppController.beginTransaction(itm.utilityName, ctl.uniqueId);
            }

            logger.info("response from server: " + message);

            reply = message.replaceAll("\r\n", "").split(";");
            logger.info("error code: " + reply[0]);
            logger.info("transactionId: " + reply[1]);

            if (Integer.parseInt(reply[0]) <= 0) {
                transactionId = reply[1];
                String additionalInfo = "";
                lastTransactionID = transactionId;
                itm.utilityTransaction = transactionId;
                if (Integer.parseInt(reply[0]) < 0) {
                    // Need more info
                    // Prompt message box to enter additional code
                    additionalInfo = readAdditionalInfo(reply[2], reply[0]);
                    if (additionalInfo == null) {
                        reply[0] = "999";

                        logger.debug("Exit. User abort");
                        return reply;
                    }
                }

                for (int time = 1; time <= RETRIES; time++) {
                    logger.info("call function processTransaction");
                    if (online) {
                        message = ppController.processTransaction(itm.utilityName, String.valueOf(itm.price), cus.getMobile(), transactionId);
                    } else {
                        message = ppController.processTransaction(itm.utilityName, transactionId);
                    }

                    logger.info("response from server: " + message);
                    message = message + ";" + transactionId;
                    logger.info("response with appended transactionId: " + message);

                    reply = message.split(";");

                    if (REPLY_OK_VALUE.equals(reply[0])) {
                        logger.info("response from server: ok");
                        break;
                    }

                    if (Integer.parseInt(reply[0]) < 100) {
                        logger.info("non-recoverable error such as account out of credit.");
                        break;
                    }
                }
            }
        } catch (NumberFormatException e) {
            logger.error("Exception: ",  e);
        }

        logger.debug("Exit. Returning: " + reply);
        return reply;
    }

    public int cancelBuyUtility(Itemdata itm) {
        logger.debug("Enter. Item code: " + itm.number.trim());

        if (!isPxlEnabled()) {
            logger.debug("Exit. PrepayXL disabled, return error message");
            return 7;
        }

        try {
            for (Itemdata itmloop : utilities) {
                logger.info("list item" + itmloop.number.trim());

                if (itmloop.number.trim().equals(itm.number.trim())) {
                    logger.info("item found");

                    int ret = pshCancelBuyUtility(itmloop);
                    logger.info("response from server: " + ret);

                    if (ret == 0) {
                        logger.info("remove item from the list");
                        utilities.remove(itmloop);
                    }

                    logger.debug("Exit. Returning: " + ret);
                    return ret;
                }
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return 97;
        }

        logger.debug("Exit. Ok");
        return OK;
    }

    private static int pshCancelBuyUtility(Itemdata itm) {
        logger.debug("Enter. TransactionId: " + itm.utilityTransaction);

        String[] ret = pshCancelPrepayXLTransactionActivation(itm.utilityTransaction);
        logger.info("error code from server response: " + ret[0]);

        if (!REPLY_OK_VALUE.equals(ret[0])) {
            logger.debug("Exit. Error message to display");
            return MNEMO_ERROR_BASE + 12;
        }

        logger.debug("Exit. Ok");
        return OK;
    }

    private static String[] pshCancelPrepayXLTransactionActivation(String transactionId) {
        logger.debug("Enter. TransactionId: " + transactionId);
        String message = "";
        String[] reply = {"999"};

        try {
            for (int time = 1; time <= RETRIES; time++) {
                message = ppController.cancelTransaction(transactionId);
                logger.info("response from server: " + message);

                reply = message.split(";");

                if (REPLY_OK_VALUE.equals(reply[0])) break;

                logger.info("Error for transaction : " + transactionId);
                if (Integer.parseInt(reply[0]) < 100) {
                    break; // non-recoverable error such as account out of credit.
                }
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
        }

        logger.debug("Exit. Returning: " + reply);
        return reply;
    }

    static int cancelAllUtilities() {
        logger.debug("Enter");

        try {
            ArrayList<Itemdata> utilitiesLoop = new ArrayList<Itemdata>(utilities);
            for (Itemdata itm : utilitiesLoop) {
                logger.info("list item: " + itm.number);

                int res = pshCancelBuyUtility(itm);
                logger.info("response from server: " + res);

                if (res != 0) {
                    Itmdc.IDC_write('Z', sc_value(tra.spf1), 0, itm.utilityTransaction, SYNCERROR_BUY, itm.price);
                } else {
                    logger.info("remove item from list");
                    utilities.remove(itm);
                }
            }
        } catch (Exception e) {
            logger.error("Exception: ", e);
            return 97;
        }

        logger.debug("Exit. Ok");
        return OK;
    }

    private static boolean ask(String message) {
        int response;

        if (SscoPosManager.getInstance().isUsed()) {
            DataNeeded dataNeeded = SscoPosManager.getInstance().sendDataNeeded("OkCancelCustomerRegistration");
            response = SscoPosManager.getInstance().waitForDataneededReply(dataNeeded.getTimeout());
        } else {
            response = gui.clearLink(message, 3);
        }
        return response == 2;
    }

    private static boolean checkPattern(String info) {
        boolean empty = true;
        for (int index = 1; index <= 9; index++) {
            String pattern = prop.getProperty(LOYALTY_PATTERN + "." + index, "");
            if (!pattern.isEmpty()) {
                if (info.matches(pattern)) return true;
                empty = false;
            }
        }
        return empty;
    }

    private static int pshCustomerCheck(Customer cus) {
        logger.debug("Enter");

        if (!checkPattern(cus.getNumber())) {
            logger.debug("Exit. Return: 159");
            return 159;
        }

        String[] reply = pshValidateAccount(LOYALTY, cus.getNumber());
        int result = 0;
        if (!REPLY_OK_VALUE.equals(reply[0])) {
            if (REPLY_NOT_FOUND_VALUE.equals(reply[0])) {
                //if (!ask(Mnemo.getMenu(124))) {
                GdPos.panel.innerVoice(ConIo.CLEAR);
                    result = 150;
                //}
            } else {
                result = MNEMO_ERROR_BASE;
            }
        }
        if (result == 0) {
            reply = pshGetAccountBalance(LOYALTY, cus.getNumber());

            logger.info("reply[0]: " + reply[0]);
            if (!REPLY_OK_VALUE.equals(reply[0])) {
                result = MNEMO_ERROR_BASE;
            } else {
                logger.info("reply[1]: " + reply[1]);

                cus.setPnt((int) Double.parseDouble(reply[1]));
                cus.setName(reply[3]);
                cus.setBranch(Integer.parseInt(prop.getProperty(BRANCH_PROP,"10")));
                if (reply.length >= PSH_API_V__1_4_CUSTOMER_REPLY_LENGTH) {
                    cus.setSelfSellEANList(reply[4]);
                } else {
                    cus.setSelfSellEANList("");
                }
                if (reply.length >= PSH_API_V_2_1_CUSTOMER_REPLY_LENGTH) {
                   cus.setBalanceMessage(reply[5]);
                   cus.setAccountInputType(reply[6]);
                   cus.setAllowedTenderTypes(reply[7]);
                }
                if (reply.length >= PSH_API_V_2_1_B_CUSTOMER_REPLY_LENGTH) {
                    if ("true".equals(prop.getProperty(ENABLE_LOYALTY_ID, "false"))) {
                        cus.setLoyaltyId(reply[8].replace('\n', ' ').trim());
                    }
                }
                if (reply.length >= PSH_API_V_2_2_CUSTOMER_REPLY_LENGTH) {
                    if (reply[9] != null && !reply[9].isEmpty()) cus.setBalanceMessage(reply[9]);
                }
            }
        }
        if (SscoPosManager.getInstance().isEnabled()) {
            SscoPosManager.getInstance().sendDataNeeded("Clear");
        }

        logger.debug("Exit. Result: " + result);
        return result;
    }

    private static int dummyCustomerCheck(Customer cus) {
        cus.setPnt((int) Double.parseDouble("100.00"));
        cus.setName("Customer Name");
        cus.setSelfSellEANList("");
        return OK;
    }

    public void customerUpdate(Customer cus, Transact tra) {
        if (!customerUpdateEnabled) {
            logger.info("Update disabled. Exit");
            return;
        }

        if (pshCustomerUpdate(cus, tra) != 0) {
            String customerCode = cus.getNumber() == null ? "0000000000000" : cus.getNumber();
            Itmdc.IDC_write('Z', sc_value(tra.spf1), 0, "0000000000000", SYNCERROR_POINTS, tra.pnt - cus.getPnt());
        }
    }

    private int pshCustomerUpdate(Customer cus, Transact tra) {
        String[] ret = pshDoTransaction(CREDIT, LOYALTY, cus.getNumber(), tra.pnt + tra.prpnt - cus.getPnt(), tra.amt, tra.gctnd);
        if (!REPLY_OK_VALUE.equals(ret[0])) {
            return MNEMO_ERROR_BASE;
        }
        try {
            if (Integer.parseInt(ret[3]) == 99 && !ret[4].isEmpty()) {
                printText(ret[4]);
            } else {
                if (!messages.get(Integer.parseInt(ret[3])).isEmpty()) {
                    prtLine.init(messages.get(Integer.parseInt(ret[3]))).book(3);
                }
            }
        } catch (Exception e) {
            System.err.println("PSHP:Error parsing response. Setting defaults.");
        }
        return OK;
    }

    private int pshPointsRedemption(Itemdata itm) {
         String[] ret = pshDoTransaction(DEBIT, LOYALTY, cus.getNumber(), -itm.prpnt, null, null, "");
        if (!REPLY_OK_VALUE.equals(ret[0])) {
            return MNEMO_ERROR_BASE;
        }
        try {
            itm.redemptionTransaction = ret[2];
            if (Integer.parseInt(ret[3]) == 99 && !ret[4].isEmpty()) {
                itm.utilityEnglishText = ret[4];
            } else {
                itm.redemptionDsc = messages.get(Integer.parseInt(ret[3]));
            }
        } catch (Exception e) {
            System.err.println("PSHP:Error parsing response. Setting defaults.");
            itm.redemptionTransaction = lastTransactionID;
            itm.redemptionDsc = "";
        }
        return OK;
    }

    public int customerCheck(Customer cus) {
        int ret = 0;
        if (SscoPosManager.getInstance().isUsed()) {
            SscoPosManager.getInstance().forceProcessor(ProcessorConstants.LOYALTY_CARD);
            ((LoyaltyRequestProcessor) (SscoPosManager.getInstance().getProcessor())).setLoyaltyInfo(new SscoCustomer(cus.getNumber(), "", "", cus.getPnt()));

            ret = SscoPosManager.getInstance().isTestEnvironment() ? dummyCustomerCheck(cus) : pshCustomerCheck(cus);
        } else {
            ret = pshCustomerCheck(cus);
        }
        if (ret != 0) {
            return ret;
        }
        // AMZ-2017-003-006#BEG -- vendita automatica
        if (!cus.getSelfSellEANList().isEmpty()) {
            tra.number = cus.getNumber(); //CGA-2017-003-006#A
            String[] eans = cus.getSelfSellEANList().split(",");
            int sts;

            if (!tra.isActive())
                GdRegis.set_tra_top();

            int nxt = event.nxt;
            event.spc = input.msk = 0;

            for (String ean : eans) {
                if (ean.startsWith(MASK_START_PV)) {
                    long promoCode = Long.parseLong(ean.substring(2, ean.indexOf("=")));
                    long promoValue = Long.parseLong(ean.substring(ean.indexOf("=") + 1));
                    logger.debug("There is a Promotion to apply PV code = " + promoCode + " , Value= " + promoValue);
                    Promo.setPromovar(promoCode, promoValue);
                } else {
                    input.prompt = "";
                    input.reset(ean.trim());
                    sts = group[5].action2(0);
                    if (sts != 0) {
                        continue;
                    }
                }
            }
            event.nxt = nxt;
        }

        cus.setMobile("");
        cus.setAdrs("");
        cus.setCity("");
        cus.setNam2("");
        cus.setDtbl("");
        cus.setFiscalId(null);
        return OK;
    }

    public int checkDirectCodeTopup(ConIo inp, Itemdata plu) {
        if (input.pb.length() >= 8) {
            String code = inp.pb;
            if (code.substring(0, 8).compareTo(prefix) == 0) {
                if (!keyboardEnabled) {
                    if (inp.key == 0x0d) {
                        return MNEMO_ERROR_BASE + 4;
                    }
                }

                logger.debug("Prefix To EAN: " + prefixToEan);
                input.reset(prefixToEan);
                plu.giftCardTopup = true;
                plu.giftCardSerial = code;

                int res = pshTotalGiftCardBalance(itm);
                if (res > 0) {
                    return res;
                }
                res = GdPos.panel.clearLink(Mnemo.getDiag(MNEMO_DIAGS_BASE + 2).trim() + editMoney(0, itm.amt), 0x83);
                if (res == 1) {
                    return -1;
                }
            }
        }
        return OK;
    }

    private static String moneyForServer(String type, long money) {
        BigDecimal bd = new BigDecimal(money);
        String ret = bd.toString();

        if (!type.equals(LOYALTY)) {
            ret = bd.movePointLeft(tnd[0].dec).toString();
            ret.replace(",", ".");
        }
        return ret;
    }

    private static String[] pshValidateAccount(String accountType, String accountNo) {
        logger.debug("Enter. accountType: " + accountType + ", accountNo" + accountNo);
        String[] reply = {"999"};
        if (SscoPosManager.getInstance().isEnabled()) {
            SscoPosManager.getInstance().sendDataNeeded("PleaseWait");
        }
        for (int time = 1; time <= RETRIES; time++) {
            String message = controller.validate(accountType, accountNo);
            logger.debug("PSHP:Server reply : " + message);
            reply = message.split(";");
            if (REPLY_OK_VALUE.equals(reply[0])) break;
        }
        return reply;
    }

    private static String[] pshGetAccountBalance(String accountType, String accountNo) {
        logger.debug("Enter. accountType: " + accountType + ", accountNo" + accountNo);
        String[] reply = {"999"};
        for (int time = 1; time <= RETRIES; time++) {
            String message = controller.getAccountBalance(accountType, accountNo, null);
            logger.debug("PSHP:Server reply : " + message);
            reply = message.split(";");
            if (REPLY_OK_VALUE.equals(reply[0])) break;
        }
        //TEST: return new String[]{"0", "123", "Ciccio", "Piccio", ""};
        return reply;
    }

    private static String[] pshDoActivation(String accountType, String accountNo, long value) {
        return pshPayload("", accountType, accountNo, value, null, null, "");
    }

    private static String[] pshDoTransaction(String transactionType, String accountType, String accountNo, long value, long amt, long amt2) {
        return pshPayload(transactionType, accountType, accountNo, value, amt, amt2, true, null, null, "");
    }

    private static String[] pshDoTransaction(String transactionType, String accountType, String accountNo, long value, Itemdata itm, List<String> items, String info) {
        return pshPayload(transactionType, accountType, accountNo, value, itm, items, info);
    }

    private static String[] pshPayload(String transactionType, String accountType, String accountNo,
                                       long value, Itemdata itm, List<String> items, String info) {
        return pshPayload(transactionType, accountType, accountNo, value, -1, -1, false, itm,  items, info);
    }

    private static String[] pshPayload(String transactionType, String accountType, String accountNo,
                                       long value, long amt, long amt2, boolean silent, Itemdata itm, List<String> items, String info) {
        String masked = accountNo != null ? mask(accountNo) : "no customer";
        logger.debug("PSHP:pshPayload : " + transactionType + ", " + accountType + ", "
                + masked + ", " + value + ", " + amt + ", " + amt2 + ", uniqueTransactionId: " + ctl.uniqueId);
        logger.debug("silent: " + silent + ", info: " + info);
        String message;

        boolean tenderExist = false;
        for (CustomTender pshTender : accountList) {
            if (pshTender.getAccountType().trim().equalsIgnoreCase(accountType.trim())) {
                tenderExist = true;
                break;
            }
        }
        if (amt < 0 && amt2 < 0 && transactionType.equals("DEBIT") && tenderExist) {
            String[] replyAmt = pshGetAccountBalance(accountType, accountNo);
            long amtGift = 0;
            if (!REPLY_OK_VALUE.equals(replyAmt[0])) {
                return replyAmt;
            } else {
                if (replyAmt[1].equals("null")) {
                    replyAmt[0] = MNEMO_ERROR_BASE + "";
                    return replyAmt;
                }
                amtGift = Long.parseLong(replyAmt[1].replace(",", "").replace(".", "").trim());
            }

            if (amtGift < value) {
                int sts = 0;
                if (checkAmountGiftCard.equals("01")) {
                    if (amtGift == 0) {
                        replyAmt[0] = GIFT_EMPTY + "";
                        return replyAmt;
                    } else {
                        sts = GdPos.panel.clearLink(Mnemo.getInfo(102), 3);
                    }

                    if (sts == 1) {  //CLEAR
                        replyAmt[0] = MNEMO_ERROR_BASE + "";
                        return replyAmt;
                    }
                }

                if (checkAmountGiftCard.equals("02")
                        || sts == 2) { //ENTER
                    value = amtGift;
                    itm.pos = itm.amt = value;
                }
            }
        }

        String valueField = moneyForServer(accountType, value);
        if (amt >= 0) {
            valueField = valueField + ";" + moneyForServer(accountType, amt) + ";" + moneyForServer(accountType, amt2);
        }
        if (!transactionType.trim().isEmpty()) {
            if ("true".equalsIgnoreCase(prop.getProperty("item-basket.enable", "false"))) {
                message = controller.beginTransaction(transactionType, accountType, accountNo, valueField, ctl.uniqueId, items);
            } else {
                message = controller.beginTransaction(transactionType, accountType, accountNo, valueField, ctl.uniqueId);
            }
        } else {
            logger.info("Call beginActivation. accountType: " + accountType + " accountNo: " + accountNo + " valueField: " + valueField + " uniqueTransactionId: " + ctl.uniqueId);
            message = controller.beginActivation(accountType, accountNo, valueField, ctl.uniqueId); // AMZ-2017-003#ADD
        }

        logger.debug("PSHP:Server reply : " + message);
        String[] reply = message.split(";");
        try {
            if (Integer.parseInt(reply[0]) <= 0) {
                String transactionId = reply[1];
                String additionalInfo = "";
                lastTransactionID = transactionId;
                if (Integer.parseInt(reply[0]) < 0) {
                    // Need more info
                    // Prompt message box to enter additional code
                    additionalInfo = readAdditionalInfo(reply[2], reply[0]);
                    if (additionalInfo == null) {
                        reply[0] = "999"; // user abort
                        return reply;
                    }
                }
                logger.debug("PSHP:Process " + transactionType + ", " + accountType + ", " + mask(accountNo) + ", "
                        + valueField + ", " + transactionId + ", " + additionalInfo);
                for (int time = 1; time <= RETRIES; time++) {
                    if (!transactionType.trim().isEmpty()) {
                        message = controller.processTransaction(transactionType, accountType, accountNo,
                                valueField, transactionId, info + additionalInfo);
                    } else {
                        message = controller.processActivation(accountType, accountNo, valueField, transactionId,
                                info + additionalInfo);
                    }
                    logger.debug("PSHP:Server reply : " + message);
                    reply = message.split(";");

                    if (REPLY_OK_VALUE.equals(reply[0])) {
                        break;
                    }
                    // ERROR HANDLING
                    logger.debug("PSHP:Error for transaction : " + transactionId);
                    if (Integer.parseInt(reply[0]) < 100) {
                        break; // non-recoverable error such as account out of credit.
                    }
                }
            } else {
                logger.debug("PSHP:Could not get transactionID. Exit.");
            }
        } catch (NumberFormatException e) {
            logger.warn("PSHP:Error parsing response. Exit.");
        }

        if (reply[0].length() > 0 && Integer.parseInt(reply[0]) > 0) {
            if (!silent) {
                dspLine.init(reply[1]).show(1);
            }
        }
        return reply;
    }

    private static String[] pshCancelActivation(String accountType, String accountNo, long value, String transactionId) {
        return pshCancelPayload("", accountType, accountNo, value, transactionId);
    }

    private static String[] pshCancelTransaction(String transactionType, String accountType, String accountNo, long value, String transactionId) {
        return pshCancelPayload(transactionType, accountType, accountNo, value, transactionId);
    }

    private static String mask(String serial) {
        if (serial == null || serial.isEmpty()) return "";
        return serial.substring(0, serial.length() - 2) + "**";
    }

    private static String[] pshCancelPayload(String transactionType, String accountType, String accountNo, long value, String transactionId) {
        logger.debug("PSHP:srvCancelTransactionActivation : " + transactionType + ", " + accountType + ", "
                + accountNo + ", " + value + ", " + transactionId);
        String message;
        String[] reply = {"999"};
        logger.debug("PSHP:Process " + transactionType + ", " + accountType + ", " + mask(accountNo) + ", "
                + moneyForServer(accountType, value) + ", " + transactionId);
        for (int time = 1; time <= RETRIES; time++) {
            if (transactionType.trim().length() > 0) {
                message = controller.cancelTransaction(transactionType, accountType, accountNo, moneyForServer(accountType, value), transactionId);
            } else {
                message = controller.cancelActivation(accountType, accountNo, moneyForServer(accountType, value), transactionId);
            }
            logger.debug("PSHP:Server reply : " + message);
            reply = message.split(";");

            if (REPLY_OK_VALUE.equals(reply[0])) break;

            logger.debug("PSHP:Error for transaction : " + transactionId);
            if (Integer.parseInt(reply[0]) < 100) break;
        }

        return reply;
    }

    public static void loadMessages() {
        int rec = 0;

        DatIo lPHM = new DatIo("PHM", 4, 46);
        lPHM.open(null, "P_REG" + lPHM.id + ".DAT", 1);
        while (lPHM.read(++rec) > 0) {
            int ind = lPHM.scanNum(3);
            String txt = lPHM.pb.substring(lPHM.scan(':').index);
            messages.put(ind, txt);
        }
        lPHM.close();
    }

    private String buildDataCollect() {
        int nbr = 0, rec;
        StringBuilder dataCollect = new StringBuilder();

        for (rec = 0; lTRA.read(++rec) > 0; ) {
            char type = lTRA.pb.charAt(32);
            if (type == 'C') /* skip empl/cust % template */
                if (lTRA.pb.charAt(35) == '9')
                    continue;
            if (type == 'u') {
                String ean = lTRA.pb.substring(43, 59);
                int i = WinUpb.getInstance().findUpbTra(ean, false);
                if (i >= 0 && tra.itemsVsUPB.get(i).isConfirmed()) {
                    lTRA.pb = lTRA.pb.substring(0, lTRA.pb.length() - 1) + "0";
                }
            }
            dataCollect.append(lTRA.scan(28));
            dataCollect.append(editNum(++nbr, 3));
            dataCollect.append(lTRA.skip(3).scan(3));
            dataCollect.append(editNum(tra.mode, 1));
            dataCollect.append(lTRA.pb.substring(++lTRA.index));
            dataCollect.append("\n");
        }
        return dataCollect.toString();
    }

    public void sendDataCollect() {
        logger.info("Enter");

        if (!tlogSyncingEnabled) {
            logger.info("EXIT sendDataCollect(), Syncing disabled");
            return;
        }

        String dataCollect = buildDataCollect();

        String tlogResult = " ";
        int retry = 3;
        if (tra.mode == M_GROSS) {
            while (tlogResult.indexOf(TLOG_OK) < 0 && retry > 0) {
                logger.info("send datacollect : logSTORE(" + ctl.uniqueId + ", dc_length: " + dataCollect.length() + " ) ");
                logger.debug("send datacollect : logSTORE(" + ctl.uniqueId + ",\n" + dataCollect + " ) ");
                tlogResult = logController.logSTORE(ctl.uniqueId, dataCollect);
                retry--;
                logger.info("tlogResult : >" + tlogResult + "<");
                try {
                    logger.info("wait 50 msec");
                    Thread.sleep(50);
                } catch (Exception e) {
                    logger.info("exception e: " + e.getMessage());
                }
            }
        }
        logger.info("Exit");
    }

    @Override
    public void printSummaries(String type) {
        if (isPrintUtilitiesEnabled(type)) printUtilities(type);
        if (isPrintGiftCardPaymentsEnabled(type)) printGiftCardPayments(type);
    }

    @Override
    public int refundGiftCard(Itemdata itm, Terminal ctl) {
        return 0;
    }

    public boolean isPrintUtilitiesEnabled(String type){
        logger.debug("print-utilities." + type + " = " + prop.getProperty("print-utilities." + type));
        return "true".equalsIgnoreCase(prop.getProperty("print-utilities." + type));
    }

    public boolean isPrintGiftCardPaymentsEnabled(String type){
        logger.debug("print-gift-cards." + type + " = " + prop.getProperty("print-gift-cards." + type));
        return "true".equalsIgnoreCase(prop.getProperty("print-gift-cards." + type));
    }

    public static void printGiftCardPayments(String type) {
        if (printEnabled) {
            for (Itemdata payment : payments) {
                printGiftCardPayment(payment, type);
            }
        }
    }

    public static void printGiftCardPayment(Itemdata item, String type) {
        logger.debug("Enter");

        prtLine.init(' ').type(2);
        prtLine.init(tnd[item.tnd].tx20);
        prtLine.onto(20, tnd[itm.tnd].symbol).upto(40, editMoney(item.tnd, item.amt));
        prtLine.push(item.mark).type(2);
        if (printSerialEnabled) {
            prtLine.init(mask(item.giftCardSerial)).type(2);
        }
        if (SAME_RECEIPT_TYPE.equals(type)) {
            Struc.prtLine.init(' ').book(3);
        } else {
            newReceipt();
        }

        logger.debug("Exit");
    }

    private static void newReceipt() {
        GdRegis.set_trailer();
        Struc.prtLine.type(2);
        GdRegis.hdr_print();
    }

    public static void printUtilities(String type) {
        for (Itemdata utility : utilities) {
            printUtility(utility, type);
        }
    }

    private static void printUtility(Itemdata item, String type) {
        logger.debug("ENTER printUtility");
        int pos = 0;

        prtLine.init(' ').type(2);
        prtLine.init("####################").type(2);
        prtLine.init(' ').type(2);

        ///////// english section
        printUtilityText(item.utilityEnglishText);

        prtLine.init(' ').type(2);
        prtLine.init(' ').type(2);

        ///////// arabic section
        printUtilityText(item.utilityArabicText);

        prtLine.init(' ').type(2);
        prtLine.init("####################").type(2);
        prtLine.init(' ').type(2);

        if (SAME_RECEIPT_TYPE.equals(type)) {
            Struc.prtLine.init(' ').book(3);
        } else {
            newReceipt();
        }

        logger.debug("EXIT printUtility");
    }

    private static void printUtilityText(String text) {
        int pos;
        String[] rows = text.split("<br />");

        for (String row : rows) {
            if (row.contains("<h") || row.contains("</h")) {
                String[] rowBold = row.split("<h");

                int x = 0;
                if (rowBold[0].length() > 0 && !rowBold[0].contains("</h")) {
                    prtLine.init(rowBold[0]).type(2);
                    x = 1;
                } else if (rowBold[0].length() == 0) {
                    x = 1;
                }

                for (; x < rowBold.length; x++) {
                    if ((pos = rowBold[x].indexOf("</h")) >= 0) {
                        if (rowBold[x].charAt(1) == '>') {
                            text = rowBold[x].substring(2, pos);
                        } else {
                            text = rowBold[x].substring(0, pos);
                        }

                        prtDwide(3, text);

                        if (pos + 5 < rowBold[x].length()) {
                            prtLine.init(rowBold[x].substring(pos + 5)).type(2);
                        }
                    } else {
                        text = rowBold[x].substring(2);
                        prtDwide(3, text);
                    }
                }
            } else if (row.contains("<b>")) {
                String[] rowBold = row.split("<b>");

                int x = 0;
                if (rowBold[0].length() > 0 && !rowBold[0].contains("</b>")) {
                    prtLine.init(rowBold[0]).type(2);
                    x = 1;
                } else if (rowBold[0].length() == 0) {
                    x = 1;
                }

                for (; x < rowBold.length; x++) {
                    if ((pos = rowBold[x].indexOf("</b>")) >= 0) {
                        text = rowBold[x].substring(0, pos);
                        prtDwide(3, text);

                        if (pos + 5 < rowBold[x].length()) {
                            prtLine.init(rowBold[x].substring(pos + 5)).type(2);
                        }
                    } else {
                        text = rowBold[x];
                        prtDwide(3, text);
                    }
                }
            } else {
                prtLine.init(row).type(2);
            }
        }
    }

    public void addPayment(Itemdata itm) {
        payments.add(itm.copy());
    }
}