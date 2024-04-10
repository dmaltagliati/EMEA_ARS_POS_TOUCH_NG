package com.ncr;

import com.ncr.common.data.AdditionalInfo;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/*******************************************************************
 * sales/tender item control data
 *******************************************************************/
public class Itemdata implements Cloneable {
	/** id of item in vector (S, L, P, M, C) **/
	public char id;
	/** index of item in vector **/
	public int index = -1;
	/** record number of empl/cust discount in IDC data file **/
	public int idc;
	/** plu/dpt properties F_?????? **/
	public int flag;
	/** extended plu/dpt properties F_?????? **/
	public int flg2;
	/** plu/dpt type **/
	public int type;
	/** index into age control table **/
	public int ages;
	/** negative sales preselections (void, return, etc) **/
	public int spf1;
	/** sales reduction preselections (price override, item discounts, etc) **/
	public int spf2;
	/** type of mark down at stock (red price, price verify, set price, etc) **/
	public int spf3;
	/** selective itemizer (index into discount limitation table) **/
	public int sit;
	/** vat code (index 0-7 into value-added-tax table) **/
	public int vat;
	/** sales category in financial reports **/
	public int cat;
	/** mix/match code **/
	public int mmt;
	/** campaign number causing bonuspoints **/
	public int cmp_nbr;
	/** department number **/
	public int dpt_nbr;
	/** salesperson number **/
	public int slm_nbr;
	/** halo / lalo **/
	public int halo;
	/** item discount rate (1 decimal place assumed) **/
	public int rate;
	/** package price = plu price * unit (1 decimal place assumed) **/
	public int unit = 10;
	/** short number of linked refund item **/
	public int link;
	/** tare code = index into tare table **/
	public int tare;
	/** status (linked=2, scanned=1) **/
	public int stat;
	/** record number of department in DPT data file **/
	public int dpt;
	/** record number of salesperson in SLM data file **/
	public int slm;
	/** record number of salesperson in SLM data file **/
	public int rcd;
	/** reason code **/
	public int qty;
	/** number of decimal quantity entries **/
	public int prm;
	/** signed count of items repeated **/
	public int rpt;
	/** decimal quantity (3 decimal places assumed) **/
	public int dec = 1000;
	/** price is extended, unit price unavailable **/
	public int ext;
	/** index into tender table **/
	public int tnd;
	/** price per unit **/
	public int price;
	/** signed count of items **/
	public int cnt;
	/** signed item price (quantities * unit price * unit) **/
	public long amt;
	/** sales commission on item/package **/
	public int prcom;
	/** signed commission **/
	public long com;
	/** manual discount per unit **/
	public int prcrd;
	/** signed manual discount on item/package **/
	public long crd;
	/** price comes from label, w/o verification on return **/
	public int prlbl;
	/** unit price after price overwrite at pos **/
	public int prpos;
	/** signed profit/loss by price overwrite on item/package **/
	public long pos;
	/** unit price after markdown at stock **/
	public int prpov;
	/** signed price difference by markdown on item/package **/
	public long pov;
	/** signed employee/customer discount on item/package **/
	public long dsc;
	/** bonuspoints per unit **/
	public int prpnt;
	/** signed count of bonuspoints **/
	public int pnt;
	/** signed rewarded quantity **/
	public int rew_qty;
	/** signed rewarded amount **/
	public long rew_amt;
	/** environmental tax code (flat rate) **/
	public int flat;
	/** environmental tax per unit **/
	public long flatax;

	/** item marker (e/c, repeat, markdown) **/
	public char mark = ' ';
	/** special price from main plu file (by D=date, T=time, X=quantity, P=prize) **/
	public char spec = ' ';
	/** item number (plu right-justified, sku left-justified) **/
	public String number = "";
	/** plu number as entered (short code, ean/upc, complete price label) **/

	public String authNum = ""; //INSTASHOP-FINALIZE-CGA#A
	public String eanupc = "";
	/** original plu number before chaining **/
	public String chaino = "";
	/** serial number **/
	public String serial = "";
	/** promotion code **/
	public String promo = "";
	/** plu/dpt description **/
	public String text = "";
	/** sales qualifications text **/
	public String qual = "";
	/** package type (ea=each, kg, etc) **/
	public String ptyp = "  ";
	/** reserved for customization (10 chars) **/
	public String xtra;

	public int meal = 0;

	/** E-record: package size **/
	public int ePack;
	/** E-record: unit price reference **/
	public int eUref;
	/** E-record: unit of measure **/
	public String eUofm = "  ";
	/** E-record: product family code **/
	public int eFamily;
	/** E-record: auxiliary text **/
	public String eXline = "";
	/** E-record: not used **/
	public char eFiller;
	/** E-record: promotional price **/
	public int ePromo;
	public int providerID;
	public int operationType;
	public long operationID;

