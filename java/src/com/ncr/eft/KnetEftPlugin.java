package com.ncr.eft;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ncr.*;
import com.ncr.eft.data.knet.requests.EspAdmin;
import com.ncr.eft.data.knet.requests.EspInterface;
import com.ncr.eft.data.knet.requests.EspRegister;
import com.ncr.eft.data.knet.requests.EspTransaction;
import com.ncr.eft.data.knet.responses.all.*;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;


public class KnetEftPlugin extends GenericEftPlugin {
    private static final Logger logger = Logger.getLogger(KnetEftPlugin.class);
    private static final String TRANS_ACTION_INIT = "INIT";
    private static final String TRANS_ACTION_CLOSE = "CLOSE";
    private static final String TRANS_ACTION_PURCHASE = "PURCHASE";
    private static String KNET_IP_PROP = "knet.tcp.ip";
    private static String KNET_PORT_PROP = "knet.tcp.port";
    private static String KNET_TIMEOUT_PROP = "knet.timeout";
    private static String KNET_CLOSE_RETRIES_PROP = "knet.close.retries";
    private static String KNET_TERMINAL_ID_PROP = "knet.terminalId";
    private static int KNET_TIMEOUT = 10;
    private static int KNET_PORT = 0;
    private static String KNET_IP = "";
    private static String KNET_TERMINAL_ID = "";
    private static int KNET_CLOSE_RETRIES;
    private static final String REGISTER_EVENT_ENABLED_PROP = "knet.register.enabled";
    private static final String REGISTER_EVENT_TYPE_PROP = "knet.register.event.type";
    private static final String REGISTER_EVENT_ID_PROP = "knet.register.event.id";
    private static final String REGISTER_EVENT_ACTION_DEBUG_ALL_ENABLED = "knet.register.event.DebugAll";
    private static final String SIGNATURE_LINE = "knet.signature.line";
    private static final String ERROR_MESSAGES = "knet.error-message.";
    private static final String PAN_ENTRY_MODES = "knet.pan-entry-mode.";
    private static final String REGISTER_EVENT_EVENT_TYPE = "EVENT";
    private static String REGISTER_EVENT_ACTION_DEBUG_ALL = "DEBUG_ALL";
    private static final String APPROVED_RESPONSE_CODE = "00";
    private static final String DECLINED_RESPONSE_CODE = "05";
    private static final String TIMEOUT_RESPONSE_CDOE = "91";
    private static final String IINCORRECT_PIN_RESPONSE_CODE = "55";
    private static final String APPROVED_RESPONSE = "APPROVE";

    private static final String SIGNATURE = "$SIGNATURE$";
    private static final String SIGNATURE_FEED = "$SIGNATURE_FEED$";
    private static final String TRANSACTION_ID = "$TRANS_ID$";
    private static final String TYPE = "$TYPE$";
    private static final String SEQUENCE_NUMBER = "$SEQUENCE_NO$";
    private static final String PAN_ENTRY_MODE = "$PAN_MODE$";
    private static final String ACTION_CODE = "$ACTION_CODE$";
    private static final String PRODUCT_NAME = "$PRODUCT_NAME$";
    private static final String LOCAL_DATE = "$LOCAL_DATE$";
    private static final String LOCAL_TIME = "$LOCAL_TIME$";
    private static final String TRANSACTION_DATE = "$TRANS_DATE$";
    private static final String APPLICATION_IDENTIFIER = "$APP_IDENT$";
    private static final String ERROR = "$ERROR$";

    private static boolean isInitialized = false;
    private static boolean registerEvent = false;
    private static boolean registerEventDebugAllEnabled = false;
    private static String terminalId;
    private List<String> approvedCodes = Arrays.asList(new String[]{"00"});
    private boolean testEnvironment = false;
    private static boolean settlementEnabledAtEod = false;
    private boolean signatureRequired;
    private String signature;
    private String error;
    private Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();
    private int timeout;
    private Socket clientSocket;
    private DataOutputStream out;
    private InputStreamReader in;
    private static final String ERRORTAG = "<Esp:Error";
    private String transactionId;
    private String type;
    private String sequenceNumber;
    private String panEntryMode;
    private String actionCode;
    private String productName;
    private String localDate;
    private String localTime;
    private String transactionDate;
    private String applicationIdentifier;
    private String responseCode;
    private Properties props;
    private long internal;

