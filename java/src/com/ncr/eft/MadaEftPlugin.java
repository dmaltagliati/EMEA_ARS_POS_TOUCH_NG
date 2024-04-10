package com.ncr.eft;

import com.is.spaninteg.SpanInteg;
import com.ncr.*;
import com.ncr.eft.data.mada.MadaTransactionResult;
import com.ncr.eft.data.mada.MadaReconciliationResult;
import com.ncr.eft.data.mada.TransactionRequest;
import org.apache.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.math.BigDecimal;
import java.util.Properties;

import static com.ncr.Itmdc.IDC_write;


public class MadaEftPlugin extends GenericEftPlugin {
    private static final Logger logger = Logger.getLogger(MadaEftPlugin.class);
    private static final String responseSALEPathKey = "mada.sale-test.path";
    private static String responseSALEPath = "responseTestPlugin//PURCHASE.xml";
    private static final String responseREFUNDPathKey = "mada.refund-test.path";
    private static String responseREFUNDPath = "responseTestPlugin//REFOUND.xml";
    private static String responseRECONPathKey = "mada.recon-test.path";
    private static String responseRECONPath = "responseTestPlugin//Reconiliation.xml";
    private static final String TRANS_ACTION_SALE = "SALE";
    private static final String TRANS_ACTION_REFUND = "REFUND";
    private static final String TRANS_ACTION_RECON = "RECON";
    private static final String APPROVED_RESPONSE = "APPROVED";
    private static final int TIMEOUT_RESPONSE_CODE = 6;

    //Properties variables
    private int printFlag;
    private boolean testEnvironment = false;
    private static boolean settlementEnabledAtEod = false;
    private static String port = "";
    private static String subNumber = "";

    //Usefull variables
    private static String terminalId = "";
    private static String retailerId = "";

    //Void variables
    private final String maskRegexInputKey = "mada.regex-input";
    private String maskRegexInput = "(\\d{6})(\\d{6})(\\d{4})";
    private final String maskRegexOutputKey = "mada.regex-output";
    private String maskRegexOutput = "$1******$3";
    private long voidAmount = 0;
    private String original_RRN = "";
    private String maskedPAN = "";

    public MadaEftPlugin() {
        terminalId = editNum(ctl.reg_nbr, 8);
    }

    public void loadEftTerminalParams(int line, String txt) {
        super.loadEftTerminalParams(line, txt);
        //EFTV0:110000:0:COM18:0000000000000000:00000011
        //EFTV0:110000:1:COM2 :0000000000000000:00000010
        switch (line) {
            case 0:
                logger.debug("EFTV0: " + txt);
                printFlag = Integer.valueOf(txt.substring(7, 8));
                port = txt.substring(9, 14).trim().replace("COM", "");
                subNumber = txt.substring(15, 31).trim();
                settlementEnabledAtEod = txt.charAt(38) == '1';
                testEnvironment = txt.charAt(39) == '1';

                logger.info("INIT - printFlag :> " + printFlag);
                logger.info("INIT - port :> " + port);
                logger.info("INIT - subNumber :> " + subNumber);
                logger.info("INIT - settlementEnabledAtEod :> " + settlementEnabledAtEod);
                logger.info("INIT - testEnvironment :> " + testEnvironment);

                break;
            default:
                break;
        }
    }

    @Override
    public void init(Properties props) {
        logger.info("ENTER");

        StringBuffer sTerminalID = new StringBuffer();
        StringBuffer sRetailerID = new StringBuffer();

        int res = SpanInteg.GetInstance().GetParams(port, sTerminalID, sRetailerID);

        logger.info("Retured code :> " + res);

        terminalId = sTerminalID.toString();
        retailerId = sRetailerID.toString();

        maskRegexInput = props.getProperty(maskRegexInputKey, maskRegexInput);
        maskRegexOutput = props.getProperty(maskRegexOutputKey, maskRegexOutput);
        responseSALEPath = props.getProperty(responseSALEPathKey, responseSALEPath);
        responseREFUNDPath = props.getProperty(responseREFUNDPathKey, responseREFUNDPath);
        responseRECONPath = props.getProperty(responseRECONPathKey, responseRECONPath);

        logger.info("INIT - maskRegexInput :> " + maskRegexInput);
        logger.info("INIT - maskRegexOutput :> " + maskRegexOutput);
        logger.info("INIT - responseSALEPath :> " + responseSALEPath);
        logger.info("INIT - responseREFUNDPath :> " + responseREFUNDPath);
        logger.info("INIT - responseRECONPath :> " + responseRECONPath);
        logger.info("INIT - TerminalID :> " + terminalId);
        logger.info("INIT - RetailerID :> " + retailerId);
        logger.info("EXIT");
    }

