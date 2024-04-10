package com.ncr.eft;

import com.ncr.*;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

import static com.ncr.Itmdc.IDC_write;

/**
 * Created with IntelliJ IDEA.
 * User: Administrator
 * Date: 01/02/16
 * Time: 17.25
 * To change this template use File | Settings | File Templates.
 */
public class MarshallEftPlugin extends GenericEftPlugin {
    private static final Logger logger = Logger.getLogger(MarshallEftPlugin.class);

    //say if dll was started correctly
    private boolean initialized = false;

    /*Macro for payment voucher*/
    private final String MACRO_cc_number = "$CC_NUMBER$";
    private final String MACRO_amount = "$AMOUNT$";
    private final String MACRO_authorisation_number = "$AUTH_NUMBER$";
    private final String MACRO_card_scheme_name = "$SCHEME_NAME$";
    private final String MACRO_ecr_amount = "$ECR_AMOUNT$";
    private final String MACRO_ecr_receipt = "$ECR_RECEIPT$";
    private final String MACRO_ecr_receipt_void = "$ECR_RECEIPT_VOID$";
    private final String MACRO_ecr_terminal_id = "$ECR_TERMINAL_ID$";
    private final String MACRO_transaction_id_Ecr = "$ECR_TRANSACTION_ID$";

    private String voidReceiptNumberECR = "";
    private String voidAmount = "";

    private static boolean settlementEnabledAtEod = false;
    private static boolean testEnvironment = false;

    private static MarshallEftPlugin instance = null;

    public static MarshallEftPlugin getInstance() {
        if (instance == null)
            instance = new MarshallEftPlugin();

        return instance;
    }

    public MarshallEftPlugin() {
    }

    public void cleanReceiptData() {
        receiptData = new HashMap();
    }

    @Override
    public void loadEftTerminalParams(int line, String txt) {
        super.loadEftTerminalParams(line, txt);

        if (line == 0) {
            settlementEnabledAtEod = txt.substring(2, 3).equals("1");
            testEnvironment = txt.substring(3, 4).equals("1");
        }
    }

    @Override
    public void init(Properties props) {
    }

    public int doTransaction(long amt, String traNum) {
        EcrMarshallObject ecrMarshallObject = testEnvironment ? new EcrObjectMock() : new EcrObject();

        initialized = ecrMarshallObject.getIsInitialized();
        logger.info("Verifone initialize [" + initialized + "]");

        if (!initialized) return ERR_NOTAUTHORIZED;

        logger.info("Verifone checkResponse");
        ecrMarshallObject.checkResponse();
        ecrMarshallObject.setTransactionType(EcrObject.TRANSACTION_TYPE_SALE);
        ecrMarshallObject.setEcrReceiptNumber(traNum);
        ecrMarshallObject.setAmoutECR(amt + "");

        ecrMarshallObject.getAuthECR();

        logger.info("Verifone response [" + ecrMarshallObject.getResponseMessageEcr() + "]");
        if (!ecrMarshallObject.checkResponse()) {
            logger.info("Verifone checkResponse failed");
            return ERR_RESPONSE;
        } else {
            logger.info("Verifone checkResponse success");

            authorizationCode = ecrMarshallObject.getApprovalCodeECR();
            cardType = ecrMarshallObject.getCardSchemaNameECR();
            cardNumber = ecrMarshallObject.getCardNumberECR();
            terminalId = ecrMarshallObject.getTIDECR();
            receiptNumber = ecrMarshallObject.getMessNumECR();

            addReceiptValues(ecrMarshallObject);
            IDC_write('z', Struc.tra.tnd, 0, ecrMarshallObject.getApprovalCodeECR(), 1, amt);
            return ERR_OK;
        }
    }

    public int voidPayment(int spec, String input) {
        switch (spec) {
            case 2:
                logger.info("Void amount: " + input);
                voidAmount = input;
                break;

            case 3:
                try {
                    logger.info("ctl.tran: " + ctl.tran);
                    logger.info("Void receipt number: " + input);
                    voidReceiptNumberECR = input;
                    EcrMarshallObject ecrMarshallObject = testEnvironment ? new EcrObjectMock() : new EcrObject();
                    ecrMarshallObject.setAmoutECR(voidAmount);
                    ecrMarshallObject.setVoidTransactionNumber(input);
                    ecrMarshallObject.setTransactionType(EcrObject.TRANSACTION_TYPE_SALE);
                    ecrMarshallObject.setEcrReceiptNumber(String.valueOf(ctl.tran));

                    if (!ecrMarshallObject.voidTransECR()) {
                        logger.error("Error during ECR Void");
                        return 121;
                    } else {
                        addReceiptValues(ecrMarshallObject);
                        authorizedAmount = Long.parseLong(ecrMarshallObject.getAmountECR());
                    }
                } catch (Exception e) {
                    logger.error("Error: ", e);
                }
                break;
            default:
                break;
        }
        return 0;
    }

