package com.ncr;

import com.ncr.eft.EftPluginManager;
import com.ncr.struc.Customer;

/*******************************************************************
 * electronic payment control data
 *******************************************************************/
class PayCards {
	/** nation code valid with bank orders **/
	int home = 280;
	/** currency code (ec track3) **/
	int currency;
	/** nation code (ec track3) **/
	int nation;
	/** valid thru (track2) **/
	int yymm;
	/** card sequence number (track3) **/
	int seqno;

	/** ec account number (track3) **/
	String acct = "";
	/** ec bank number (track3) **/
	String bank = "";
	/** ec card number (manual input) **/
	String card = "";
	/** ec check number (manual input) **/
	String cheque = "";
	/** ec customer number **/
	String custom = "";
	// TSC-MOD2014-AMZ#BEG
	/** credit card number --unused ? **/
	String credit = "";
	// TSC-MOD2014-AMZ#END
}

/*******************************************************************
 * cash denominations for loan, pickup, cashcount, float
 *******************************************************************/
class CshDenom {
	/** monitary amount of denomination **/
	long value;
	/** description of denomination (operator prompt) **/
	String text;
}

/*******************************************************************
 * messages on receipt selected by mode/actioncode
 *******************************************************************/
class MsgLines {
	/** triggering registry mode **/
	int mode;
	/** triggering actioncode **/
	int code;
	/** first line to print from mdac_txt **/
	int line;
	/** last line to print from mdac_txt **/
	int last;
	/** parameter not used ":" **/
	char logo;
	/** parameter not used " " **/
	char flag;
}

/*******************************************************************
 * slip print control info
 *******************************************************************/
class SlpLines {
	/** actioncode **/
	int code;
	/** lines to skip on top of form **/
	int top;
	/** last line available on form **/
	int end;
	/** "L" = print logo on top of form **/
	char logo;
	/** "-" = no receipt, "*" = slip instead **/
	char flag;
}

/*******************************************************************
 * end-of-day report request params
 *******************************************************************/
class EodTypes {
	/** report actioncode **/
	int ac;
	/** report subcode (0=XXXX, 1=*XXX, 2=**XX, 3=***X) **/
	int type;
	/** terminal selection (A=All consolidated, S=Single) **/
	char sel;
}

/*******************************************************************
 * static initialization of commonly used basic data structures
 *******************************************************************/
public abstract class Struc extends FmtIo implements Constant {
	public static String dspBmap, dspSins;
	public static LinIo dspLine = new LinIo("DSP", 0, 20);
	public static LinIo oplLine = new LinIo("OPL", 0, 20);
	public static LinIo hdrLine = new LinIo("HDR", 0, 20);
	public static LinIo cusLine = new LinIo("CUS", 0, 20);
	public static LinIo stsLine = new LinIo("STS", 0, 20);
	public static LinIo cntLine = new LinIo("CNT", 0, 20);
	public static LinIo idsLine = new LinIo("IDS", 0, 17);
	public static LinIo prtLine = new LinIo("PRN", 1, 42);

	public static Terminal ctl = new Terminal();
	public static Transact tra = new Transact(), dct;
	public static Itemdata itm;
	public static Itemdata pit;
	public static Itemdata plu;
	static Itemdata ref;
	static Itemdata dci;
	static Itemdata dlu = new Itemdata();
	public static Customer cus;
	static PayCards ecn = new PayCards();
	public static Monitors mon = new Monitors();

	public static ItalianBackGroundColor asr_ibkc = new ItalianBackGroundColor();

	public static EftPluginManager eftPluginManager = EftPluginManager.getInstance();
	public static GiftCardPluginManager giftCardManager = GiftCardPluginManager.getInstance();
	/** CandC, xCaRd, copy2, etc **/
	public static int options[] = new int[40];
	/** actual reason codes **/
	static int rcd_tbl[] = new int[10];
	/** denomination counters **/
	static int dnom_tbl[] = new int[32];
	/** tare table **/
	static int tare_tbl[] = new int[100];
	/** age restriction table **/
	static int ckr_age[] = new int[10];
	/** age restriction table **/
	static int cus_age[] = new int[10];

