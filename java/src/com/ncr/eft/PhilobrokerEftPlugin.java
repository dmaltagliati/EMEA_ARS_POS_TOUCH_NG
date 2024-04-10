package com.ncr.eft;

import com.google.gson.*;
import com.ncr.*;
import com.ncr.common.data.TerminalInfo;
import com.ncr.eft.data.philobroker.*;
import com.ncr.zatca.utils.UnirestWrapper;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static com.ncr.Itmdc.IDC_write;

public class PhilobrokerEftPlugin extends GenericEftPlugin {
    private static final Logger logger = Logger.getLogger(PhilobrokerEftPlugin.class);
    private static final String URL = "url";
    private static final String MERCHANT = "merchant";
    private static final String CURRENCY_CODE = "currency-code";
    private static final String PAYMENT = "Payment";
    private static final String VOID = "Void";
    private static final String SETTLEMENT = "CloseBatch";
    private static final String PURCHASE = "PURCHASE";
    private static final String ID = "id";


    private long voidAmount = 0;
    private String originalRRN = "";
    private String originalDate = "";


    private final List<String> approvedCodes = Arrays.asList(new String[] {"000", "001", "003", "007", "087", "089", "300", "400", "500", "800"});
    private boolean testEnvironment = false;
    private static boolean settlementEnabledAtEod = false;
    private final Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
    private Properties props;

    public PhilobrokerEftPlugin() {
        terminalId = editNum(ctl.reg_nbr, 8);
    }

    public void loadEftTerminalParams(int line, String txt) {
        super.loadEftTerminalParams(line, txt);
        switch (line) {
            case 0:
                logger.debug("EFTZ0: " + txt);
                settlementEnabledAtEod = txt.charAt(38) == '1';
                testEnvironment = txt.charAt(39) == '1';

                logger.info("INIT - settlementEnabledAtEod :> " + settlementEnabledAtEod);
                logger.info("INIT - testEnvironment :> " + testEnvironment);
                break;
            default:
                break;
        }
    }

    @Override
    public void init(Properties props) {
        logger.debug("Enter");
        this.props = props;

        logger.info("Exit");
    }

    private Response eftCommunication(Request request) {
        logger.debug("Enter");
        String endpoint = props.getProperty(URL, "http://localhost/") + "?id=" + props.getProperty(ID, "default");
        String jsonString = gson.toJson(request);
        logger.debug("Eft request: " + jsonString);

        JsonElement jsonElement = UnirestWrapper.getInstance().postRequest(endpoint, jsonString, props);
        logger.debug("Exit. Response: " + jsonElement);
        return gson.fromJson(jsonElement, Response.class);
    }

    @Override
    public int pay(Itemdata itm, Terminal ctl, LinIo line) {
        logger.debug("Enter");
        TerminalInfo terminalInfo = new TerminalInfo(
                editNum(ctl.sto_nbr, 4),
                editKey(ctl.reg_nbr, 3),
                editNum(ctl.ckr_nbr, 3),
                editNum(ctl.tran, 4));

        int sts = eftPayment(terminalInfo, itm.amt);
        if (sts == ERR_OK) {
            itm.amt = authorizedAmount;
            addReceiptValues();
            Itmdc.IDC_write('z', Struc.tra.tnd, 0, authorizationCode, 1, authorizedAmount);
        } else {
            addErrorReceiptValues();
        }

        logger.info("Exit. status: " + sts);
        return sts;
    }

    private int eftPayment(TerminalInfo terminalInfo, long amount) {
        Request request = buildPaymentRequest(terminalInfo, amount);

        if (testEnvironment) {
            //TODO: Add fields
            return ERR_OK;
        } else {
            Response response = eftCommunication(request);
            if (response != null && approvedCodes.contains(response.getResults().get(0).getResponseCode())) {
                Result result= response.getResults().get(0);
                authorizationCode = result.getTransactionDetail().getAuthCode();
                authorizedAmount = new BigDecimal(result.getTransactionDetail().getTransactionAmount()).multiply(BigDecimal.valueOf(Math.pow(10, tnd[0].dec))).longValue();
                cardNumber = result.getTransactionDetail().getAccountNumber();
                cardType = result.getTransactionDetail().getCardProductName();
                terminalId = result.getTransactionDetail().getTerminalIdentification();
                receiptNumber = Integer.toString(ctl.tran);
                rrn = result.getTransactionDetail().getRetrievalReferenceNumber();
                mid = result.getTransactionDetail().getMid();
                return ERR_OK;
            } else {
                //TODO: Fill info for error
                return ERR_DECLINED;
            }
        }
    }

