package com.ncr;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*******************************************************************
 * transaction control data
 *******************************************************************/
public class Transact {
	public static final int NORMAL = 0;
	public static final int ECOMMERCE = 1;
	public static final int SIMPLIFIED_INVOICE = 2;
	public static final int B2B_INVOICE = 3;
	public static final int SPECIAL_4 = 4;
	public static final int SPECIAL_5 = 5;
	public static final int SPECIAL_6 = 6;
	public static final int SPECIAL_7 = 7;
	public static final int SPECIAL_8 = 8;
	public static final int SPECIAL_9 = 9;

	/** registry mode 0=money, 1=sales, 2=cancel, 5=inventory, 6=ledger, 7=layaway, 8=suspend **/
	public int mode;
	/** action code 00=sales, 01=open, 02=close, etc **/
	public int code;
    public boolean cleanPo = false;
	public Set<Integer> special = new HashSet<Integer>();
	/** subcode (report type all, single, etc) **/
	public int subc;
	/** index of title in text table (menu section) **/
	public int head;
	/** negative transaction preselections (void, return) **/
	public int spf1;
	/** discount preselections (employee, customer) **/
	public int spf2;
	/** other preselections (tax exempt, surcharge, etc) **/
	public int spf3;
	/** print preselections (no receipt, deferred receipt, slip instead **/
	public int slip;
	/** prerequest copy of tenderization on slip **/
	public int tslp;
	/** personalized sales (1=anonymous, 2=customer#, 3=in file) **/
	public int stat;
	/** record number of salesperson in SLM data file **/
	public int slm;
	/** record number of sales total line in GPO data file **/
	public int gpo;
	/** transaction resumption mode **/
	public int res;
	/** time of transaction start in seconds **/
	public int tim;
	/** tenderization phase (1 after 1st tender) **/
	public int tnd;
	/** customer's date of birth yymmdd **/
	public int age;
	/** supervisor number closing a cashier **/
	public int who;
	/** terminal number selected in reports **/
	public int comm;
	/** rate of employee/customer discount (1 decimal place assumed) **/
	public int rate;
	/** rate of surcharge on delivery (1 decimal place assumed) **/
	public int xtra;
	/** salesperson short number **/
	public int slm_nbr;
	/** salesperson employee number **/
	public int slm_prs;
	/** signed count of items **/
	public int cnt;
	/** signed amount of sales total **/
	public long amt;
	/** signed count of items with surcharge **/
	public int chg_cnt;
	/** signed amount of surcharge **/
	public long chg_amt;
	/** signed amount of sales with possible surcharge **/
	public long chg_sls;
	/** signed count of items with employee/customer discount **/
	public int dsc_cnt;
	/** signed amount of employee/customer discount **/
	public long dsc_amt;
	/** signed amount of sales with possible discount **/
	public long dsc_sls;
	/** signed count of items with possible bonuspoints **/
	int pnt_cnt;
	/** signed amount of bonuspoints **/
	public int pnt;
	/** signed amount of sales with possible bonuspoints **/
	long pnt_sls;
	/** signed count of items before latest subtotal **/
	int sub_cnt;
	/** signed amount of items before latest subtotal **/
	long sub_amt;
	/** signed amount of discount on totals (dept, si, tl) **/
	long rbt_amt;
	/** signed amount of all auto discounts on totals (rbt + dp) **/
	long tld_amt;
	/** signed balance due **/
	public long bal;
	/** signed amount of total cashback **/
	long csh_bck;
	/** employee/customer or other id number **/
	public String number = "";
	/** tax exemption permit number **/
	String taxidn = "";

	public int prpnt;	// PSH-ENH-001-SBE
	public long gctnd;	// PSH-ENH-001-SBE

	public ItemVector vItems = new ItemVector();
	ItemVector vItems_k = new ItemVector(); //DMA_VAT-DISTRIBUTION#A
	ItemVector vTrans = new ItemVector();
	// EMEA-UPB-DMA#A BEG
    public ArrayList<UPBTrans> itemsVsUPB = new ArrayList<UPBTrans>();
	// EMEA-UPB-DMA#A END
	boolean successRedeemPoint = true;
	boolean successTransaction = true;
	public int bmpSequence = 0;
	public int eCommerce = 0;
	boolean print = true;
	private String referenceNumber = "";
	private String referenceDate = "";
	public boolean giftCard = false;

	/***************************************************************************
	 * check the transacton state
	 * 
	 * @return true=in progress, false=not begun
	 ***************************************************************************/
	public boolean isActive() {
		return slm_nbr > 0;
	}

	public String getReferenceNumber() {
		return referenceNumber;
	}

	public void setReferenceNumber(String referenceNumber) {
		this.referenceNumber = referenceNumber;
	}

	public String getReferenceDate() {
		return referenceDate;
	}

	public void setReferenceDate(String referenceDate) {
		this.referenceDate = referenceDate;
	}

	public boolean isSale() {
		return code == 0 || code == 3 || code == 4;
	}

	public boolean IsSuspend() {
		return IsTransactionMode(Struc.M_SUSPND);
	}

	public boolean IsResume() {
		return IsTransactionMode(Struc.M_RESUME);
	}

	private boolean IsTransactionMode(int _mode) {
		return this.mode == _mode;
	}

	public boolean testStarted = false;
}