    public KnetEftPlugin() {
        terminalId = editNum(ctl.reg_nbr, 8);
    }

    public void loadEftTerminalParams(int line, String txt) {
        super.loadEftTerminalParams(line, txt);
        //TODO: Add specific parameters
        switch (line) {
            case 0:
                KNET_TIMEOUT = timeout = Integer.parseInt(txt.substring(3, 8).trim()) * 1000;
                logger.debug("Timeout: " + KNET_TIMEOUT);
                KNET_PORT = Integer.parseInt(txt.substring(9, 14).trim());
                settlementEnabledAtEod = txt.substring(38, 39).equals("1");
                testEnvironment = txt.substring(39, 40).equals("1");
                logger.debug("Params read: " + KNET_PORT);
                break;
            case 1:
                logger.debug("EFTU1: " + line);
                break;
            default:
                break;
        }
    }

    @Override
    public void init(Properties props) {
        logger.info("ENTER");
        this.props = props;
        KNET_IP = props.getProperty(KNET_IP_PROP, "127.0.0.1");
        KNET_CLOSE_RETRIES = Integer.parseInt(props.getProperty(KNET_CLOSE_RETRIES_PROP, "3"));
        registerEvent = Boolean.parseBoolean(props.getProperty(REGISTER_EVENT_ENABLED_PROP));
        registerEventDebugAllEnabled = Boolean.parseBoolean(props.getProperty(REGISTER_EVENT_ACTION_DEBUG_ALL_ENABLED));
        terminalId = KNET_TERMINAL_ID = props.getProperty(KNET_TERMINAL_ID_PROP, terminalId);
        signature = props.getProperty(SIGNATURE_LINE, "_ _ _ _ _ _ _ _ _ _");
        EspBasicResponse response = null;
        try {

            EspAdmin espAdmin = new EspAdmin(KNET_TERMINAL_ID, TRANS_ACTION_INIT);
            if (registerEvent) {
                if (registerEventDebugAllEnabled) {
                    espAdmin.getEspRegister().add(new EspRegister(REGISTER_EVENT_EVENT_TYPE, REGISTER_EVENT_ACTION_DEBUG_ALL));
                }
                EspRegister espReg = new EspRegister(REGISTER_EVENT_TYPE_PROP, REGISTER_EVENT_ID_PROP);
                espAdmin.getEspRegister().add(espReg);
            }
            EspInterface espInterface = new EspInterface();
            espInterface.getEspRegisters().add(espAdmin);
            StringWriter request = new StringWriter();
            JAXB.marshal(espInterface, request);
            sendRequest(KNET_IP, KNET_PORT, request.toString());
            response = receiveResponse();
            if (response instanceof EspAdminResponse) {
                if (((EspAdminResponse) response).getActionCode().equals(APPROVED_RESPONSE)) {
                    logger.info("KNET Init success");
                    isInitialized = true;
                }
            } else {
                if (response instanceof EspErrorResponse) {
                    logger.error("KNET Init failed with code: " + ((EspErrorResponse) response).getMessageReasonCode());
                }
            }
        } catch (Exception e) {
            logger.error("Error Initialize Knet plugin : ", e);
        } finally {
            if (clientSocket != null && clientSocket.isConnected()) {
                stopConnection();
                logger.debug("Socket Closed Correctly");
            }
        }
        logger.info("EXIT");
    }