	/** cash preset values **/
	static int csh_tbl[] = new int[10];
	/** department preset table **/
	static int dir_tbl[] = new int[15];
	/** department descriptions **/
	static String dir_txt[] = new String[15];
	/** plu preset table **/
	static String plu_tbl[] = new String[15];
	/** plu descriptions **/
	static String plu_txt[] = new String[15];
	/** list selector table **/
	static String sel_tbl[][] = new String[15][8];
	/** list descriptions **/
	static String sel_txt[][] = new String[15][8];

	/** alt tl / tender keys 1-F **/
	static int tnd_tbl[] = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, };

	public static String key_txt[] = /* touch key inscriptions */
	{ " Confirm  ", "  Clear   ", "  Cancel  ", "  Select  ", };

	static String vrs_tbl[] = { "EXE Version ", "ORG Version ", "VRS Scale   ", "CRC Scale   ", };
	static int version[] = { 90, 0, 0, 23718 };

	static String chk_nbr[] = { "Zero*", "*One*", "*Two*", "Three", "Four*", "Five*", "*Six*", "Seven", "Eight",
			"Nine*", };

	static String ean_weights = "31313131313131313131";
	static String lbl_weights = "31731731731731731731";

	public static TaxRates vat[] = new TaxRates[8];
	public static TndMedia tnd[] = new TndMedia[40];
	static MsgLines mat[] = new MsgLines[5];
	static SlpLines slp[] = new SlpLines[50];
	static EodTypes eod[] = new EodTypes[10];

	static String ean_16spec[] = new String[32];
	static String msr_20spec[] = new String[32];

	public static String mnt_line = "LAN PLU MAINTENANCE A00000 C00000 D00000";
	public static String stl_line = "                     ------- -----------";
	public static String chk_line = "NCR GREAT DEALER 90    86156 AUGSBURG   ";
	public static String cpy_line = "valid with the original receipt only !!!";
	public static String iht_line = "document for internal use / no receipt !";
	public static String inq_line = "Ctl Dsc Vat Mm/T  Unit/Pkg   Link   Dept";
	public static String trl_line = "**** ****/***/***   dd.mm.yy hh:mm AC-**";
	public static String fso_line = "01 collection of frequentShopper options";
	public static String ecu_line = "B954B000E954E000-000-000-000-000-000-000";

	/** alphanumeric keyboards **/
	static String kbd_alpha[] = { "  Alpha-   Keyboard  Missing! ParamDABC0",
			"  Alpha-   Keyboard  Missing! ParamDABC1", "  Alpha-   Keyboard  Missing! ParamDABC2",
			"ABCDEFGHIJKLMNOPQRST  UVWXYZ  0123456789", /* Italian Fiscal Id */
	};

	/** target terminal presets **/
	public static int note_tbl[] = new int[10];
	/** contents of notices **/
	public static String note_txt[] = new String[10];
	/** bank order form template **/
	public static String bank_txt[] = new String[16];
	/** EURO club advertizing **/
	public static String euro_txt[] = new String[16];
	/** messages by mode/ac **/
	public static String mdac_txt[] = new String[16];
	/** LAN offline apology **/
	public static String offl_txt[] = new String[16];
	/** total discount statement **/
	public static String save_txt[] = new String[16];
	/** receipt header lines **/
	public static String head_txt[] = new String[16];
	/** merchandize messages **/
	public static String mess_txt[] = new String[16];
	/** charge/delivery specials **/
	public static String spec_txt[] = new String[16];
	/** tax exempt certificate **/
	public static String xtax_txt[] = new String[16];
	/** trans view column header **/
	public static String view_txt[] = { "Item.Description....   Quantity     Pric",
			"e     Amount TagXXXXXXXXXXXXXXXXXXXXXXXX", };
	public static String specialHeader[][] = new String[10][10];
	/** qr message error template **/
	public static String zmsg_txt[] = new String[16];
	/** qr content **/
	public static String zqrc_txt[] = new String[16];

	/** NCRMEA-2022-002
	 * GC Redemption error template **/
	public static String gcer_txt[] = new String[16];

	static {
		TndMedia.tbl = tnd; /* for inner base reference */
	}
}