    @Override
    public int pay(Itemdata itm, Terminal ctl, LinIo line) {
        logger.info("SALE - ENTER");

        TransactionRequest tr = new TransactionRequest();
        tr.setCommand(TRANS_ACTION_SALE);
        tr.setAmount(itm.amt);
        tr.setSubscriberNumber(subNumber);
        tr.setPrintFlag(printFlag);
        tr.setAdditionalData("");

        int sts = elaborateTransaction(tr, responseSALEPath);

        if (sts == 0)
            Itmdc.IDC_write('z', Struc.tra.tnd, 0, authorizationCode, 1, authorizedAmount);

        logger.info("SALE - EXIT. status: " + sts);
        return sts;
    }

    @Override
    public int voidPayment(int spec, String input) {
        logger.info("VOID - ENTER");
        int sts = ERR_OK;

        switch (spec) {
            case 2:
                logger.info("Void amount: " + input);
                voidAmount = Long.parseLong(input);
                break;
            case 5:
                logger.info("Void Original RRN: " + input);
                original_RRN = input;
                break;
            case 3:
                logger.info("Void Masked PAN: " + input);
                maskedPAN = input;

                logger.info("Void process");
                maskedPAN = maskedPAN.replaceAll(maskRegexInput, maskRegexOutput);

                TransactionRequest tr = new TransactionRequest();
                tr.setCommand(TRANS_ACTION_REFUND);
                tr.setAmount(voidAmount);
                tr.setOriginal_RRN(original_RRN);
                tr.setMaskedPAN(maskedPAN);
                tr.setPrintFlag(printFlag);
                tr.setAdditionalData("");

                sts = elaborateTransaction(tr, responseREFUNDPath);
                //TODO: Scrivere qualche record IDC?
                break;
            default:
                break;
        }

        logger.info("VOID - EXIT. status: " + sts);
        return sts;
    }

    @Override
    public boolean settle() {
        logger.info("RECON - ENTER");

        TransactionRequest tr = new TransactionRequest();
        tr.setCommand(TRANS_ACTION_RECON);
        tr.setPrintFlag(printFlag);

        int sts = elaborateRecoon(tr, responseRECONPath);
        if (sts == 0)
            IDC_write('z', tra.tnd, 2, "000", 1, 0);

        logger.info("RECON - EXIT. status: " + sts);
        return sts > 0;
    }

