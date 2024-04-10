package com.ncr.eft;

import ECRWrapper.DLLLoader;
import ECRWrapper.TxnInput;
import ECRWrapper.TxnOutput;
import com.ncr.*;
import org.apache.log4j.Logger;

import java.util.Properties;

import static com.ncr.Itmdc.IDC_write;


public class CredimaxEftPlugin extends GenericEftPlugin {
    private static final Logger logger = Logger.getLogger(CredimaxEftPlugin.class);
    private static boolean installed = false;
    private static String SALE = "1";

    //Properties variables
    private boolean testEnvironment = false;
    private static boolean settlementEnabledAtEod = false;

    //Void variables
    private String voidAmount = "";
    private String original_RCT = "";

    public CredimaxEftPlugin() {
        terminalId = editNum(ctl.reg_nbr, 8);
    }

    public void loadEftTerminalParams(int line, String txt) {
        super.loadEftTerminalParams(line, txt);
        //EFTW0:11000000:COM18:0000000000000000:00000011 MAYBE
        //EFTW0:1100000000000000000000000000000000000011
        switch (line) {
            case 0:
                logger.debug("EFTW0: " + txt);
                settlementEnabledAtEod = txt.charAt(38) == '1';
                testEnvironment = txt.charAt(39) == '1';

                logger.info("settlementEnabledAtEod :> " + settlementEnabledAtEod);
                logger.info("testEnvironment :> " + testEnvironment);
                break;
            default:
                break;
        }
    }

    @Override
    public void init(Properties props) {
        logger.info("ENTER");

        try {
            logger.debug("TRY DO SETUP ...");

            TxnOutput objoutput = new TxnOutput();

            if (!testEnvironment)
                DLLLoader.VFI_LoadSetting(objoutput);

            String response = objoutput.VFI_RespCode;
            logger.info("Response Code: " + response);
            logger.info("Response Message: " + objoutput.VFI_RespMess);

            if (response.equals("00")) installed = true;
            logger.info("Installed result -> " + installed);
        } catch (Exception e) {
            logger.error(e);
            installed = false;
            logger.info("Installed result -> " + installed);
            if (testEnvironment)
                installed = true;
        }
        logger.info("EXIT");
    }

