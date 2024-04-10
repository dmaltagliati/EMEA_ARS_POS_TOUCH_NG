package com.ncr.ssco.communication.requestprocessors;

import com.ncr.ssco.communication.entities.pos.SscoCustomer;
import com.ncr.ssco.communication.entities.pos.SscoError;
import com.ncr.ssco.communication.manager.SscoMessageHandler;
import com.ncr.ssco.communication.requestdecoder.RequestFromSsco;
import com.ncr.ssco.communication.responseencoder.ResponseToSsco;
import org.apache.log4j.Logger;

public class LoyaltyRequestProcessor extends ItemRequestProcessor {
    private static final Logger logger = Logger.getLogger(LoyaltyRequestProcessor.class);
    private SscoCustomer sscoCustomer;
    private String cardType = "";
    private String responseName = "";
    public static final String PREFIX_NUM = "00";
    public static final String PREFIX_PLUS = "+";

    public LoyaltyRequestProcessor(SscoMessageHandler messageHandler) {
        super(messageHandler);
    }

    @Override
    public void process(RequestFromSsco requestFromSsco) {
        logger.debug("Enter");
        String code = requestFromSsco.getStringField("Code");
        String accountNumber = requestFromSsco.getStringField("AccountNumber");
        String countryCode = requestFromSsco.getStringField("CountryCode");
        String entryMethod = requestFromSsco.getStringField("EntryMethod");
        if (code != null && code.length() > 1) {
            if (code.charAt(0) == PREFIX_PLUS.charAt(0)) {
                countryCode = PREFIX_NUM;
                accountNumber = code.substring(1);
            } else {
                countryCode = "";
                accountNumber = code;
            }
        }
        logger.debug("Country code: " + countryCode + " Account: " + accountNumber);
        setLoyaltyInfo(new SscoCustomer(accountNumber, countryCode, entryMethod, 0));
        getManager().loyaltyRequest(getSscoCustomer());
        logger.debug("Exit");
    }

    public void setLoyaltyInfo(SscoCustomer sscoCustomer) {
        logger.debug("Enter");

        setSscoCustomer(sscoCustomer);
        setCardType("Loyalty");
        setResponseName("LoyaltyCard");
        logger.debug("Exit");
    }

    @Override
    public void sendResponses(SscoError sscoError) {
        logger.debug("Enter");

        if (!getManager().transactionHasStarted() && SscoError.OK == sscoError.getCode()) {
            sendStartTransaction();
            getManager().addCustomer(getSscoCustomer());
        } else {
            getManager().clearRequest();
            sendLoyaltyResponse(sscoError);
            sendTotalsResponse(sscoError);
        }
        getMessageHandler().getResponses().add(addEndResponse());

        logger.debug("Exit");
    }

    private int mapErrorToStatus(SscoError sscoError) {
        switch (sscoError.getCode()) {
            case SscoError.OK:
                return 1;
            case 117:
                return 2;
            default:
                return 0;
        }
    }

    private void sendLoyaltyResponse(SscoError sscoError) {
        logger.debug("Enter");

        int status = mapErrorToStatus(sscoError);
        ResponseToSsco responseToSsco = getMessageHandler().createResponseToSsco(getResponseName());
        responseToSsco.setStringField("AccountNumber", getSscoCustomer().getAccountNumber());
        responseToSsco.setStringField("CardType", getCardType());
        responseToSsco.setIntField("Status", status);
        responseToSsco.setIntField("Points", getSscoCustomer().getPoints());
        if (status == 0) {
            responseToSsco.setStringField("Message.1", sscoError.getMessage());
            String tableName = getManager().getProcessorTableName(LoyaltyRequestProcessor.class.getSimpleName());
            if (tableName != null) {
                responseToSsco.setStringField("TableName", tableName);
            }
        }
        if (getSscoCustomer().getPointsValue() != null) {
            responseToSsco.setStringField("PointsValue", getSscoCustomer().getPointsValue());
        }

        getMessageHandler().sendResponseToSsco(responseToSsco);
        logger.debug("Exit");
    }

    public SscoCustomer getSscoCustomer() {
        return sscoCustomer;
    }

    public void setSscoCustomer(SscoCustomer sscoCustomer) {
        this.sscoCustomer = sscoCustomer;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getResponseName() {
        return responseName;
    }

    public void setResponseName(String responseName) {
        this.responseName = responseName;
    }
}