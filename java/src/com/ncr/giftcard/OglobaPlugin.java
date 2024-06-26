package com.ncr.giftcard;

import com.google.gson.*;

import java.io.*;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.ncr.*;
import com.ncr.giftcard.olgb.data.CustomTenderOg;
import com.ncr.giftcard.olgb.data.requests.*;
import com.ncr.giftcard.olgb.data.responses.*;
import com.ncr.giftcard.psh.data.CustomTender;
import com.ncr.gui.ModDlg;
import com.ncr.restclient.IHttpClient;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

import com.ncr.ssco.communication.manager.SscoPosManager;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.log4j.Logger;

import java.net.SocketTimeoutException;
import java.util.*;
import java.util.concurrent.TimeoutException;

import static com.ncr.Basis.*;
import static com.ncr.Struc.ctl;


/**
 * NCRMEA-2022-002 Olgb GC integration class
 *
 * @author SMO
 */
public class OglobaPlugin extends GiftCardPlugin {
    private static final Logger logger = Logger.getLogger(OglobaPlugin.class);
    private static OglobaPlugin instance = null;
    private final static boolean isSuccessful = false;
    private static int MNEMO_ERROR_BASE = 86;
    private static int MNEMO_ERROR_RELOAD_AMT_MAX = 162;
    private static int MNEMO_ERROR_RELOAD_AMT_MIN = 164;
    private static int MNEMO_ERROR_REFUND_AMT_MAX = 162;
    public static final String PROPERTIES = "conf/ogloba.properties";
    public static final String ERROR_CODE_PROPERTIES = "conf/errorCodes.properties";
    private static final String USERNAME_PROPERTY = "username";
    private static final String PASSWORD_PROPERTY = "password";
    private static final String OLGB_SRV_ENDPOINT = "endPoint";
    private static final String ACTIVATION_CARD_API = "/activation";
    private static final String CONFIRM_TRANSACTION_API = "/confirmTransaction";
    private static final String RELOAD_API = "/reload";
    private static final String RECONCILIATION_CARD_API = "/reconciliation";
    private static final String REDEMPTION_CARD_API = "/redemption";
    private static final String CANCEL_TRANSACTION_API = "/cancelTransaction";
    private static final String REVERSAL_API = "/reversal";
    private static final String MERCHANT_ID_PROPERTY = "merchantId";
    private static final int STATUS_SUCCESS = 200;
    private static final String TIMEOUT_PROPERTY = "timeout";
    private static final String PAYMENT_MEAN = "GC";
    private static boolean enabled = false;
    public static boolean readingGCSerial = false;
    private static boolean insertKeyBoardEnabled = true;
    private static boolean KEYBOARD_ENABLED = true;
    private static final String SERVER_ERROR_CODE = "-2";
    private static final String SERVER_ERROR_DESC = "ERROR SERVER";
    private static final String ERROR_CODE = "0000";
    private static final String TIMEOUT_CODE = "-1";
    private static final String IS_SUCCESS_FIELD = "isSuccess";
    private static final String IS_SUCCESSFULL_FIELD = "isSuccessful";
    private static String transactionNumber = ""; // AMZ-2017-003#ADD
    private static String originaltransactionNumber = "";
    // TRANSACTION TYPE VALUES
    private static final String TRANSACTION_TYPE_ACTIVATION = "A";
    private static final String TRANSACTION_TYPE_RELOAD = "L";
    private static final String TRANSACTION_TYPE_REDEMPTION = "P";
    private static final String TRANSACTION_TYPE_VOID = "V";
    public static final String SALE_ACCOUNT = "account.sale";
    public static final String TOPUP_ACCOUNT = "account.topup";

    private static final String FINAL_STATUS_REQUESTED_NOT_CONFIRMED_CANCELED = "R";
    private static final String FINAL_STATUS_REQUESTED_CONFIRMED = "Y";
    private static final String FINAL_STATUS_REQUESTED_CANCELED = "N";

    public static final String RELOAD_GC_DESC = "Gift Card Reload";
    private static ArrayList<CustomTender> accountList = new ArrayList<CustomTender>();
    private static String tenderMaskProps = "accountType.";
    private Properties props = new Properties();
    private Properties errorProps = new Properties();

    private static Map<String, String> errorCodesMap = new HashMap<String, String>();
    private static final String MASK_ERROR_CODE_PROPERTY = "oglobaPlugin.";
    //Reconciliation file Info
    private static final String RECONCILIATION_FILE_PATH = "reconciliationFile.path";
    private static final String RECONCILIATION_FILE_NAME_MASK = "reconciliationFile.name.mask";
    private static boolean fileIsEmpty = true;
    private static String businessDate = "";
    private static int decimalDigits = 0;

    public final static String NEW_RECEIPT_TYPE = "new-receipt";
    public final static String SAME_RECEIPT_TYPE = "same-receipt";

    //Transaction Lists
    public static ArrayList<Itemdata> solds = new ArrayList<Itemdata>();
    public static ArrayList<Itemdata> reloadeds = new ArrayList<Itemdata>();
    public static ArrayList<Itemdata> refunds = new ArrayList<Itemdata>();
    public static ArrayList<Itemdata> payments = new ArrayList<Itemdata>();
    public static ArrayList<ReconciliationRecord> records = new ArrayList<ReconciliationRecord>();
    public static String maxAmountReloaded = "";
    public static String minAmountReloaded = "";
    public static String maxAmountRefund = "";