    private Request buildPaymentRequest(TerminalInfo terminalInfo, long amount) {
        AdditionalInfo additionalInfo = AdditionalInfo.builder()
                .amount(editDec(amount, tnd[0].dec))
                .build();
        return buildRequest(terminalInfo, additionalInfo, PAYMENT);
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
                logger.info("Void Original Date: " + input);
                if (!input.matches("^(0[1-9]|[12][0-9]|3[01])(0[1-9]|1[012])([0-9]{2})$"))
                    return 5;
                originalDate = input;
                break;
            case 3:
                logger.info("Void Original RRN: " + input);
                originalRRN = input;

                logger.info("Void process");

                TerminalInfo terminalInfo = new TerminalInfo(
                        editNum(ctl.sto_nbr, 4),
                        editKey(ctl.reg_nbr, 3),
                        editNum(ctl.ckr_nbr, 3),
                        editNum(ctl.tran, 4));

                 sts = eftVoid(terminalInfo);
                break;
            default:
                break;
        }

        if (sts == ERR_OK) {
            IDC_write('z', tra.tnd, 2, "000", 1, 0);
        }

        logger.debug("Exit. status: " + sts);
        return sts;
    }

    private int eftVoid(TerminalInfo terminalInfo) {
        Request request = buildVoidRequest(terminalInfo);
        Response response = eftCommunication(request);
        if (response != null && approvedCodes.contains(response.getResults().get(0).getResponseCode())) {
            return ERR_OK;
        } else {
            return ERR_RESPONSE;
        }
    }

    private Request buildVoidRequest(TerminalInfo terminalInfo) {
        AdditionalInfo additionalInfo = AdditionalInfo.builder()
                .retrievalReferenceNumber(originalRRN)
                .transactionDateTime(originalDate)
                .amount(voidAmount+"")
                .customData(PURCHASE)
                .build();

        return buildRequest(terminalInfo, additionalInfo, VOID);
    }

    @Override
    public boolean settle() {
        logger.debug("Enter");
        TerminalInfo terminalInfo = new TerminalInfo(
                editNum(ctl.sto_nbr, 4),
                editKey(ctl.reg_nbr, 3),
                editNum(ctl.ckr_nbr, 3),
                editNum(ctl.tran, 4));

        int sts = eftSettlement(terminalInfo);
        if (sts == ERR_OK) {
            IDC_write('z', tra.tnd, 2, "000", 1, 0);
        }

        logger.debug("Exit. status: " + sts);
        return sts > 0;
    }

    private int eftSettlement(TerminalInfo terminalInfo) {
        Request request = buildSettlementRequest(terminalInfo);

        Response response = eftCommunication(request);
        return ERR_OK;
    }

    private Request buildSettlementRequest(TerminalInfo terminalInfo) {
        AdditionalInfo additionalInfo = AdditionalInfo.builder()
                .build();
        return buildRequest(terminalInfo, additionalInfo, SETTLEMENT);
    }

    private Request buildRequest(TerminalInfo terminalInfo, AdditionalInfo additionalInfo, String operationType) {
        Payload payload = Payload.builder()
                .cashierId(terminalInfo.getCashierId())
                .currencyCode(props.getProperty(CURRENCY_CODE, "978"))
                .additionalInfo(additionalInfo)
                .build();
        Info info = Info.builder()
                .terminalId(terminalInfo.getRegisterId())
                .storeId(terminalInfo.getStoreId())
                .merchant(props.getProperty(MERCHANT, ""))
                .build();
        String transactionId = editNum(SequenceGenerator.getInstance().getNext(this.getClass().getSimpleName(), 1, 999999), 6);
        logger.debug("TransactionId: " + transactionId);
        return Request.builder()
                .systemId(transactionId)
                .transactionType(operationType)
                .payload(payload).info(info).build();
    }

    @Override
    public String getTenderId() {
        return PHILOBROKER_TENDER_ID;
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
        logger.debug("Enter");
        super.stop(ctl);
        logger.debug("Exit");
    }

    @Override
    public String managePluginMacro(String line) {
        //TODO: Implement plugin-specific macros
        return line;
    }

    @Override
    public boolean isDateRequiredOnVoid() {
        return true;
    };
}
