package com.ncr.eft;

import com.ncr.Itemdata;
import com.ncr.LinIo;
import com.ncr.Terminal;

import java.util.Properties;

public interface EftPlugin {
	String GEIDEA_TENDER_ID = "J";
	String ALSHAYA_TENDER_ID = "K";
	String MARSHALL_TENDER_ID = "M";
	String EYEPAY_TENDER_ID = "N";
	String TONETAG_TENDER_ID = "O";
	String NEW_GEIDEA_TENDER_ID = "Q";
	String APEX_TENDER_ID = "T";
	String KNET_TENDER_ID ="U";
	String MADA_TENDER_ID = "V";
	String CREDIMAX_TENDER_ID = "W";
	String NO_TENDER_ID = "X";
	String PHILOBROKER_TENDER_ID = "Z";

	int CARD_NUM_INDEX = 0;
	int TERMIINAL_ID_INDEX = 1;
	int AUTH_CODE_INDEX = 2;
	int TRANSACTION_ID_INDEX = 3;
	int FLAGS_SIZE = 4;

	// Return codes
	int ERR_OK = 0;
	int ERR_NOTCONNECTED = 70;
	int ERR_TIMEOUTTRANSACTION = 71;
	int ERR_NOTAUTHORIZED = 72;
	int ERR_RESPONSE = 73;
	int ERR_INVALID_CARD = 92;
	int ERR_CARD_EXPIRED = 93;
	int ERR_COMMUNICATION_FAILURE = 94;
	int ERR_DECLINED = 95;
	int ERR_INCORRECT_PIN = 96;
	int ERR_VOID = 121;
	int ERR_NOT_INITiALIZED=158;			//TSC KNET eSocket POS Integration


	String NEW_RECEIPT_TYPE = "S_PLURCO.DAT";
	String SAME_RECEIPT_TYPE = "S_PLURCP.DAT";
	String ERR_RECEIPT_TYPE = "S_PLURCE.DAT";

	/* Macro for payment voucher */
	String EFT_CC_NUMBER = "$CC_NUMBER$";
	String EFT_AMOUNT = "$AMOUNT$";
	String EFT_AUTH_NUMBER = "$AUTH_NUMBER$";
	String EFT_CARD_SCHEME = "$SCHEME_NAME$";
	String EFT_RECEIPT_NUMBER = "$ECR_RECEIPT$";
	String EFT_RECEIPT_VOID = "$ECR_RECEIPT_VOID$";
	String EFT_TERMINAL_ID = "$ECR_TERMINAL_ID$";
	String EFT_RRN_NUMBER = "$RRN_NUMBER$";
	String EFT_CASE_NUMBER = "$CASE_NUMBER$";
	String EFT_MERCHANT_ID = "$MID$";
	String SKIP_LINE = "$$";
	String NEW_RECEIPT = "$NEW_RECEIPT$";
	String EFT_VOID ="$EFT_VOID$";;

	int pay(Itemdata itm, Terminal ctl, LinIo line);
	int voidPayment(int spec, String input);
	boolean settle();
	void init(Properties props);
	void stop(Terminal ctl);

	void printVouchers(String type);
	void resetVouchers();
	void loadEftTerminalParams(int line, String txt);
	void addReceiptValues(String line);
	String getAuthorizationCode();
	String getCardNumber();
    String getCardType();
    String getTerminalId();
    String getTenderId();
    long getAuthorizedAmount();
	String getReceiptNumber();
	String getRrn();
    boolean isIdcEnabled(int index);
	boolean isReceiptEnabled(int index);

	boolean isVoidCapable();
	boolean isVoidPerformedOnVoidTransaction();
	boolean isAmountRequiredOnVoid();
	boolean isDateRequiredOnVoid();
	boolean isSettlementCapable();
	boolean isSettlementEnabledAtEod();
	String managePluginMacro(String line);
}