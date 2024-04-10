package com.ncr.ssco.communication.requestprocessors;

import com.ncr.ssco.communication.entities.TenderExceptionType;
import com.ncr.ssco.communication.entities.TenderType;
import com.ncr.ssco.communication.entities.TenderTypeEnum;
import com.ncr.ssco.communication.entities.pos.SscoError;
import com.ncr.ssco.communication.entities.pos.SscoTender;
import com.ncr.ssco.communication.manager.SscoMessageHandler;
import com.ncr.ssco.communication.manager.TenderTypeManager;
import com.ncr.ssco.communication.requestdecoder.RequestFromSsco;
import com.ncr.ssco.communication.responseencoder.ResponseToSsco;
import org.apache.log4j.Logger;

import java.util.Properties;

public class TenderRequestProcessor extends TransactionProcessor {
    private static final Logger logger = Logger.getLogger(TenderRequestProcessor.class);
    private SscoTender sscoTender;

    public TenderRequestProcessor(SscoMessageHandler messageHandler) {
        super(messageHandler);
    }

    @Override
    public void process(RequestFromSsco requestFromSsco) {
        logger.debug("Enter");

        try {
            sscoTender = new SscoTender(TenderTypeEnum.valueOf(requestFromSsco.getStringField("TenderType").replaceAll(" ", "")), requestFromSsco.getIntField("Amount"));
            sscoTender.setUpc(requestFromSsco.getStringField("UPC"));
        } catch (Exception e) {
            logger.error("Error in tender message: ", e);
        }
        logger.info("tenderType: " + sscoTender.getTenderType());
        logger.info("amount: " + sscoTender.getAmount());

        if (sscoTender.getAmount() == 0 && TenderTypeEnum.Cash.equals(sscoTender.getTenderType())) {
            logger.info("Tender Amount 0: sending end of transaction");
            sendTenderAcceptedResponseToSsco(sscoTender);
            sendTotalsResponse(new SscoError());
            sendEndTransactionResponseToSsco("1");
            getMessageHandler().getResponses().add(addEndResponse());
        } else {
            TenderType tenderType = TenderTypeManager.getInstance().getActionPOSByName(sscoTender.getTenderType());

            if (!tenderType.isHigherThanAmount() && sscoTender.getAmount() > getManager().getTotalsAmount().getBalanceDue()) {
                sendResponses(new SscoError(SscoError.HIGHER_THAN_BALANCE, "Higher than balance"));
            } else {
                getManager().tenderRequest(sscoTender);
            }
        }

        logger.debug("Exit");
    }

    @Override
    public void sendResponses(SscoError sscoError) {
        logger.debug("Enter");

        TenderType tenderType = TenderTypeManager.getInstance().getActionPOSByName(sscoTender.getTenderType());

        if (sscoError.getCode() != SscoError.OK) {
            sendTenderExceptionResponseToSsco(sscoError);
            if (tenderType != null && tenderType.isAdditionalClear()) {
                getManager().clearRequest();
            }
        } else {
            sendTenderAcceptedResponseToSsco(getManager().getLastTender());
            sendTotalsResponse(sscoError);
            if (getManager().getTotalsAmount().getBalanceDue() <= 0) {
                sendEndTransactionResponseToSsco("1");
            }
        }
        getMessageHandler().getResponses().add(addEndResponse());

        logger.debug("Exit");
    }

    private void sendTenderAcceptedResponseToSsco(SscoTender sscoTender) {
        logger.debug("Enter");

        ResponseToSsco responseToSsco = getMessageHandler().createResponseToSsco("TenderAccepted");
        responseToSsco.setIntField("Amount", sscoTender.getAmount());
        responseToSsco.setStringField("TenderType", sscoTender.getTenderType().toString());
        responseToSsco.setStringField("Description", sscoTender.getDescription());

        getMessageHandler().sendResponseToSsco(responseToSsco);

        logger.debug("Exit");
    }

    private void sendTenderExceptionResponseToSsco(SscoError sscoError) {
        logger.debug("Enter. ErrorMessage: " + sscoError.getMessage());

        Properties processorsProperties = getMessageHandler().getProcessorsProperties();
        int tenderExceptionId = 0;
        TenderExceptionType tenderExceptionType = TenderExceptionType.Event;

        ResponseToSsco responseToSsco = getMessageHandler().createResponseToSsco("TenderException");
        responseToSsco.setStringField("TenderType", sscoTender.getTenderType().toString());

        if (processorsProperties.getProperty("Tender." + tenderExceptionType + ".Exception.Attendant") != null) {
            // TODO: do stuff
        }

        try {
            tenderExceptionType = TenderExceptionType.valueOf(processorsProperties.getProperty("TenderException." + sscoError.getCode() + ".Type"));
            tenderExceptionId = Integer.parseInt(processorsProperties.getProperty("TenderException." + sscoError.getCode() + ".Id"));
        } catch (Exception e) {
            logger.error("Error reading properties: ", e);
        }

        TenderType tenderType = TenderTypeManager.getInstance().getActionPOSByName(sscoTender.getTenderType());
        if (tenderType.getGenericException() > 0) {
            sscoError = new SscoError(tenderType.getGenericException());
        }

        responseToSsco.setIntField("ExceptionType", tenderExceptionType.getCode());
        responseToSsco.setIntField("ExceptionId", tenderExceptionId);
        responseToSsco.setStringField("Message.1", sscoError.getMessage());
        String tableName = getManager().getProcessorTableName(TenderRequestProcessor.class.getSimpleName());
        if (tableName != null) {
            responseToSsco.setStringField("TableName", tableName);
        }

        getMessageHandler().sendResponseToSsco(responseToSsco);

        logger.debug("Exit");
    }
}