    private int elaborateTransaction(TransactionRequest tr, String responseTestPath) {
        int sts = ERR_OK;

        try {
            StringBuffer sXmlResult = new StringBuffer();

            //Create Request
            JAXBContext contextObj = JAXBContext.newInstance(TransactionRequest.class);
            Marshaller marshallerObj = contextObj.createMarshaller();
            marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            StringWriter request = new StringWriter();
            marshallerObj.marshal(tr, request);
            int res = 0;

            logger.debug("XMLTransaction request :> \n" + request.toString());

            //Perform request
            if (!testEnvironment)
                res = SpanInteg.GetInstance().PerformMadaEcrTrx(port, request.toString(), sXmlResult);
            else {
                BufferedReader br = new BufferedReader(new FileReader(responseTestPath));
                //Assuming xml files are written in one line
                String xmlFIleTest = br.readLine();
                sXmlResult.append(xmlFIleTest);
            }
            logger.info("Retured code :> " + res);
            logger.debug("XMLTransaction response :> \n" + sXmlResult.toString());

            if (res != ERR_OK) {
                logger.info("API Error code :> " + res);
                sts = ERR_DECLINED;
                if (res == TIMEOUT_RESPONSE_CODE)
                    sts = ERR_TIMEOUTTRANSACTION;
                return sts;
            }

            //Process response
            JAXBContext jaxbContext = JAXBContext.newInstance(MadaTransactionResult.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            StringReader reader = new StringReader(sXmlResult.toString());
            MadaTransactionResult result = (MadaTransactionResult) jaxbUnmarshaller.unmarshal(reader);

            //Elaborate response code
            if (!result.getResult().getEnglish().equals(APPROVED_RESPONSE)) {
                logger.info("Transaction Not Approved");
                sts = ERR_DECLINED;
            }

            if (!result.getTags().getTerminalStatusCode().equals("00")) {
                logger.info("Transaction Decined");
                return ERR_DECLINED;
            }

            logger.info("Transaction approved");
            authorizationCode = result.getTags().getAC();
            //authorizedAmount = (long) (Double.parseDouble(result.getAmounts().getPurchaseAmount()) * Math.pow(10, tnd[0].dec));
            authorizedAmount = new BigDecimal(result.getAmounts().getPurchaseAmount()).multiply(new BigDecimal(Math.pow(10, tnd[0].dec))).longValue();
            cardNumber = result.getPAN();
            cardType = result.getApplicationLabel().getEnglish();
            terminalId = result.getTerminalID();
            receiptNumber = Integer.toString(ctl.tran);
            rrn = result.getRRN();
            mid = result.getMerchantID();

            itm.amt = authorizedAmount;
            addReceiptValues();
        } catch (Exception e) {
            logger.error("Error: ", e);
            sts = ERR_RESPONSE;
        }

        return sts;
    }

    private int elaborateRecoon(TransactionRequest tr, String responseTestPath) {
        int sts = ERR_OK;

        try {
            StringBuffer sXmlResult = new StringBuffer();

            //Create Request
            JAXBContext contextObj = JAXBContext.newInstance(TransactionRequest.class);
            Marshaller marshallerObj = contextObj.createMarshaller();
            marshallerObj.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            StringWriter request = new StringWriter();
            marshallerObj.marshal(tr, request);
            int res = 0;

            logger.debug("XMLTransaction request :> \n" + request.toString());

            //Perform request
            if (!testEnvironment)
                res = SpanInteg.GetInstance().PerformMadaEcrTrx(port, request.toString(), sXmlResult);
            else {
                BufferedReader br = new BufferedReader(new FileReader(responseTestPath));
                //Assuming xml files are written in one line
                String xmlFIleTest = br.readLine();
                sXmlResult.append(xmlFIleTest);
            }
            logger.info("Retured code :> " + res);
            logger.debug("XMLTransaction response :> \n" + sXmlResult.toString());

            if (res != 0) {
                logger.info("API Error code :> " + res);
                sts = ERR_DECLINED;
                if (res == TIMEOUT_RESPONSE_CODE)
                    sts = ERR_TIMEOUTTRANSACTION;
                return sts;
            }

            //Process response
            JAXBContext jaxbContext = JAXBContext.newInstance(MadaReconciliationResult.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            StringReader reader = new StringReader(sXmlResult.toString());
            MadaReconciliationResult result = (MadaReconciliationResult) jaxbUnmarshaller.unmarshal(reader);

            terminalId = result.getTerminalID();
            receiptNumber = Integer.toString(ctl.tran);
            rrn = result.getRRN();
            mid = result.getMerchantID();

            //addReceiptValues();
        } catch (Exception e) {
            logger.error("Error: ", e);
            sts = ERR_RESPONSE;
        }

        return sts;
    }

    @Override
    public String getTenderId() {
        return MADA_TENDER_ID;
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

    @Override
    public void stop(Terminal ctl) {
        logger.info("ENTER");
        super.stop(ctl);
        logger.info("EXIT");
    }

    @Override
    public String managePluginMacro(String line) {
        return super.managePluginMacro(line);
    }
}