	/** GiftCard flag **/
	public char gCard;
	/** GiftCard extended description line **/
	public String gCardDsc = "";
	public String gCardBal = "";
	/** GiftCard serial number **/
	public String giftCardSerial = "";
	/** GiftCard Topup flag **/
	public Boolean giftCardTopup = false;
	/** GiftCard payment flag **/
	public Boolean gCardPayment = false;
	/** Philoshopic transaction number **/
	public String gCardTransaction = "";
	public String redemptionDsc = "";
	public String redemptionTransaction = "";
	// PSH-ENH-001-AMZ#END
    //PSH-ENH-20151120-CGA#A BEG
    /** Utility range max price **/
	public int utilityMaxPrice = 0;
    /** Utility transaction number **/
	public String utilityTransaction = "";
    /** Utility serial number **/
	public String utilitySerial = "";
    /** Utility item description **/
	public String utilityName = "";
    /** Utility plu code **/
	public String utilityCode = "";
    /** Utility english text for print voucher **/
	public String utilityEnglishText = "";
    /** Utility arab text for print voucher **/
	public String utilityArabicText = "";
	/** Utility pin **/
	public String utilityPin = "";
    //PSH-ENH-20151120-CGA#A END
	// SPINNEYS-ENH-DSC-SBE#A BEG
	public boolean prchange = false;
	// SPINNEYS-ENH-DSC-SBE#A END
	public boolean coupon = false;
	public int originalPrice;
	public String crdDescription = "";
    // TSC-ENH2014-1-AMZ#BEG
	/** Expiry date if present **/
	String expdate = "";
	// TSC-ENH2014-1-AMZ#END
	public long round;
	public int discountFlag;
	public boolean qrcode;
	public EcommerceInfo ecommerceInfo = new EcommerceInfo();
    public String accountType;
    public boolean accountNumberRequired;
	//NCRMEA-2022-002
    public String referenceNumberGC;
    private String originalTransactionNumberGC;
	private List<AdditionalInfo> additionalInfos = new ArrayList<AdditionalInfo>();
	public long mmDiscount;
	public int mmQty;

	public Itemdata() {}

	public Itemdata(String text) {
		this.text = text;
	}

	/** set all signs depending on negative preselections **/
	void sign() {
		for (int ind = 128; ind > 0; ind >>= 1) {
			if ((spf1 & ind) == 0)
				continue;
			amt = -amt;
			cnt = -cnt;
			dsc = -dsc;
			pos = -pos;
			pov = -pov;
			com = -com;
		}
	}

	/** true if item is a plu **/
	boolean isPlu() {
		if (number.length() < 16)
			return false;
		return number.charAt(0) == ' ' && number.charAt(15) > ' ';
	}

	/** clone an item **/
	public Itemdata copy() {
		try {
			return (Itemdata) super.clone();
		} catch (Exception e) {
		}
		return null;
	}

	private boolean IsFlag(int val) {
		return (val & this.flag) > 0;
	}

	public boolean IsNegativeSalesItem() {
		return IsFlag(Struc.F_NEGSLS);
	}

	public boolean IsDepositItem() {
		return IsFlag(Struc.F_DPOSIT);
	}

	public boolean IsWeightItem() {
		return IsFlag(Struc.F_WEIGHT);
	}

	public boolean IsSpecialSaleItem() {
		return IsFlag(Struc.F_SPCSLS);
	}

	public boolean IsOnslipItem() {
		return IsFlag(Struc.F_ONSLIP);
	}

	public boolean IsSkipSKUItem() {
		return IsFlag(Struc.F_SKPSKU);
	}

	public boolean IsDecimalQuantityItem() {
		return IsFlag(Struc.F_DECQTY);
	}

	public String getOriginalTransactionNumberGC() {
		return originalTransactionNumberGC;
	}

	public void setOriginalTransactionNumberGC(String originalTransactionNumberGC) {
		this.originalTransactionNumberGC = originalTransactionNumberGC;
	}

	public List<AdditionalInfo> getAdditionalInfos() {
		return additionalInfos;
	}

	public void setAdditionalInfos(List<AdditionalInfo> additionalInfos) {
		this.additionalInfos = additionalInfos;
	}

	private int itemType = 0;

	public int getItemType() {
		return itemType;
	}

	public void setItemType(int itemType) {
		this.itemType = itemType;
	}

	public static final int VOIDED = 1000;
	public static final int ITEM = 1;
	public static final int DISCOUNT = 2;
	public static final int TENDER = 3;
	public static final int SUBTOTAL = 4;
	public static final int CUSTOMER = 5;
	public static final int MESSAGE = 6;
	public static final int INQUIRYITEM = 7;
	public static final int COUPON = 8; // MMS-ECOUPONING#A
	public static final int POINT = 9;
	public static final int FISCALCODE = 10; // FRG-DMO-CF#A
	public static final int LOTTERY = 13; // MMS-LOTTERY#A
	public static final int COUPON_MESSAGE = 14;

	public long entryId; // EntryID dell'articolo corrente
	public long originalEntryId; // EntryID dell'articolo stornato o annullato
	public String dump() {
		return "entryId=" + entryId + "; originalEntryId=" + originalEntryId + "; number=" + number + "; text=" + text
				+ "; itemType=" + itemType + "; qty=" + qty + "; price=" + price + "; amt=" + amt + "; spf1=" + spf1;
	}

	public boolean isInquiryItem() {
		return (itemType % VOIDED) == INQUIRYITEM;
	}
	public boolean isItem() {
		return (itemType % VOIDED) == ITEM;
	}
	public boolean isDiscount() {
		return (itemType % VOIDED) == DISCOUNT;
	}
	public boolean isCustomer() {
		return (itemType % VOIDED) == CUSTOMER;
	}
	public boolean isVoided() {
		return (itemType > VOIDED);
	}
	public boolean isMessage() {
		return (itemType % VOIDED) == MESSAGE;
	}
	public boolean isTender() {
		return (itemType % VOIDED) == TENDER;
	}
	public boolean isSubtotal() {
		return (itemType % VOIDED) == SUBTOTAL;
	}
	public boolean isPoint() {
		return (itemType % VOIDED) == POINT;
	}
	public boolean isFiscalCode() {
		return (itemType % VOIDED) == FISCALCODE;
	}

	public void setVoided() {
		if (!isVoided()) {
			this.itemType += VOIDED;
		}
	}


	public LinkedList promotionListGui = new LinkedList();

	String dscDescription = "";
	public String getDscDescription() {
		return dscDescription;
	}

	public void setDscDescription(String dscDescription) {
		this.dscDescription = dscDescription;
	}
}

