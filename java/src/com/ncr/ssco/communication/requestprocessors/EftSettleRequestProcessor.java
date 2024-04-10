package com.ncr.ssco.communication.requestprocessors;

import com.ncr.ssco.communication.entities.pos.SscoError;
import com.ncr.ssco.communication.manager.SscoMessageHandler;
import com.ncr.ssco.communication.requestdecoder.RequestFromSsco;
import com.ncr.ssco.communication.responseencoder.ResponseToSsco;
import org.apache.log4j.Logger;

public class EftSettleRequestProcessor extends DefaultRequestProcessor {
    private static final Logger logger = Logger.getLogger(EftSettleRequestProcessor.class);

    public EftSettleRequestProcessor(SscoMessageHandler messageHandler) {
        super(messageHandler);
    }

    @Override
    public void process(RequestFromSsco requestFromSsco) {
        logger.debug("Enter");

        if (!getManager().eftSettleRequest()) logger.warn("-- Warning ");

        logger.debug("Exit");
    }

    @Override
    public void sendResponses(SscoError sscoError) {
        logger.debug("Enter");

        ResponseToSsco responseToSsco = getMessageHandler().createResponseToSsco("EFTSettleReply");
        responseToSsco.setIntField("Status", getManager().isSettlemetSucceeded() ? 1 : 0);

        if (sscoError.OK != sscoError.getCode()) {
            logger.info("Response Eft Settle KO - Enter - error code: " + sscoError.getCode() + " message: " + sscoError.getMessage());
            responseToSsco.setStringField("Message.1", sscoError.getMessage());
        }

        getMessageHandler().sendResponseToSsco(responseToSsco);
        getMessageHandler().getResponses().add(addEndResponse());

        logger.debug("Exit");
    }
}