    @Override
    public int pay(Itemdata itm, Terminal ctl, LinIo line) {
        logger.info("SALE - ENTER");
        int sts = ERR_OK;

        if (!installed && !testEnvironment) {
            logger.error("PinPad not connected");
            return ERR_NOTCONNECTED;
        }

        TxnOutput objoutput = new TxnOutput();
        TxnInput objinput = new TxnInput();
        boolean res = false;

        objinput.VFI_TransType = SALE;
        logger.info("VFI_TransType:     " + SALE);
        objinput.VFI_Amount = String.valueOf(itm.amt);
        logger.info("VFI_Amount:        " + itm.amt);
        objinput.VFI_CashAmount = "";
        logger.info("VFI_CashAmount:    " + "");
        objinput.VFI_AdditionalInfo = "";
        logger.info("VFI_AdditionalInfo: " + "");
        objinput.VFI_ECRRcptNum = editNum(ctl.tran, 4);
        logger.info("VFI_ECRRcptNum:    " + editNum(ctl.tran, 4));

        if (!testEnvironment) {
            res = DLLLoader.VFI_GetAuth(objinput, objoutput);
        }

        logger.info("Response Code: " + objoutput.VFI_RespCode);
        logger.info("Response Message: " + objoutput.VFI_RespMess);

        if ((!res || !objoutput.VFI_RespMess.isEmpty()) && !testEnvironment) {
            gui.clearLink(objoutput.VFI_RespMess.toString(), 1);
            logger.error("Transaction Not Approved");
            return ERR_DECLINED;
        }

        if (testEnvironment && !res) {
            objoutput.VFI_ApprovalCode = "123456789";
            objoutput.VFI_Amount = String.valueOf(itm.amt / 100);
            objoutput.VFI_CardNo = "1324123412341234";
            objoutput.VFI_CardTypes = "MASTER CARD";
            objoutput.VFI_TID = "123123123";
            objoutput.VFI_ECRRcptNum = "1234";
            objoutput.VFI_RetrievalRefNo = "123123";
            objoutput.VFI_MerchantID = "123123123";
        }

        authorizationCode = objoutput.VFI_ApprovalCode;
        authorizedAmount = (long) Double.parseDouble(objoutput.VFI_Amount) * (long) Math.pow(10, tnd[0].dec);
        cardNumber = objoutput.VFI_CardNo;
        cardType = objoutput.VFI_CardTypes;
        terminalId = objoutput.VFI_TID;
        receiptNumber = objoutput.VFI_ECRRcptNum;
        rrn = objoutput.VFI_RetrievalRefNo;
        mid = objoutput.VFI_MerchantID;

        itm.amt = authorizedAmount;
        addReceiptValues();

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
                voidAmount = input;
                break;
            case 3:
                logger.info("Void Original RCT: " + input);
                original_RCT = input;

                if (!installed && !testEnvironment) {
                    logger.error("PinPad not connected");
                    return ERR_NOTCONNECTED;
                }
                TxnOutput objoutput = new TxnOutput();
                TxnInput objinput = new TxnInput();
                boolean res = false;

                objinput.VFI_VoidReceiptNum = original_RCT;
                logger.info("VFI_VoidReceiptNum:     " + original_RCT);
                objinput.VFI_Amount = String.valueOf(itm.amt);
                logger.info("VFI_Amount:        " + itm.amt);
                objinput.VFI_CashAmount = "";
                logger.info("VFI_CashAmount:    " + "");
                objinput.VFI_AdditionalInfo = "";
                logger.info("VFI_AdditionalInfo: " + "");
                objinput.VFI_ECRRcptNum = editNum(ctl.tran, 4);
                logger.info("VFI_ECRRcptNum:    " + editNum(ctl.tran, 4));

                if (!testEnvironment) {
                    res = DLLLoader.VFI_VoidTrans(objinput, objoutput);
                }

                logger.info("Response Code: " + objoutput.VFI_RespCode);
                logger.info("Response Message: " + objoutput.VFI_RespMess);

                if ((!res || !objoutput.VFI_RespMess.isEmpty()) && !testEnvironment) {
                    gui.clearLink(objoutput.VFI_RespMess.toString(), 1);
                    logger.error("Transaction Not Approved");
                    return ERR_DECLINED;
                }

                if (testEnvironment && !res) {
                    objoutput.VFI_ApprovalCode = "123456789";
                    objoutput.VFI_Amount = String.valueOf(itm.amt / 100);
                    objoutput.VFI_CardNo = "1324123412341234";
                    objoutput.VFI_CardTypes = "MASTER CARD";
                    objoutput.VFI_TID = "123123123";
                    objoutput.VFI_ECRRcptNum = "1234";
                    objoutput.VFI_RetrievalRefNo = "123123";
                    objoutput.VFI_MerchantID = "123123123";
                }

                authorizationCode = objoutput.VFI_ApprovalCode;
                authorizedAmount = (long) Double.parseDouble(objoutput.VFI_Amount) * (long) Math.pow(10, tnd[0].dec);
                cardNumber = objoutput.VFI_CardNo;
                cardType = objoutput.VFI_CardTypes;
                terminalId = objoutput.VFI_TID;
                receiptNumber = objoutput.VFI_ECRRcptNum;
                rrn = objoutput.VFI_RetrievalRefNo;
                mid = objoutput.VFI_MerchantID;

                itm.amt = authorizedAmount;
                addReceiptValues();

                sts = ERR_OK;

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
        boolean sts = false;

        if (!installed && !testEnvironment) {
            logger.error("PinPad not connected");
            return false;
        }

        TxnOutput objoutput = new TxnOutput();
        TxnInput objinput = new TxnInput();
        boolean res = false;

        objinput.VFI_TID = terminalId;
        logger.info("VFI_TID:     " + terminalId);

        sts = DLLLoader.VFI_Settle(objinput, objoutput);

        logger.info("ResponseCode       -> " + objoutput.VFI_RespCode);
        logger.info("ResponseMessage    -> " + objoutput.VFI_RespMess);

        if (sts) {
            authorizationCode = objoutput.VFI_ApprovalCode;
            authorizedAmount = (long) Double.parseDouble(objoutput.VFI_Amount) * (long) Math.pow(10, tnd[0].dec);
            cardNumber = objoutput.VFI_CardNo;
            cardType = objoutput.VFI_CardTypes;
            terminalId = objoutput.VFI_TID;
            receiptNumber = objoutput.VFI_ECRRcptNum;
            rrn = objoutput.VFI_RetrievalRefNo;
            mid = objoutput.VFI_MerchantID;

            itm.amt = authorizedAmount;
            addReceiptValues();

            IDC_write('z', tra.tnd, 2, "000", 1, 0);
        } else {
            logger.error("Transaction Not Approved");
            sts = false;
        }

        logger.info("RECON - EXIT. status: " + sts);
        return sts;
    }

    @Override
    public String getTenderId() {
        return CREDIMAX_TENDER_ID;
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