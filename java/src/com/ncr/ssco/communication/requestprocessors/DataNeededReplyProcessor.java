package com.ncr.ssco.communication.requestprocessors;

import com.ncr.ssco.communication.manager.SscoMessageHandler;
import com.ncr.ssco.communication.requestdecoder.RequestFromSsco;
import com.ncr.ssco.communication.responseencoder.ResponseToSsco;

public class DataNeededReplyProcessor extends DefaultRequestProcessor {

    public DataNeededReplyProcessor(SscoMessageHandler messageHandler) {
        super(messageHandler);
    }

    private static int delayedConfirmation;
    private static boolean waitReplyOfCloseDateneeded;
    private String dataNeededInputData;

    @Override
    public void setProcessRequest(RequestFromSsco requestFromSsco) {
    }

    private void sendClearDataNeeded() {
        ResponseToSsco responseToSsco = getMessageHandler().createResponseToSsco("DataNeeded");
        responseToSsco.setIntField("Type", 0);
        responseToSsco.setIntField("Id", 0);
        responseToSsco.setIntField("Mode", 0);
        getMessageHandler().sendResponseToSsco(responseToSsco);
        getMessageHandler().getResponses().add(addEndResponse());
    }

    private boolean detectCancelAndClose(RequestFromSsco requestFromSsco) {
        Integer repCancel = requestFromSsco.getIntField("Cancel");
        if (repCancel != null) {
            logger.info("Cancel detected =" + repCancel);
            if (repCancel == 1) {
                //logger.info("Closing dataneeded");
                //sendClearDataNeeded();
                return true;
            }
        }
        return false;
    }

    @Override
    public void process(RequestFromSsco requestFromSsco) {
        int type = requestFromSsco.getIntField("Type");

        if (type == 0) {
            getMessageHandler().pendingResposeToResponse();
            getMessageHandler().getResponses().add(addEndResponse());
            getManager().closeDataneeded();
            return;
        }

        if (detectCancelAndClose(requestFromSsco)) {
            getManager().closeDataneeded();
            return;
        }

        String originalDataneededName = getMessageHandler().getPendingDataNeeded().getName();
        if (originalDataneededName.equalsIgnoreCase("LoyaltyNotFound") ||
                originalDataneededName.equalsIgnoreCase("LoyaltyError") ||
                originalDataneededName.equalsIgnoreCase("QrCodeUnsoldItems") ||
                originalDataneededName.equalsIgnoreCase("ZatcaMessage") ||
                originalDataneededName.equalsIgnoreCase("UPBError")) {
            sendClearDataNeeded();
        }

        if (originalDataneededName.equalsIgnoreCase("CashierDisplayMessage") ||
                originalDataneededName.equalsIgnoreCase("CashierDisplayMessageStore") ||
                originalDataneededName.equalsIgnoreCase("OkCancel") ||
                originalDataneededName.equalsIgnoreCase("OkCancelStore") ||
                originalDataneededName.equalsIgnoreCase("OkCancelReceiptPrint") ||
                originalDataneededName.equalsIgnoreCase("UPBConfirmError") ||
                originalDataneededName.equalsIgnoreCase("UPBConfirm") ||
                originalDataneededName.equalsIgnoreCase("OkCancelDonation")) {
            logger.info("Data needed name: " + originalDataneededName);
            logger.info("Type=" + type);

            int confirmation = requestFromSsco.getIntField("Confirmation");
            logger.info("confirmation=" + confirmation);

            if (confirmation == 1) {
                getManager().sendDataneededEnter();
            } else {
                getManager().sendDataneededCorrettore();
            }
            return;
        }

        if (originalDataneededName.equalsIgnoreCase("InsertLoyaltyCard")
                || originalDataneededName.equalsIgnoreCase("InsertLoyaltyAmount")
                || originalDataneededName.equalsIgnoreCase("InsertAmount")
                || originalDataneededName.startsWith("InsertAdditionalInfo")
                || originalDataneededName.startsWith("InsertSerialNumber")) {
            getManager().setInsertedCode(requestFromSsco.getStringField("Data.1"));
            logger.debug("Received: " + requestFromSsco.getStringField("Data.1") + " Sending close data needed");
            getManager().closeDataneeded();
        }
    }
}