    private EspBasicResponse checkResponse(String msg) throws JAXBException {
        EspInterfaceResponse response = null;
        String responseString = msg.substring(2);
        StringReader reader = new StringReader(responseString);
        JAXBContext jaxbContext = JAXBContext.newInstance(EspInterfaceResponse.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        //unmarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        response = (EspInterfaceResponse) unmarshaller.unmarshal(reader);
        logger.debug("Response returned from eSocket POS :" + responseString);
        return response.getEspElement();
    }

    public void startConnection(String ip, int port) throws IOException {
        logger.info("StartConnection" + " IP : " + ip + " port " + port + " timeout = " + KNET_TIMEOUT);
        clientSocket = new Socket(ip, port);
        logger.info("Socket Client connection connected");
        clientSocket.setSoTimeout(KNET_TIMEOUT);
        out = new DataOutputStream(clientSocket.getOutputStream());
        in = new InputStreamReader(clientSocket.getInputStream());
    }

    public void sendMessage(String msg) throws IOException {
        logger.debug("Request sent to eSocket POS: " + msg);
        byte[] result = getLengthRequest(msg);
        out.write(result);
    }

    public void sendRequest(String ip, int port, String request) throws IOException {
        String xml = "";
        xml = request.toString();
        startConnection(ip, port);
        sendMessage(xml);
    }

    public EspBasicResponse receiveResponse() throws IOException, JAXBException {
        EspBasicResponse response = null;
        String res = receiveMessage();
        logger.info("Esocket POS returns : " + res);
        response = checkResponse(res);
        return response;
    }

    public String receiveMessage() throws IOException {
        byte[] resultBuff = new byte[0];
        byte[] buff = new byte[1024];
        int k = -1;
        String app = "";
        String endResponse = "</Esp:Interface>";

        StringBuilder sb = new StringBuilder();
        while ((k = clientSocket.getInputStream().read(buff, 0, buff.length)) > -1) {
            byte[] tbuff = new byte[resultBuff.length + k]; // temp buffer size = bytes already read + bytes last read
            System.arraycopy(resultBuff, 0, tbuff, 0, resultBuff.length); // copy previous bytes
            System.arraycopy(buff, 0, tbuff, resultBuff.length, k);  // copy current lot
            resultBuff = tbuff; // call the temp buffer as your result buff
            app = new String(resultBuff, "UTF-8");
            sb.append(app);
            if (sb.toString().contains(endResponse)) break;
        }
        String ss = new String(resultBuff, "UTF-8");
        return ss;
    }

    public void stopConnection() {
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (Exception ie) {
            logger.error("Error during closing socket" + ie);
        }
    }

    @Override
    public int pay(Itemdata itm, Terminal ctl, LinIo line) {
        logger.info("ENTER");
        int sts = ERR_RESPONSE;
        EspBasicResponse response = null;

        try {
            logger.debug("KNET EFT is initialzed: " + isInitialized);
            if (false && !isInitialized) {
                return ERR_NOT_INITiALIZED;
            } else {
                EspTransaction espTransaction = new EspTransaction();
                espTransaction.setTerminalId(terminalId);
                espTransaction.setType(TRANS_ACTION_PURCHASE);
                espTransaction.setTransactionAmount("" + itm.pos);
                String transactionId = "" + SequenceGenerator.getInstance().getNext(this.getClass().getSimpleName(), 100000, 999999);
                logger.debug("TransactionId: " + transactionId);
                espTransaction.setTransactionId(transactionId);
                espTransaction.setTransactionId("9" + editNum(ctl.tran, 4) + internal++ % 10);
                EspInterface espInterface = new EspInterface();
                espInterface.getEspRegisters().add(espTransaction);
                StringWriter request = new StringWriter();
                JAXB.marshal(espInterface, request);

                if (request != null) {
                    sendRequest(KNET_IP, KNET_PORT, request.toString());
                    response = receiveResponse();
                    while (response instanceof EspEventResponse) {
                        if (response instanceof EspTransactionResponse) {
                            break;
                        }
                        panel.clearLink(((EspEventResponse) response).getEventId(), 0x80);
                        response = receiveResponse();
                    }

                    if (response instanceof EspTransactionResponse) {
                        EspTransactionResponse espTransactionResponse = (EspTransactionResponse) response;
                        setVariables(espTransactionResponse);
                        if (espTransactionResponse.getActionCode().equals(APPROVED_RESPONSE) && espTransactionResponse.getResponseCode().trim().equals(APPROVED_RESPONSE_CODE)) {
                            logger.info("Transaction approved");
                            addReceiptValues();
                            Itmdc.IDC_write('z', Struc.tra.tnd, 0, authorizationCode, 1, authorizedAmount);
                            sts = ERR_OK;
                        } else {
                            String resp = espTransactionResponse.getResponseCode();
                            if (resp.equals(TIMEOUT_RESPONSE_CDOE)) {
                                sts = ERR_TIMEOUTTRANSACTION;
                            }
                            if (resp.equals(DECLINED_RESPONSE_CODE)) {
                                sts = ERR_DECLINED;
                            }
                            if (resp.equals(IINCORRECT_PIN_RESPONSE_CODE)) {
                                sts = ERR_INCORRECT_PIN;
                            }
                            error = Mnemo.getInfo(sts);
                            addErrorReceiptValues();
                        }
                    } else {
                        logger.error("KNET Init failed with code: " + ((EspAdminResponse) response).getMessageReasonCode());
                        return ERR_RESPONSE;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error: ", e);
            return ERR_RESPONSE;
        } finally {
            if (clientSocket != null && clientSocket.isConnected()) {
                stopConnection();
                logger.debug("Socket Closed Correctly");
            }
        }
        logger.info("EXIT. status: " + sts);
        return sts;
    }

    private void setVariables(EspTransactionResponse espTransactionResponse) {
        authorizationCode = espTransactionResponse.getAuthorizationNumber();
        cardType = espTransactionResponse.getEmvApplicationLabel();
        cardNumber = espTransactionResponse.getCardNumber();
        terminalId = espTransactionResponse.getTerminalId();
        try {
            authorizedAmount = Long.parseLong(espTransactionResponse.getEmvAmount());
        } catch (Exception e) {
            logger.error("Error parsing amount: ", e);
            authorizedAmount = itm.pos;
        }
        signatureRequired = "true".equalsIgnoreCase(espTransactionResponse.getSignatureRequired());
        rrn = espTransactionResponse.getRetrievalRefNr();
        mid = espTransactionResponse.getMerchantId();
        transactionId = espTransactionResponse.getTransactionId();
        type = espTransactionResponse.getType();
        sequenceNumber = espTransactionResponse.getRetrievalRefNr();
        panEntryMode = espTransactionResponse.getPanEntryMode();
        actionCode = espTransactionResponse.getActionCode();
        productName = espTransactionResponse.getCardProductName();
        localDate = espTransactionResponse.getLocalDate();
        localTime = espTransactionResponse.getLocalTime();
        transactionDate = espTransactionResponse.getEmvTransactionDate();
        applicationIdentifier = espTransactionResponse.getEmvApplicationIdentifier();
        responseCode = espTransactionResponse.getResponseCode();
    }

    @Override
    public int voidPayment(int spec, String input) {
        return 0;
    }

    @Override
    public boolean settle() {
        return false;
    }

    @Override
    public String getTenderId() {
        return KNET_TENDER_ID;
    }

    @Override
    public boolean isVoidCapable() {
        return true;
    }

    @Override
    public boolean isSettlementCapable() {
        return true;
    }

    @Override
    public boolean isSettlementEnabledAtEod() {
        return settlementEnabledAtEod;
    }

    private byte[] getLengthRequest(String msg) {
        byte[] data;
        byte[] result;
        int ln = msg.getBytes().length;
        byte[] header;
        data = msg.getBytes();

        if (ln < 65535) {
            int a = ln / 256;
            int b = ln % 256;
            header = new byte[2];
            header[0] = (byte) a;
            header[1] = (byte) b;
            result = new byte[header.length + data.length];
            result = ArrayUtils.addAll(header, data);

        } else {
            byte[] header1 = new byte[2];
            header1[0] = (byte) 0xFF;
            header1[1] = (byte) 0xFF;
            byte[] header2 = new byte[4];
            header2 = ByteBuffer.allocate(4).order(ByteOrder.nativeOrder()).putInt(ln).array();
            header = ArrayUtils.addAll(header1, header2);
            result = new byte[6 + data.length];
            result = ArrayUtils.addAll(header, data);
        }

        return result;
    }

    @Override
    public void stop(Terminal ctl) {
        logger.info("ENTER");

        EspBasicResponse response = null;
        String res = null;
        boolean closeApproved = false;
        int closeRetries = 0;
        try {
            EspAdmin espAdmin = new EspAdmin(terminalId, TRANS_ACTION_CLOSE);
            EspInterface espInterface = new EspInterface();
            espInterface.getEspRegisters().add(espAdmin);
            StringWriter request = new StringWriter();
            JAXB.marshal(espInterface, request);

            do {
                sendRequest(KNET_IP, 25000, request.toString());
                response = receiveResponse();
                closeRetries++;
                if (response instanceof EspAdminResponse) {
                    if (((EspAdminResponse) response).getActionCode().equals(APPROVED_RESPONSE)) {
                        closeApproved = true;
                        break;
                    }
                }
            } while (closeRetries > KNET_CLOSE_RETRIES);
            if (!closeApproved) {
                if (response instanceof EspErrorResponse) {
                    logger.error("KNET Init failed with code: " + ((EspErrorResponse) response).getMessageReasonCode());
                }
            }
        } catch (Exception e) {
            logger.error("Error Close Active session : ", e);
        }
        logger.info("EXIT");
    }

    @Override
    public String managePluginMacro(String line) {
        if (line.indexOf(SIGNATURE) >= 0) {
            line = signatureRequired ? signature : SKIP_LINE;
        } else if (line.indexOf(SIGNATURE_FEED) >= 0) {
            line = signatureRequired ? " " : SKIP_LINE;
        } else if (line.indexOf(TRANSACTION_ID) >= 0) {
            line = line.substring(0, line.indexOf(TRANSACTION_ID)) + transactionId
                    + line.substring(line.indexOf(TRANSACTION_ID) + TRANSACTION_ID.length());
        } else if (line.indexOf(TYPE) >= 0) {
            line = line.substring(0, line.indexOf(TYPE)) + type
                    + line.substring(line.indexOf(TYPE) + TYPE.length());
        } else if (line.indexOf(SEQUENCE_NUMBER) >= 0) {
            line = line.substring(0, line.indexOf(SEQUENCE_NUMBER)) + sequenceNumber
                    + line.substring(line.indexOf(SEQUENCE_NUMBER) + SEQUENCE_NUMBER.length());
        } else if (line.indexOf(PAN_ENTRY_MODE) >= 0) {
            line = line.substring(0, line.indexOf(PAN_ENTRY_MODE)) + getPanEntryModeDescription(panEntryMode)
                    + line.substring(line.indexOf(PAN_ENTRY_MODE) + PAN_ENTRY_MODE.length());
        } else if (line.indexOf(ACTION_CODE) >= 0) {
            line = line.substring(0, line.indexOf(ACTION_CODE)) + actionCode
                    + line.substring(line.indexOf(ACTION_CODE) + ACTION_CODE.length());
        } else if (line.indexOf(PRODUCT_NAME) >= 0) {
            line = line.substring(0, line.indexOf(PRODUCT_NAME)) + productName
                    + line.substring(line.indexOf(PRODUCT_NAME) + PRODUCT_NAME.length());
        } else if (line.indexOf(LOCAL_DATE) >= 0) {
            line = line.substring(0, line.indexOf(LOCAL_DATE)) + localDate
                    + line.substring(line.indexOf(LOCAL_DATE) + LOCAL_DATE.length());
        } else if (line.indexOf(LOCAL_TIME) >= 0) {
            line = line.substring(0, line.indexOf(LOCAL_TIME)) + localTime
                    + line.substring(line.indexOf(LOCAL_TIME) + LOCAL_TIME.length());
        } else if (line.indexOf(TRANSACTION_DATE) >= 0) {
            line = line.substring(0, line.indexOf(TRANSACTION_DATE)) + transactionDate
                    + line.substring(line.indexOf(TRANSACTION_DATE) + TRANSACTION_DATE.length());
        } else if (line.indexOf(APPLICATION_IDENTIFIER) >= 0) {
            line = line.substring(0, line.indexOf(APPLICATION_IDENTIFIER)) + applicationIdentifier
                    + line.substring(line.indexOf(APPLICATION_IDENTIFIER) + APPLICATION_IDENTIFIER.length());
        } else if (line.indexOf(ERROR) >= 0) {
            line = line.substring(0, line.indexOf(ERROR)) + getErrorDescription(responseCode)
                    + line.substring(line.indexOf(ERROR) + ERROR.length());
        }
        return line;
    }

    private String getErrorDescription(String responseCode) {
        return props.getProperty(ERROR_MESSAGES + responseCode, Mnemo.getInfo(ERR_DECLINED));
    }

    private String getPanEntryModeDescription(String panEntryMode) {
        return props.getProperty(PAN_ENTRY_MODES + panEntryMode, panEntryMode);
    }
}
