package com.ncr.eft;

import apexserialecr.*;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ncr.*;

import com.ncr.eft.data.ComHandshake;
import com.ncr.eft.data.ComParity;
import com.ncr.eft.data.ComStopBits;
import com.ncr.eft.data.apex.SaleResponse;
import com.ncr.eft.data.apex.SettlementResponse;
import com.ncr.eft.data.apex.VoidResponse;
import net.sf.jni4net.Bridge;
import org.apache.log4j.Logger;
import system.Enum;
import system.Type;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class ApexEftPlugin extends GenericEftPlugin {
    public static final String MSG = "$MSG$";
    public static final String SERIAL = "$SERIAL$";
    public static final String HOST_MID = "$HOST_MID$";
    public static final String BATCH_NUM = "$BATCH_NUM$";
    public static final String CLIENT_NAME = "$CLIENT_NAME$";
    public static final String AID = "$AID$";
    public static final String TVR = "$TVR$";
    public static final String APP_NAME = "$APP_NAME$";

    private static final Logger logger = Logger.getLogger(ApexEftPlugin.class);
    private Interface ecrInterface;
    private List<String> approvedCodes = Arrays.asList(new String[]{"00"});
    private boolean testEnvironment = false;
    private static boolean settlementEnabledAtEod = false;
    private Gson gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create();
    private String voidReceiptNumberECR = "";
    private String voidAmount = "";
    private String acquirer;
    private int timeout;
    private String decimalSeparator;
    private ComPortSettings cps;
    private EcrConfig ecr;
    private PrinterConfig pcf;

    private String msg = "";
    private String serial = "";
    private String hostMid  = "";
    private String batchNum = "";
    private String clientName = "";
    private String aid = "";
    private String tvr = "";
    private String appName = "";

    public ApexEftPlugin() {
        try {
            Bridge.init();
            Bridge.LoadAndRegisterAssemblyFrom(new java.io.File("lib/ApexSerialEcr.j4n.dll"));
        } catch (IOException e) {
            logger.error("Error initializing DLL", e);
        }
        cps = new ComPortSettings();
        ecr = new EcrConfig();
        pcf = new PrinterConfig();
    }

    public void loadEftTerminalParams(int line, String txt) {
        super.loadEftTerminalParams(line, txt);

        switch (line) {
            case 0:
                timeout = Integer.parseInt(txt.substring(2, 8).trim());
                logger.debug("Timeout: " + timeout);
                String comPort = txt.substring(9, 14).trim();
                int comBaud = Integer.parseInt(txt.substring(15, 21).trim());
                int comParity = Integer.parseInt(txt.substring(22, 23));
                int comDataBits = Integer.parseInt(txt.substring(24, 25));
                int comStopBits = Integer.parseInt(txt.substring(26, 27));
                int comHandshake = Integer.parseInt(txt.substring(28, 29));
                settlementEnabledAtEod = txt.substring(38, 39).equals("1");
                testEnvironment = txt.substring(39, 40).equals("1");
                logger.debug("Params read: " + comPort + "|" + comBaud + "|" + comParity + "|" + comDataBits + "|" + comStopBits + "|" + comHandshake);

                cps.setComPortName(comPort);
                logger.debug("COM port: " + cps.getComPortName());
                cps.setBaudRate(comBaud);
                logger.debug("Baud rate: " + cps.getBaudRate());
                Type type = cps.getParity().GetType();
                cps.setParity((Enum)Enum.Parse(type, ComParity.valueOfLabel(comParity).toString()));
                logger.debug("Parity: " + cps.getParity());
                cps.setDataBits(comDataBits);
                logger.debug("Data bits: " + cps.getDataBits());
                type = cps.getStopBits().GetType();
                cps.setStopBits((Enum)Enum.Parse(type, ComStopBits.valueOfLabel(comStopBits).toString()));
                logger.debug("Stop bits: " + cps.getStopBits());
                type = cps.getHandshake().GetType();
                cps.setHandshake((Enum)Enum.Parse(type, ComHandshake.valueOfLabel(comHandshake).toString()));
                logger.debug("Handshake: " + cps.getHandshake());
                break;
            case 3:
                ecr.setMid(txt.substring(0, 15).trim());
                logger.debug("MID: " + ecr.getMid());
                ecr.setTid(txt.substring(16, 24).trim());
                logger.debug("TID: " + ecr.getTid());
                ecr.setEcrCurrencyCode(txt.substring(25, 28).trim());
                logger.debug("Currency code: " + ecr.getEcrCurrencyCode());
                acquirer = txt.substring(29).trim();
                logger.debug("Acquirer: " + acquirer);
                break;
            case 4:
                ecr.setMerchantSecureKey(txt.trim());
                logger.debug("Merchant secure key: " + ecr.getMerchantSecureKey());
                break;
            case 5:
                ecr.setEcrTillerUserName(txt.trim());
                logger.debug("Tiller user name: " + ecr.getEcrTillerUserName());
                break;
            case 6:
                ecr.setEcrTillerFullName(txt.trim());
                logger.debug("Tiller full name: " + ecr.getEcrTillerFullName());
                break;
            case 7:
                String stationName = txt.trim().length() > 0 ? txt.trim() : editKey(ctl.reg_nbr, 3);
                ecr.setEcrStationName(stationName);
                logger.debug("Station name: " + ecr.getEcrStationName());
                break;
            default:
                break;
        }
    }

    @Override
    public void init(Properties props) {
        loadProperties(props);
        logger.debug("COM port: " + cps.getComPortName());
        ecrInterface = new Interface(cps, ecr, pcf);
    }

    private void loadProperties(Properties props) {
        ecr.setMid(props.getProperty("mid", ecr.getMid()));
        logger.debug("MID: " + ecr.getMid());
        ecr.setTid(props.getProperty("tid", ecr.getTid()));
        logger.debug("TID: " + ecr.getTid());
        ecr.setEcrCurrencyCode(props.getProperty("currency-code", ecr.getEcrCurrencyCode()));
        logger.debug("Currency code: " + ecr.getEcrCurrencyCode());
        acquirer = props.getProperty("acquirer", "default");
        logger.debug("Acquirer: " + acquirer);
        ecr.setMerchantSecureKey(props.getProperty("merchant.secure-key", ecr.getMerchantSecureKey()));
        logger.debug("Merchant secure key: " + ecr.getMerchantSecureKey());
        ecr.setEcrTillerUserName(props.getProperty("tiller.user-name", ecr.getEcrTillerUserName()));
        logger.debug("Tiller user name: " + ecr.getEcrTillerUserName());
        ecr.setEcrTillerFullName(props.getProperty("tiller.full-name", ecr.getEcrTillerFullName()));
        logger.debug("Tiller full name: " + ecr.getEcrTillerFullName());
        String stationName = props.getProperty("station-name", "");
        stationName = stationName.length() > 0 ? stationName : editKey(ctl.reg_nbr, 3);
        ecr.setEcrStationName(stationName);
        logger.debug("Station name: " + ecr.getEcrStationName());
        decimalSeparator = props.getProperty("decimal-separator", ".");
    }

    @Override
    public int pay(Itemdata itm, Terminal ctl, LinIo line) {
        int sts = ERR_OK;

        try {
            if (testEnvironment) {
                authorizationCode = "111";
                cardType = "VISA";
                cardNumber = "111222333444";
                terminalId ="111";
                authorizedAmount = 200;
                receiptNumber = "222";
                rrn = "333";
                msg = "444";
                hostMid = "555";
                batchNum = "666";
                clientName = "test";
                serial = "1234567890";
                aid = "AID";
                tvr = "TVR";
                appName = "APP";
                eftVoid = false;

                addReceiptValues();

                Itmdc.IDC_write('z', Struc.tra.tnd, 0, authorizationCode, 1, authorizedAmount);
                sts = ERR_OK;
                return sts;
            }
            boolean res = ecrInterface.Open();
            logger.debug("Open result: " + res);
            if (!res) {
                String error = ecrInterface.GetLastError();
                logger.warn("Error opening connection: " + error);
                return ERR_RESPONSE;
            } else {
                String amount = editDec(itm.pos, tnd[0].dec).replace('.', decimalSeparator.charAt(0));
                logger.debug("Executing sale. Amount: " + amount + " timeout: " + timeout);
                Object ret = ecrInterface.Sale(amount, editKey(ctl.reg_nbr, 3), ctl.uniqueId, timeout);
                EcrRsp_Sale response = Bridge.cast(ret, EcrRsp_Sale.class);
                if (response != null) {
                    logger.debug("Response JSON: " + response.Serialize());
                    SaleResponse saleResponse = gson.fromJson(response.Serialize(), SaleResponse.class);

                    if (approvedCodes.contains(saleResponse.getCode())) {
                        logger.info("Transaction approved");

                        authorizationCode = response.getauthCode();
                        cardType = response.getissuerName();
                        cardNumber = response.getpan();
                        terminalId = response.gethostTid();
                        authorizedAmount = itm.pos;
                        receiptNumber = response.getinvoice();
                        rrn = response.getrrn();
                        msg = saleResponse.getMsg();
                        hostMid = saleResponse.getHostMid();
                        batchNum = saleResponse.getBatchNum();
                        clientName = saleResponse.getClientName();
                        serial = saleResponse.getSerial();
                        eftVoid = false;
                        if (saleResponse.getEmv() != null) {
                            aid = saleResponse.getEmv().getAid();
                            tvr = saleResponse.getEmv().getTvr();
                            appName = saleResponse.getEmv().getAppName();
                        }

                        if (msg == null) {
                            String jsonData = saleResponse.getJsonData();
                            SaleResponse innerSaleResponse = gson.fromJson(jsonData, SaleResponse.class);
                            msg = innerSaleResponse.getMsg();
                        }
                        addReceiptValues();

                        Itmdc.IDC_write('z', Struc.tra.tnd, 0, authorizationCode, 1, authorizedAmount);
                        sts = ERR_OK;
                    } else {
                        sts = ERR_NOTAUTHORIZED;
                    }
                } else {
                    sts = ERR_NOTCONNECTED;
                }
            }
        } catch (Exception e) {
            logger.error("Error: ", e);
            return ERR_RESPONSE;
        } finally {
            ecrInterface.Close();
        }

        return sts;
    }

    @Override
    public int voidPayment(int spec, String input) {
        int sts = ERR_OK;

        switch (spec) {
             case 2:
                logger.info("Void amount: " + input);
                voidAmount = input;
                break;
            case 3:
                try {
                    boolean res = ecrInterface.Open();
                    logger.debug("Open result: " + res);
                    if (!res) {
                        String error = ecrInterface.GetLastError();
                        logger.warn("Error opening connection: " + error);
                        return ERR_RESPONSE;
                    } else {
                        logger.info("ctl.tran: " + ctl.tran);
                        logger.info("Void receipt number: " + input);
                        voidReceiptNumberECR = input;

                        if (testEnvironment) {
                            authorizationCode = "111";
                            cardType = "VISA";
                            cardNumber = "111222333444";
                            terminalId ="111";
                            authorizedAmount = 200;
                            receiptNumber = "222";
                            rrn = "333";
                            msg = "444";
                            hostMid = "555";
                            batchNum = "666";
                            clientName = "test";
                            serial = "1234567890";
                            aid = "AID";
                            tvr = "TVR";
                            appName = "APP";
                            eftVoid = true;

                            addReceiptValues();

                            sts = ERR_OK;
                        } else {
                            Object ret = ecrInterface.Void(voidReceiptNumberECR, timeout);
                            EcrRsp_Void response = Bridge.cast(ret, EcrRsp_Void.class);
                            if (response != null) {
                                logger.debug("Response JSON: " + response.Serialize());

                                VoidResponse voidResponse = gson.fromJson(response.Serialize(), VoidResponse.class);

                                if (approvedCodes.contains(voidResponse.getCode())) {
                                    logger.info("Transaction void approved");

                                    authorizationCode = response.getauthCode();
                                    cardType = response.getissuerName();
                                    cardNumber = response.getpan();
                                    terminalId = response.gethostTid();
                                    receiptNumber = response.getinv();
                                    rrn = response.getrrn();
                                    eftVoid = true;

                                    String jsonData = voidResponse.getJsonData();
                                    SaleResponse innerSaleResponse = gson.fromJson(jsonData, SaleResponse.class);
                                    msg = innerSaleResponse.getMsg();
                                    authorizedAmount = new BigDecimal(innerSaleResponse.getAmt()).multiply(new BigDecimal(Math.pow(10, tnd[0].dec))).longValue();
                                    addReceiptValues();
                                    sts = ERR_OK;
                                } else {
                                    sts = ERR_NOTAUTHORIZED;
                                }
                            } else {
                                sts = ERR_NOTCONNECTED;
                            }
                        }
                        return sts;
                    }
                } catch (Exception e) {
                    logger.error("Error: ", e);
                } finally {
                    ecrInterface.Close();
                }
                break;
            default:
                break;
        }
        return 0;
    }

    @Override
    public boolean settle() {
        try {
            boolean res = ecrInterface.Open();
            logger.debug("Open result: " + res);
            if (!res) {
                String error = ecrInterface.GetLastError();
                logger.warn("Error opening connection: " + error);
                return false;
            } else {
                Object ret = ecrInterface.Settle(acquirer, timeout);
                EcrRsp_Settle response = Bridge.cast(ret, EcrRsp_Settle.class);
                if (response != null) {
                    logger.debug("Response JSON: " + response.Serialize());

                    SettlementResponse settlementResponse = gson.fromJson(response.Serialize(), SettlementResponse.class);

                    if (approvedCodes.contains(settlementResponse.getCode())) {
                        logger.info("Settlement approved");
                        res = true;
                    } else {
                        res = false;
                    }
                } else {
                    res = false;
                }
                return res;
            }
        } catch (Exception e) {
            logger.error("Error: ", e);
        } finally {
            ecrInterface.Close();
        }
        return false;
    }

    @Override
    public String getTenderId() {
        return APEX_TENDER_ID;
    }

    @Override
    public boolean isVoidCapable() {
        return true;
    }

    @Override
    public boolean isVoidPerformedOnVoidTransaction() {
        return true;
    }

    @Override
    public boolean isAmountRequiredOnVoid() {
        return false;
    };

    @Override
    public boolean isSettlementCapable() {
        return true;
    }

    @Override
    public boolean isSettlementEnabledAtEod() {
        return settlementEnabledAtEod;
    }

    @Override
    public String managePluginMacro(String line) {
        if (line.indexOf(MSG) >= 0) {
            line = line.substring(0, line.indexOf(MSG)) + msg
                    + line.substring(line.indexOf(MSG) + MSG.length());
        }
        if (line.indexOf(SERIAL) >= 0) {
            line = line.substring(0, line.indexOf(SERIAL)) + serial
                    + line.substring(line.indexOf(SERIAL) + SERIAL.length());
        }
        if (line.indexOf(HOST_MID) >= 0) {
            line = line.substring(0, line.indexOf(HOST_MID)) + hostMid
                    + line.substring(line.indexOf(HOST_MID) + HOST_MID.length());
        }
        if (line.indexOf(BATCH_NUM) >= 0) {
            line = line.substring(0, line.indexOf(BATCH_NUM)) + batchNum
                    + line.substring(line.indexOf(BATCH_NUM) + BATCH_NUM.length());
        }
        if (line.indexOf(CLIENT_NAME) >= 0) {
            line = line.substring(0, line.indexOf(CLIENT_NAME)) + clientName
                    + line.substring(line.indexOf(CLIENT_NAME) + CLIENT_NAME.length());
        }
        if (line.indexOf(AID) >= 0) {
            line = line.substring(0, line.indexOf(AID)) + aid
                    + line.substring(line.indexOf(AID) + AID.length());
        }
        if (line.indexOf(TVR) >= 0) {
            line = line.substring(0, line.indexOf(TVR)) + tvr
                    + line.substring(line.indexOf(TVR) + TVR.length());
        }
        if (line.indexOf(APP_NAME) >= 0) {
            line = line.substring(0, line.indexOf(APP_NAME)) + appName
                    + line.substring(line.indexOf(APP_NAME) + APP_NAME.length());
        }
        return line;
    }
}