    private static IHttpClient client;
    private Gson gson = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY)
            .setPrettyPrinting()
            .create();

    public static OglobaPlugin getInstance() {
        if (instance == null)
            instance = new OglobaPlugin();
        return instance;
    }

    private OglobaPlugin() {
        loadProperties();
        loadErrorCodeMap();
        enabled = Boolean.parseBoolean(getProps().getProperty("enabled"));
        readAccountNumberList();
    }


    private void loadProperties() {
        logger.debug("Enter");
        try {
            props.load(new FileInputStream(PROPERTIES));
        } catch (Exception e) {
            logger.error("Error: ", e);
        }
        logger.debug("Exit");
    }

    public void loadErrorCodeMap() {
        logger.debug("ENTER loadErrorCodeMap");

        Properties prop = new Properties();

        try {
            prop.load(new FileInputStream(ERROR_CODE_PROPERTIES));
            for (Object key : prop.keySet().toArray()) {
                if (key.toString().startsWith(MASK_ERROR_CODE_PROPERTY)) {
                    String value = prop.getProperty(key.toString());
                    errorCodesMap.put(key.toString().substring(key.toString().lastIndexOf('.') + 1), value);
                }
            }
        } catch (Exception e) {
            logger.error("Error: ", e);
        }
        logger.debug("EXIT loadErrorCodeMap");
    }

    @Override
    public int activationGiftCard(Itemdata itm, Terminal ctl) {
        logger.debug("Enter activationGiftCard " + this.getClass());
        int res;
        if ((res = checkAmount(itm)) != 0) return res;

        ActivationResponse response = (ActivationResponse) srvActivation(itm);
        if (response.isSuccessful()) {
            itm.referenceNumberGC = response.getReferenceNumber();
            itm.setOriginalTransactionNumberGC(getTransactionNumber());
            itm.gCardBal = response.getBalance() + "";
            solds.add(itm);
            records.add(addRecordItems(itm, FINAL_STATUS_REQUESTED_NOT_CONFIRMED_CANCELED, itm.price, TRANSACTION_TYPE_ACTIVATION));
            logger.debug("Exit activationGiftCard ");
            return 0;
        } else {
            if (Integer.parseInt(response.getErrorCode()) < 0) {
                logger.debug("Exit activationGiftCard ");
                return MNEMO_ERROR_BASE;
            } else {
                if (!SscoPosManager.getInstance().isEnabled())
                    GdPos.panel.clearLink(errorCodesMap.get(response.getErrorCode()) != null ? errorCodesMap.get(response.getErrorCode()) : response.getErrorCode() + " - " + response.getErrorMessage(), 0x81);
                logger.debug("Exit activationGiftCard ");
                return Integer.parseInt(SERVER_ERROR_CODE);
            }
        }
    }

    private ReconciliationRecord addRecordItems(Itemdata itm, String status, long amount, String transactionType) {
        logger.debug("Enter addRecordItems");
        ReconciliationRecord record = new ReconciliationRecord();
        record.setAmount(addDecimals(amount));
        record.setBalance(Double.parseDouble(itm.gCardBal.trim().length() > 0 ? itm.gCardBal : "0"));
        record.setCardNumber(itm.giftCardSerial);
        record.setCashierId(ctl.ckr_nbr + "");
        record.setTransactionNumber(getTransactionNumber());
        record.setTransactionType(transactionType);
        record.setFinalStatus(status);
        record.setTerminalId(ctl.reg_nbr + "");
        record.setReferenceNumber(itm.referenceNumberGC);
        record.setCurrency(tnd[0].symbol);
        record.setTerminalTxNo(ctl.tran + "");
        record.setGenCode(itm.number);
        return record;
    }

    public int reversalGiftCard(Itemdata itm, String TransactionType) {
        ReversalResponse response = (ReversalResponse) srvReversal(itm);
        if (!response.isSuccessful()) {
            records.add(addRecordItems(itm, FINAL_STATUS_REQUESTED_CANCELED, itm.price, TransactionType));
        }
        logger.debug("Exit ReversalGiftCard ");
        return 0;

    }

    @Override
    public int reloadGiftCard(Itemdata itm) {
        logger.debug("Enter");
        int res;
        if ((res = checkAmount(itm)) != 0) return res;

        TransactionResponse response = (TransactionResponse) srvReloadGiftCard(itm);
        if (response.isSuccessful()) {
            logger.debug("IsSuccessful = " + true);
            itm.referenceNumberGC = response.getReferenceNumber();
            itm.setOriginalTransactionNumberGC(getTransactionNumber());
            itm.gCardBal = response.getBalance() + "";
            reloadeds.add(itm);
            records.add(addRecordItems(itm, FINAL_STATUS_REQUESTED_NOT_CONFIRMED_CANCELED, itm.price, TRANSACTION_TYPE_RELOAD));
            logger.debug("Exit Ok");
            return 0;
        } else {
            if (Integer.parseInt(response.getErrorCode()) < 0) {
                logger.debug("Exit: " + MNEMO_ERROR_BASE);
                return MNEMO_ERROR_BASE;
            } else {
                if (!SscoPosManager.getInstance().isEnabled())
                    GdPos.panel.clearLink(errorCodesMap.get(response.getErrorCode()) != null ? errorCodesMap.get(response.getErrorCode()) : response.getErrorCode() + " - " + response.getErrorMessage(), 0x81);
                logger.debug("Exit " + SERVER_ERROR_CODE);
                return Integer.parseInt(SERVER_ERROR_CODE);
            }
        }
    }

    private int checkAmount(Itemdata itm) {
        long maxAmountReloaded = Long.parseLong(props.getProperty("reload.max.amount", "9999999"));
        long minAmountReloaded = Long.parseLong(props.getProperty("reload.min.amount", "0"));

        if (itm.price > maxAmountReloaded) {
            logger.debug("Price item exceeds Max amount to be Reload GiftCard " + maxAmountReloaded);
            logger.debug("Exit ReloadGiftCard ");
            return MNEMO_ERROR_RELOAD_AMT_MAX;
        } else if (itm.price < minAmountReloaded) {
            logger.debug("Price item is less than Min Amount to be Reload GiftCard " + minAmountReloaded);
            logger.debug("Exit ReloadGiftCard ");
            return MNEMO_ERROR_RELOAD_AMT_MIN;
        }
        return 0;
    }

    @Override
    public int reconciliationGiftCard() {
        logger.debug("Enter reconciliationGiftCard " + this.getClass());
        ReconciliationResponse response = (ReconciliationResponse) srvReconciliation();
        if (!response.isSuccess()) {
            logger.debug("Exit ReconciliationGiftCard ");
            if (!SscoPosManager.getInstance().isEnabled())
                GdPos.panel.clearLink(errorCodesMap.get(response.getErrorCode()) != null ? errorCodesMap.get(response.getErrorCode()) : response.getErrorCode() + " - " + response.getErrorMessage(), 0x81);
            return Integer.parseInt(response.getErrorCode());
        }
        return 0;
    }

    @Override
    public int cancelGiftCard(Itemdata itm) {
        int ret = 0;

        if (itm.giftCardTopup) {
            ArrayList<Itemdata> reloadItemsLoop = new ArrayList<Itemdata>(reloadeds);

            for (Itemdata itmloop : reloadItemsLoop) {
                if (itmloop.giftCardSerial.trim().compareTo(itm.giftCardSerial.trim()) != 0)
                    continue;
                if (itmloop.number.trim().compareTo(itm.number.trim()) != 0)
                    continue;
                if (itmloop.price != itm.price)
                    continue;
                ret = cancelTransaction(itmloop, TRANSACTION_TYPE_RELOAD);
                reloadeds.remove(itmloop);
                itm.gCardDsc = itmloop.gCardDsc;
            }
        } else {
            ArrayList<Itemdata> soldItemsLoop = new ArrayList<Itemdata>(solds);

            for (Itemdata itmloop : soldItemsLoop) {
                if (itmloop.giftCardSerial.trim().compareTo(itm.giftCardSerial.trim()) != 0)
                    continue;
                if (itmloop.number.trim().compareTo(itm.number.trim()) != 0)
                    continue;
                if (itmloop.price != itm.price)
                    continue;
                ret = cancelTransaction(itmloop, TRANSACTION_TYPE_ACTIVATION);
                solds.remove(itmloop);
                itm.gCardDsc = itmloop.gCardDsc;
            }
        }

        for (ReconciliationRecord record : records) {
            if (record.getCardNumber().trim().compareTo(itm.giftCardSerial.trim()) != 0)
                continue;
            if (record.getGenCode().trim().compareTo(itm.number.trim()) != 0)
                continue;
            if (record.getReferenceNumber().trim().compareTo(itm.referenceNumberGC.trim()) != 0)
                continue;

            if (removeDecimals(record.getAmount()) != itm.price)
                continue;
            record.setFinalStatus(FINAL_STATUS_REQUESTED_CANCELED);
        }

        if (!records.isEmpty()) {
            createReconciliationFile(records);
        }

        return ret;
    }

    @Override
    public int refundGiftCard(Itemdata itm, Terminal ctl) {
        logger.debug("Enter RefundGiftCard " + this.getClass());
        int res;
        long maxAmountRefund = Long.parseLong(props.getProperty("refund.max.amount", "9999999"));

        if (itm.amt > maxAmountRefund) {
            logger.debug("Price item exceeds Max amount to be Refund GiftCard " + maxAmountRefund);
            logger.debug("Exit RefundGiftCard ");
            return MNEMO_ERROR_REFUND_AMT_MAX;
        }
        res = readSerial32(itm);

        if (res > 0) return res;
        else if (res < 0) return 5;

        String serial = checkGiftCardSerial(itm);
        if(serial.isEmpty()) {
            itm.giftCardSerial = "";
            return 8;
        }
        itm.number = serial;
        TransactionResponse response = (TransactionResponse) srvRefundGiftCard(itm);
        if (response.isSuccessful()) {
            logger.debug("IsSuccessful = " + true);
            itm.referenceNumberGC = response.getReferenceNumber();
            itm.setOriginalTransactionNumberGC(getTransactionNumber());
            itm.gCardBal = response.getBalance() + "";
            refunds.add(itm);
            records.add(addRecordItems(itm, FINAL_STATUS_REQUESTED_NOT_CONFIRMED_CANCELED, itm.price, TRANSACTION_TYPE_RELOAD));
            logger.debug("Exit RefundGiftCard ");
            return 0;
        } else {
            itm.giftCardSerial = "";
            if (Integer.parseInt(response.getErrorCode()) < 0) {
                logger.debug("Exit RefundGiftCard ");
                return MNEMO_ERROR_BASE;
            } else {
                if (!SscoPosManager.getInstance().isEnabled())
                    GdPos.panel.clearLink(errorCodesMap.get(response.getErrorCode()) != null ? errorCodesMap.get(response.getErrorCode()) : response.getErrorCode() + " - " + response.getErrorMessage(), 0x81);
                logger.debug("Exit RefundGiftCard ");
                return Integer.parseInt(SERVER_ERROR_CODE);
            }
        }
    }

    @Override
    public int cancelAll() {
        ArrayList<Itemdata> soldItemsLoop = new ArrayList<Itemdata>(solds);
        for (Itemdata itm : soldItemsLoop) {
            int res = cancelTransaction(itm, TRANSACTION_TYPE_ACTIVATION);
            if (res != 0) {
                Itmdc.IDC_write('Z', sc_value(tra.spf1), 0, itm.giftCardSerial, SYNCERROR_BUY, itm.price);
            } else {
                solds.remove(itm);
            }
        }
        ArrayList<Itemdata> reloadedItemsLoopLoop = new ArrayList<Itemdata>(reloadeds);
        for (Itemdata itm : reloadedItemsLoopLoop) {
            int res = cancelTransaction(itm, TRANSACTION_TYPE_RELOAD);
            if (res != 0) {
                Itmdc.IDC_write('Z', sc_value(tra.spf1), 0, itm.giftCardSerial, SYNCERROR_TOPUP, itm.price);
            } else {
                reloadeds.remove(itm);
            }
        }
        ArrayList<Itemdata> paymentItemsLoopLoop = new ArrayList<Itemdata>(payments);
        for (Itemdata itm : paymentItemsLoopLoop) {
            int res = cancelTransaction(itm, TRANSACTION_TYPE_REDEMPTION);
            if (res != 0) {
                Itmdc.IDC_write('Z', sc_value(tra.spf1), 0, itm.giftCardSerial, SYNCERROR_PAY, itm.amt);
            } else {
                payments.remove(itm);
            }
        }
        ArrayList<Itemdata> refundItemsLoopLoop = new ArrayList<Itemdata>(refunds);
        for (Itemdata itm : refundItemsLoopLoop) {
            int res = cancelTransaction(itm, TRANSACTION_TYPE_RELOAD);
            if (res != 0) {
                Itmdc.IDC_write('Z', sc_value(tra.spf1), 0, itm.giftCardSerial, SYNCERROR_TOPUP, itm.price);
            } else {
                refunds.remove(itm);
            }
        }
        if(!records.isEmpty()){

        }
        if (!records.isEmpty()) {
            createReconciliationFile(records);
        }
        return 0;
    }

    @Override
    public int cancelTransaction(Itemdata itm, String transactionType) {
        logger.debug("Enter cancelGiftCard " + this.getClass());
        Response response = srvCancellation(itm);
        if (response.isSuccessful()) {
            logger.debug("GIFT CARD CANCELED SUCCESSFULLY");
            return 0;
        } else {
            logger.debug("ERROR CANCELLING GIFT CARD : " + maskErrorCode(response.getErrorCode(), response.getErrorMessage()));
            logger.debug("GIFT CARD CANCELLATION WILL BE DONE BY OGLOBA SERVER DURING RECONCILIATION");
            if (!SscoPosManager.getInstance().isEnabled()) {
                GdPos.panel.clearLink(maskErrorCode(response.getErrorCode(), response.getErrorMessage()), 0x80);
                GdPos.panel.clearLink("CANCEL. WILL BE DONE DURING RECONCILIATION", 0x80);
            }
            return Integer.parseInt(response.getErrorCode());
        }
    }

    @Override
    public int cancelTransaction(Itemdata itm) {
        return 0;
    }

    @Override
    public int confirmTransaction(Itemdata itm) {
        logger.debug("Enter confirmationTransaction " + this.getClass());
        ConfirmTransactionResponse response = (ConfirmTransactionResponse) srvConfirmation(itm);
        if (response.isSuccessful()) {
            logger.debug("Exit confirmationTransaction ");
            itm.gCardBal = response.getBalance() + "";
            return 0;
        } else {
            logger.debug("Exit confirmationTransaction");
            if (!SscoPosManager.getInstance().isEnabled())
                GdPos.panel.clearLink(maskErrorCode(response.getErrorCode(), response.getErrorMessage()), 0x81);
            return Integer.parseInt(response.getErrorCode());
        }
    }

    private String maskErrorCode(String errorCode, String errorDescription) {
        return errorCodesMap.get(errorCode) != null ? errorCodesMap.get(errorCode) : errorDescription;
    }

    @Override
    public int redemptionGiftCard(Itemdata itm, Transact tra) {
        int res = readSerial32(itm);

        if (res > 0) return res;
        else if (res < 0) return 5;

        String serial = checkGiftCardSerial(itm);
        if (serial.isEmpty()) {
            itm.giftCardSerial = "";
            return 8;
        }
        itm.number = serial;
        RedemptionResponse response = (RedemptionResponse) srvRedemptionGiftCard(itm);
        if (response.isSuccessful()) {
            itm.referenceNumberGC = response.getReferenceNumber();
            itm.setOriginalTransactionNumberGC(getTransactionNumber());
            itm.gCardBal = response.getBalance() + "";
            payments.add(itm);
            records.add(addRecordItems(itm, FINAL_STATUS_REQUESTED_NOT_CONFIRMED_CANCELED, itm.amt, TRANSACTION_TYPE_REDEMPTION));
            logger.debug("Exit RedemptionGiftCard ");
            return 0;
        } else {
            itm.giftCardSerial = "";
            if (Integer.parseInt(response.getErrorCode()) < 0) {
                logger.debug("Exit RedemptionGiftCard ");
                return MNEMO_ERROR_BASE;
            } else {
                if (!SscoPosManager.getInstance().isEnabled())
                    GdPos.panel.clearLink(errorCodesMap.get(response.getErrorCode()) != null ? errorCodesMap.get(response.getErrorCode()) : response.getErrorCode() + " - " + response.getErrorMessage(), 0x81);
                logger.debug("Exit RedemptionGiftCard ");
                return 156;
            }
        }

    }

    public void addPayment(Itemdata itm) {
        payments.add(itm.copy());
    }

    public int getCustomerTenderByGCSerialNumber(String gcSerialNumber) {
        for (CustomTender cs : accountList) {
            String patternSt = ((CustomTenderOg) cs).getPattern();
            if (patternSt != null) {
                if (gcSerialNumber.matches(patternSt)) {
                    return cs.getTenderId();
                }
            }
        }
        return 0;
    }

    public Response srvCancellation(Itemdata itm) {
        Response response = new Response();
        CancelTransactionRequest request = (CancelTransactionRequest) setCancelTransactionRequestObject(itm);
        do {
            response = doTransaction(CANCEL_TRANSACTION_API, request, response);
        } while (!response.isSuccessful() && response.getErrorCode().equals(TIMEOUT_CODE));

        return response;
    }

    private Response srvReloadGiftCard(Itemdata itm) {
        ReloadRequest request = setReloadRequestObject(itm);
        return srvCommunication(itm, request, "Card Cannot be Reloaded");
    }

    private Response srvRefundGiftCard(Itemdata itm) {
        RefundRequest request = setRefundRequestObject(itm);
        return srvCommunication(itm, request, "Refund Error");
    }

    private TransactionResponse srvCommunication(Itemdata itm, PaymentRequest request, String errorMessage) {
        TransactionResponse response = new TransactionResponse();
        response = (TransactionResponse) doTransaction(RELOAD_API, request, response);
        if (!response.isSuccessful() && response.getErrorCode().equals(TIMEOUT_CODE)) {
            reversalGiftCard(itm, TRANSACTION_TYPE_RELOAD);
            response.setSuccessful(false);
            response.setErrorMessage(errorMessage);
            itm.giftCardSerial = "";
            itm.setOriginalTransactionNumberGC("");
            itm.referenceNumberGC = "";
        }
        return response;
    }

    private Response srvRedemptionGiftCard(Itemdata itm) {
        RedemptionRequest request = (RedemptionRequest) setRedemptionRequestObject(itm);

        RedemptionResponse response = new RedemptionResponse();
        response = (RedemptionResponse) doTransaction(REDEMPTION_CARD_API, request, response);
        if (!response.isSuccessful() && response.getErrorCode().equals(TIMEOUT_CODE)) {
            reversalGiftCard(itm, TRANSACTION_TYPE_REDEMPTION);
            response.setSuccessful(false);
            response.setErrorMessage("Redemption Error");
            itm.giftCardSerial = "";
            itm.setOriginalTransactionNumberGC("");
            itm.referenceNumberGC = "";
        }
        return response;
    }

    public Response srvActivation(Itemdata itm) {
        ActivationResponse response = new ActivationResponse();
        ActivationRequest request = (ActivationRequest) setActivationRequestObject(itm);

        response = (ActivationResponse) doTransaction(ACTIVATION_CARD_API, request, response);
        if (!response.isSuccessful() && response.getErrorCode().equals(TIMEOUT_CODE)) {
            reversalGiftCard(itm, TRANSACTION_TYPE_ACTIVATION);
            response.setSuccessful(false);
            response.setErrorMessage("Card Cannot be Activated");
            itm.giftCardSerial = "";
            itm.setOriginalTransactionNumberGC("");
            itm.referenceNumberGC = "";
        }
        return response;
    }

    public Response srvConfirmation(Itemdata itm) {
        int retries = 4;
        int i = 0;
        ConfirmTransactionResponse response = new ConfirmTransactionResponse();
        ConfirmTransactionRequest request = setConfirmTransactionRequest(itm);
        do {
            response = (ConfirmTransactionResponse) doTransaction(CONFIRM_TRANSACTION_API, request, response);
            if (response.isSuccessful()) {
                return response;
            }
            i++;
        } while (i < retries && !response.isSuccessful() && response.getErrorCode().equals(TIMEOUT_CODE));
        return response;
    }

    public ResponseBase srvReconciliation() {
        int retries = 4;
        int i = 0;
        ReconciliationResponse res = new ReconciliationResponse();

        ReconciliationRequest request = setReconciliationRequestObject();
        do {
            res = (ReconciliationResponse) doReconciliationTransaction(RECONCILIATION_CARD_API, request);
            if (res.isSuccess()) {
                return res;
            }
            i++;
        } while (i < retries && !res.isSuccess() && res.getErrorCode().equals(TIMEOUT_CODE));

        return res;
    }

    public int reconciliationItm(ReconciliationRecord record){
        logger.debug("Begin ReconciliationItm ");

        ReconciliationResponse response = (ReconciliationResponse) srvReconciliationItm(record);
        if (!response.isSuccess()) {
            logger.debug("Exit ReconciliationGiftCard ");
            if (!SscoPosManager.getInstance().isEnabled())
                GdPos.panel.clearLink(errorCodesMap.get(response.getErrorCode()) != null ? errorCodesMap.get(response.getErrorCode()) : response.getErrorCode() + " - " + response.getErrorMessage(), 0x81);
            return Integer.parseInt(response.getErrorCode());
        }
        return 0;
    }

    public ResponseBase srvReconciliationItm(ReconciliationRecord record) {
        int retries = 4;
        int i = 0;
        ReconciliationResponse res = new ReconciliationResponse();
        ReconciliationRequest request = new ReconciliationRequest();
        setBusinessDate();
        request.setBusinessDate(Integer.parseInt(getBusinessDate()));
        request.setMerchantId(props.getProperty("merchantId"));
        request.setReconciliationRecords(record);

        do {
            res = (ReconciliationResponse) doReconciliationTransaction(RECONCILIATION_CARD_API, request);
            if (res.isSuccess()) {
                return res;
            }
            i++;
        } while (i < retries && !res.isSuccess() && res.getErrorCode().equals(TIMEOUT_CODE));

        return res;
    }


    public Response srvReversal(Itemdata itm) {
        ReversalResponse response = new ReversalResponse();
        ReversalRequest request = (ReversalRequest) setReversalRequest(itm);
        response = (ReversalResponse) doTransaction(REVERSAL_API, request, response);
        return response;
    }


    private Response doTransaction(String url, Request request, Response response) {
        String requestSerialized = gson.toJson(request);
        logger.debug("Request = " + requestSerialized);
        JsonElement jsonResponse = post(url, requestSerialized);
        response = gson.fromJson(jsonResponse, response.getClass());
        logger.debug("Response = " + jsonResponse);
        return response;
    }

    private ResponseBase doReconciliationTransaction(String url, ReconciliationRequest request) {
        String requestSerialized = gson.toJson(request);
        ResponseBase response;
        logger.debug("Request = " + requestSerialized);
        JsonElement jsonResponse = post(url, requestSerialized);
        response = gson.fromJson(jsonResponse, ReconciliationResponse.class);
        logger.debug("Response = " + jsonResponse);
        return response;
    }

    private ConfirmTransactionRequest setConfirmTransactionRequest(Itemdata itm) {
        ConfirmTransactionRequest request = new ConfirmTransactionRequest(props.getProperty(MERCHANT_ID_PROPERTY), ctl.reg_nbr + "", ctl.ckr_nbr + "", "");
        request.setReferenceNumber(itm.referenceNumberGC);
        return request;
    }

    private Request setCancelTransactionRequestObject(Itemdata itm) {
        CancelTransactionRequest request = new CancelTransactionRequest(props.getProperty(MERCHANT_ID_PROPERTY), ctl.reg_nbr + "", ctl.ckr_nbr + "", "");
        request.setReferenceNumber(itm.referenceNumberGC);
        return request;
    }

    private Request setReversalRequest(Itemdata itm) {
        ReversalRequest request = new ReversalRequest(props.getProperty(MERCHANT_ID_PROPERTY), ctl.reg_nbr + "", ctl.ckr_nbr + "", "");
        request.setOriginalCashierID(ctl.ckr_nbr + "");
        request.setOriginalMerchantID(props.getProperty(MERCHANT_ID_PROPERTY));
        request.setOriginalTerminalID(ctl.reg_nbr + "");
        request.setOriginalTransNumber(getOriginaltransactionNumber());
        request.setTransactionNumber(getTransactionNumber());
        return request;
    }

    private Request setActivationRequestObject(Itemdata itm) {
        ActivationRequest request = new ActivationRequest(props.getProperty(MERCHANT_ID_PROPERTY), ctl.reg_nbr + "", ctl.ckr_nbr + "", "");
        setTransactionNumber();
        request.setTransactionNumber(getTransactionNumber());
        itm.setOriginalTransactionNumberGC(request.getTransactionNumber());
        request.setCardNumber(itm.giftCardSerial);
        request.setGencode(itm.number.trim());
        request.setAmount(addDecimals(itm.price));
        return request;
    }


    private ReloadRequest setReloadRequestObject(Itemdata itm) {
        ReloadRequest request = new ReloadRequest(props.getProperty(MERCHANT_ID_PROPERTY), ctl.reg_nbr + "", ctl.ckr_nbr + "", "");
        setTransactionNumber();
        request.setTransactionNumber(getTransactionNumber());
        request.setCardNumber(itm.giftCardSerial);
        request.setGencode(itm.number.trim());
        request.setAmount(addDecimals(itm.price));
        return request;
    }

    private RefundRequest setRefundRequestObject(Itemdata itm) {
        RefundRequest request = new RefundRequest(props.getProperty(MERCHANT_ID_PROPERTY), ctl.reg_nbr + "", ctl.ckr_nbr + "", "");
        setTransactionNumber();
        request.setTransactionNumber(getTransactionNumber());
        request.setCardNumber(itm.giftCardSerial);
        request.setGencode(itm.number.trim());
        request.setAmount(Math.abs(addDecimals(itm.amt)));
        return request;
    }

    public Request setRedemptionRequestObject(Itemdata itm) {
        RedemptionRequest request = new RedemptionRequest(props.getProperty(MERCHANT_ID_PROPERTY), ctl.reg_nbr + "", ctl.ckr_nbr + "", "");
        setTransactionNumber();
        request.setTransactionNumber(getTransactionNumber());
        request.setCardNumber(itm.giftCardSerial);
        request.setGencode(itm.number.trim());
        request.setPaymentMean(PAYMENT_MEAN);
        request.setAmount(addDecimals(itm.amt));
        return request;
    }

    public ReconciliationRequest setReconciliationRequestObject() {
        return createReconciliationFile(new ArrayList<ReconciliationRecord>());
    }

    @Override
    public int readSerial32(Itemdata itm) {
        logger.debug("Enter. GiftCardSerial: " + itm.giftCardSerial);

        if (!itm.giftCardSerial.isEmpty()) {
            return 0;
        }
        if (SscoPosManager.getInstance().isEnabled()) {
            logger.debug("Invoking DataNeeded");

            SscoPosManager.getInstance().sendDataNeeded("InsertSerialNumber.GIFTCARD");
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
                    if (isInsertKeyBoardEnabled()) {
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
            itm.giftCardSerial = newInput.pb;
        }

        return 0;
    }

    @Override
    public int confirmAllGiftCard() {
        solds = handleConfirmListItems(solds);
        reloadeds = handleConfirmListItems(reloadeds);
        payments = handleConfirmPaymentsItem(payments);
        refunds = handleConfirmListItems(refunds);

        if( !records.isEmpty()){
            records = handleReciliationListItems(records);
        }
        if (!records.isEmpty()) {
            createReconciliationFile(records);
        }
        return 0;
    }

    private ArrayList<ReconciliationRecord> handleReciliationListItems(ArrayList<ReconciliationRecord> records) {
        ArrayList<ReconciliationRecord> recordsForReconcil = new ArrayList<ReconciliationRecord>(records);
        for(ReconciliationRecord rec : recordsForReconcil){
            int res = reconciliationItm(rec);
            if(res == 0){
                records.remove(rec);
            }
        }
        return records;

    }

    private ArrayList<Itemdata> handleConfirmPaymentsItem(ArrayList<Itemdata> listItems) {
        ArrayList<ReconciliationRecord> recordsForReconcil = new ArrayList<ReconciliationRecord>(records);
        ArrayList<Itemdata> itemsConfirmed = new ArrayList<Itemdata>(listItems);
        for (ReconciliationRecord rec : recordsForReconcil) {
            if (rec.getFinalStatus().equals(FINAL_STATUS_REQUESTED_NOT_CONFIRMED_CANCELED)) {
                for (Itemdata itm : itemsConfirmed) {
                    int res = confirmTransaction(itm);
                    if (res == 0) {
                        rec.setFinalStatus(FINAL_STATUS_REQUESTED_CONFIRMED);
                        for (Itemdata i : itemsConfirmed) {
                            if (i.giftCardSerial.equals(rec.getCardNumber()))
                                continue;
                            if (i.referenceNumberGC.equals(rec.getReferenceNumber()))
                                continue;
                            listItems.remove(i);
                        }
                    } else {
                        prtBlockDwide(ELJRN + 3, gcer_txt, 0, 3);
                        prtLine.book(3);
                        rec.setFinalStatus(FINAL_STATUS_REQUESTED_CANCELED);
                    }
                }
            }
        }
        return listItems;
    }

    private ArrayList<Itemdata> handleConfirmListItems(ArrayList<Itemdata> listItems) {
        ArrayList<ReconciliationRecord> recordsForReconcil = new ArrayList<ReconciliationRecord>(records);
        ArrayList<Itemdata> itemsConfirmed = new ArrayList<Itemdata>(listItems);
        for (ReconciliationRecord rec : recordsForReconcil) {
            if (rec.getFinalStatus().equals(FINAL_STATUS_REQUESTED_NOT_CONFIRMED_CANCELED)) {
                for (Itemdata itm : itemsConfirmed) {
                    int res = confirmTransaction(itm);
                    if (res == 0) {
                        rec.setFinalStatus(FINAL_STATUS_REQUESTED_CONFIRMED);
                        for (Itemdata i : itemsConfirmed) {
                            if (i.giftCardSerial.equals(rec.getCardNumber()) && i.referenceNumberGC.equals(rec.getReferenceNumber())) {
                                listItems.remove(i);
                            }
                        }
                    } else {
                        rec.setFinalStatus(FINAL_STATUS_REQUESTED_CANCELED);
                        prtBlockDwide(ELJRN + 3, gcer_txt, 3, 6);
                        prtLine.book(3);
                        //prtLine.init("Card not activated" + rec.getCardNumber()).book(3);
                    }
                    itm.giftCardSerial = "";
                }
            }
        }
        return listItems;
    }

    private void readAccountNumberList() {
        Set<Object> listKeys = props.keySet();
        for (Object key : listKeys) {
            if (key.toString().startsWith(tenderMaskProps)) {
                int tt = key.toString().indexOf(tenderMaskProps);
                String tnd = key.toString().substring(tenderMaskProps.length());
                String prefix = props.getProperty(key.toString());
                String[] str = prefix.split("\\[");
                accountList.add(new CustomTenderOg(Integer.parseInt(tnd), str[0], Boolean.parseBoolean(str[1]), prefix));
            }
        }
    }

    public JsonElement post(String endPoint, String request) {
        JsonElement res;
        Map<String, String> headers = new HashMap<String, String>();
        try {
            headers.put("Content-Type", "application/json");
            if (props.containsKey("X-WSRG-API-Version")) {
                headers.put("X-WSRG-API-Version", props.getProperty("X-WSRG-API-Version"));
            }
            String url = getProps().getProperty(OLGB_SRV_ENDPOINT, "http://localhost") + endPoint;

            RequestConfig config = RequestConfig.custom()
                    .setConnectTimeout(Integer.parseInt(props.getProperty(TIMEOUT_PROPERTY, "0")))
                    .setConnectionRequestTimeout(Integer.parseInt(props.getProperty(TIMEOUT_PROPERTY, "0")))
                    .setSocketTimeout(Integer.parseInt(props.getProperty(TIMEOUT_PROPERTY, "0"))).build();
            HttpClient httpClient = HttpClients
                    .custom()
                    .setSSLContext(new SSLContextBuilder().loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build())
                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                    .setDefaultRequestConfig(config)
                    .build();
            Unirest.setHttpClient(httpClient);

            HttpResponse<String> response = Unirest.post(url)
                    .basicAuth(getProps().getProperty(USERNAME_PROPERTY, ""), getProps().getProperty(PASSWORD_PROPERTY, ""))
                    .headers(headers)
                    .body(request)
                    .asString();
            if (response.getStatus() == STATUS_SUCCESS) {
                logger.info("Status: " + response.getStatus());
                logger.info("Body response: " + response.getBody());
                res = new JsonParser().parse(response.getBody());
            } else {
                logger.error("Error from server: " + response.getStatus());
                res = new JsonParser().parse(response.getBody());
                logger.error("Error from server: " + res);
                if (res.getAsJsonObject().has(IS_SUCCESS_FIELD) || res.getAsJsonObject().has(IS_SUCCESSFULL_FIELD)) {
                    return new JsonParser().parse(response.getBody());
                } else {
                    return gson.toJsonTree(new ResponseBase(SERVER_ERROR_CODE, SERVER_ERROR_DESC + "", ""));
                }
            }
        } catch (Exception e) {
            logger.error("Error: ", e);
            String codeError = SERVER_ERROR_CODE;

            if (e instanceof UnirestException) {
                if ((e.getCause() instanceof TimeoutException) || (e.getCause() instanceof ConnectTimeoutException) || (e.getCause() instanceof SocketTimeoutException)) {
                    codeError = TIMEOUT_CODE;
                }
            }
            res = gson.toJsonTree(new ResponseBase(codeError, SERVER_ERROR_DESC, ""));
        }
        return res;
    }

    public static void writeReconciliationFile() {
        logger.debug("ENTER");

        try {
            FileWriter reconciliationAppend = new FileWriter("data//transactionsGiftCard.txt", true);
            BufferedWriter bw = new BufferedWriter(reconciliationAppend);
            logger.info("ctl.tran: " + ctl.tran);
            String line = "";
            bw.write(line);
            bw.close();
            reconciliationAppend.close();
        } catch (Exception e) {
            logger.error("Error: ", e);
        }
        logger.debug("EXIT");
    }

    public double addDecimals(double amt) {
        int dec = getDecimalDigits(getTenderId());
        return amt / Math.pow(10, dec);
    }

    public double removeDecimals(double amt) {
        int dec = getDecimalDigits(getTenderId());
        return amt * Math.pow(10, dec);
    }

    //Getter & Setter
    public Properties getProps() {
        return props;
    }

    public void setProps(Properties props) {
        this.props = props;
    }

    public String getTransactionNumber() {
        return transactionNumber;
    }

    public void setTransactionNumber() {
        OglobaPlugin.transactionNumber = "20" + ctl.date + ctl.time;
        setOriginaltransactionNumber();
    }

    public String getOriginaltransactionNumber() {
        return originaltransactionNumber;
    }

    public void setOriginaltransactionNumber() {
        OglobaPlugin.originaltransactionNumber = getTransactionNumber();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getBusinessDate() {
        return businessDate;
    }

    public void setBusinessDate() {
        OglobaPlugin.businessDate = "20" + ctl.date;
    }

    public int getDecimalDigits(String tenderId) {
        for (TndMedia tender : tnd) {
            if (tenderId.charAt(0) == tender.getType()) {
                decimalDigits = tender.dec;
                break;
            }
        }
        return decimalDigits;
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
            int ret = cancelTransaction(payment, TRANSACTION_TYPE_REDEMPTION);
            if (ret == 0) {
                payments.remove(payment);
                itm.utilityEnglishText = payment.utilityEnglishText;
                itm.gCardDsc = payment.gCardDsc;
                return 0;
            }
            return ret;
        }
        return 0;
    }

    @Override
    public int cancelRedemption(Itemdata itm) {
        return 0;
    }

    @Override
    public boolean isGiftCard(Itemdata itm) {
        return OLGB_TENDER_ID.equals(itm.gCard + "");
    }

    @Override
    public String getTenderId() {
        return OLGB_TENDER_ID;
    }

    public boolean isInsertKeyBoardEnabled() {
        return "true".equalsIgnoreCase(props.getProperty("keyboard-enabled", "false"));
    }

    @Override
    public void resetAll() {
        solds.clear();
        reloadeds.clear();
        payments.clear();
        records.clear();
    }

    private String setFilepath() {
        setBusinessDate();
        String filePath = props.getProperty(RECONCILIATION_FILE_PATH);
        String fileName = props.getProperty(RECONCILIATION_FILE_NAME_MASK).replace("merchantId", props.getProperty(MERCHANT_ID_PROPERTY))
                .replace("YYYYMMDD", getBusinessDate());
        return filePath.concat(fileName);
    }

    private ReconciliationRequest getReconciliaitionFile() {
        ReconciliationRequest reconcilReq = new ReconciliationRequest();
        try {
            reconcilReq = gson.fromJson(new FileReader(new File(setFilepath())), ReconciliationRequest.class);

        } catch (Exception e) {
            logger.error("Reconciliation file doesn't exist");
        }
        return reconcilReq;
    }

    private ReconciliationRequest createReconciliationFile(ArrayList<ReconciliationRecord> records) {
        String fileName = setFilepath();
        logger.debug("Reconciliation file = " + fileName);
        FileWriter writer;
        ReconciliationRequest reconcilReq = new ReconciliationRequest();
        try {
            File reconciliationFile = new File(fileName);
            if (!reconciliationFile.exists()) {
                ReconciliationRequest initRequest = new ReconciliationRequest();
                initRequest.setBusinessDate(Integer.parseInt(getBusinessDate()));
                initRequest.setMerchantId(props.getProperty("merchantId"));
                writer = new FileWriter(reconciliationFile, true);
                gson.toJson(initRequest, writer);
                writer.close();
            }

            reconcilReq = getReconciliaitionFile();

            if (records != null && records.size() > 0) {
                ArrayList<ReconciliationRecord> recordsForReconcil = new ArrayList<ReconciliationRecord>(reconcilReq.getReconciliationRecords());
                for (ReconciliationRecord record : records) {
                    boolean exist = false;
                    for (ReconciliationRecord rec : recordsForReconcil) {
                        if (record.getTransactionNumber().trim().equals(rec.getTransactionNumber().trim()))
                            continue;
                        if (record.getCardNumber().trim().equals(rec.getCardNumber().trim()))
                            continue;
                        if (record.getReferenceNumber().trim().equals(rec.getReferenceNumber().trim()))
                            continue;
                        exist = true;
                    }
                    if (!exist) {
                        reconcilReq.setReconciliationRecords(record);
                    }
                }
                writer = new FileWriter(reconciliationFile);
                gson.toJson(reconcilReq, writer);
                writer.close();
            }
            return reconcilReq;


        } catch (Exception e) {
            logger.error("Error Creating Reconciliation file");
        }
        return reconcilReq;
    }

    @Override
    public void printSummaries(String type) {
        logger.debug("print-gift-cards." + type + " = " + props.getProperty("print-gift-cards." + type));
        if ("true".equalsIgnoreCase(props.getProperty("print-gift-cards." + type))) printGiftCardPayments(type);
    }

    public void printGiftCardPayments(String type) {
        for (Itemdata payment : payments) {
            printGiftCardPayment(payment.copy(), type);
        }
    }

    public void printGiftCardPayment(Itemdata item, String type) {
        logger.debug("Enter");

        prtLine.init(' ').type(2);
        prtLine.init(tnd[item.tnd].tx20);
        prtLine.onto(20, tnd[itm.tnd].symbol).upto(40, editMoney(item.tnd, item.amt));
        prtLine.push(item.mark).type(2);
        prtLine.init(mask(item.giftCardSerial)).type(2);
        if (SAME_RECEIPT_TYPE.equals(type)) {
            Struc.prtLine.init(' ').book(3);
        } else {
            newReceipt();
        }

        logger.debug("Exit");
    }

    private void newReceipt() {
        GdRegis.set_trailer();
        Struc.prtLine.type(2);
        GdRegis.hdr_print();
    }

    private String mask(String serial) {
        if (serial == null || serial.isEmpty()) return "";
        return serial.substring(0, serial.length() - 2) + "**";
    }

    public String checkGiftCardSerial(String serial) {
        for (CustomTender cs : accountList) {
            String patternSt = ((CustomTenderOg) cs).getPattern();
            logger.debug("Pattern: " + patternSt);
            if (patternSt != null) {
                if (serial.matches(patternSt)) {
                    return serial.substring(0, 3);
                }
            }
        }
        return "";
    }

    public String checkGiftCardSerial(Itemdata itm) {
        String serial = itm.giftCardSerial;
        logger.debug("Serial: " + itm.giftCardSerial);
        String result = checkGiftCardSerial(serial);
        if (!result.isEmpty()) itm.giftCardSerial = itm.giftCardSerial.substring(3);
        return result;
    }
}