    @Override
    public boolean settle() {
        logger.info("Enter");

        EcrMarshallObject ecrMarshallObject = testEnvironment ? new EcrObjectMock() : new EcrObject();

        boolean result = ecrMarshallObject.settleECR();
        itm.dpt = result ? 1 : 0;
        logger.info("itm.dpt: " + itm.dpt);

        IDC_write('z', tra.tnd, 2, "000", 1, 0);

        logger.info("Exit, result: " + result);
        return result;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public String getCardType() {
        return cardType;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    private void addReceiptValues(EcrMarshallObject ecrMarshallObject) {
        receiptData = new HashMap();
        addReceiptValues(NEW_RECEIPT_TYPE, ecrMarshallObject);
        addReceiptValues(SAME_RECEIPT_TYPE, ecrMarshallObject);
    }

    private void addReceiptValues(String type, EcrMarshallObject ecrMarshallObject) {
        logger.debug("Type: " + type);

        try {
            File file = new File(type);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            Vector voucher = new Vector();

            if (reader != null) {
                try {
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        line = manageMacro(line, ecrMarshallObject);
                        voucher.add(line);
                        logger.debug("Added line: " + line);
                    }
                } catch (Exception exception) {
                    logger.error("addReceiptValues exception : ", exception);
                    return;
                }
                receiptData.put(type, voucher);
            } else {
                logger.info("Error in addReceiptValues() , textVoucher is null check file S_PLU");
            }
        } catch (Exception e) {
            logger.error("Error on reading S_PLURCO.DAT file : ", e);
            return;
        }
    }

    private String manageMacro(String line, EcrMarshallObject ecrMarshallObject) {
        if (line.indexOf(MACRO_authorisation_number) >= 0) {
            logger.debug("found macro: " + MACRO_authorisation_number);
            line = line.substring(0, line.indexOf(MACRO_authorisation_number)) + ecrMarshallObject.getApprovalCodeECR() + line.substring(line.indexOf(MACRO_authorisation_number) + MACRO_authorisation_number.length());
        }
        if (line.indexOf(MACRO_amount) >= 0) {
            logger.debug("found macro: " + MACRO_amount);
            line = line.substring(0, line.indexOf(MACRO_amount)) + GdRegis.editMoney(0, ecrMarshallObject.getLongAmountECR()) + line.substring(line.indexOf(MACRO_amount) + MACRO_amount.length());
        }
        if (line.indexOf(MACRO_cc_number) >= 0) {
            logger.debug("found macro: " + MACRO_cc_number);
            line = line.substring(0, line.indexOf(MACRO_cc_number)) + ecrMarshallObject.getCardNumberECR() + line.substring(line.indexOf(MACRO_cc_number) + MACRO_cc_number.length());
        }
        if (line.indexOf(MACRO_card_scheme_name) >= 0) {
            logger.debug("found macro: " + MACRO_card_scheme_name);
            line = line.substring(0, line.indexOf(MACRO_card_scheme_name)) + ecrMarshallObject.getCardSchemaNameECR() + line.substring(line.indexOf(MACRO_card_scheme_name) + MACRO_card_scheme_name.length());
        }
        if (line.indexOf(MACRO_ecr_amount) >= 0) {
            logger.debug("found macro: " + MACRO_ecr_amount);
            line = line.substring(0, line.indexOf(MACRO_ecr_amount)) + editMoney(0, Long.parseLong(ecrMarshallObject.getAmountECR())) + line.substring(line.indexOf(MACRO_ecr_amount) + MACRO_ecr_amount.length());
        }
        if (line.indexOf(MACRO_ecr_receipt) >= 0) {
            logger.debug("found macro: " + MACRO_ecr_receipt);
            line = line.substring(0, line.indexOf(MACRO_ecr_receipt)) + ecrMarshallObject.getEcrReceiptNumber() + line.substring(line.indexOf(MACRO_ecr_receipt) + MACRO_ecr_receipt.length());
        }
        if (line.indexOf(MACRO_ecr_receipt_void) >= 0) {
            logger.debug("found macro: " + MACRO_ecr_receipt_void);
            line = line.substring(0, line.indexOf(MACRO_ecr_receipt_void)) + voidReceiptNumberECR + line.substring(line.indexOf(MACRO_ecr_receipt_void) + MACRO_ecr_receipt_void.length());
        }
        if (line.indexOf(MACRO_ecr_terminal_id) >= 0) {
            logger.debug("found macro: " + MACRO_ecr_terminal_id);
            logger.debug("TIDECR: " + ecrMarshallObject.getTIDECR());
            //    line = line.substring(0, line.indexOf(MACRO_ecr_terminal_id)) + veriFoneManager.getResponseMessageEcr() + line.substring(line.indexOf(MACRO_ecr_terminal_id) + MACRO_ecr_terminal_id.length());
            line = line.substring(0, line.indexOf(MACRO_ecr_terminal_id)) + ecrMarshallObject.getTIDECR() + line.substring(line.indexOf(MACRO_ecr_terminal_id) + MACRO_ecr_terminal_id.length());
        }
        if (line.indexOf(MACRO_transaction_id_Ecr) >= 0) {
            logger.debug("found macro: " + MACRO_transaction_id_Ecr);
            logger.debug("messNum: " + ecrMarshallObject.getMessNumECR());
            line = line.substring(0, line.indexOf(MACRO_transaction_id_Ecr)) + ecrMarshallObject.getMessNumECR() + line.substring(line.indexOf(MACRO_transaction_id_Ecr) + MACRO_transaction_id_Ecr.length());
        }

        return line;
    }

    @Override
    public String getTenderId() {
        return MARSHALL_TENDER_ID;
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
}