package com.ncr;

import com.ncr.ecommerce.ECommerceManager;

/*******************************************************************
 *
 * tender table entry
 *
 *******************************************************************/
public class TndMedia extends FmtIo {

    /**
     * smallest coin
     **/
    public int coin;

    // TSC-ENH2014-3-AMZ#BEG
    public boolean customerFavour;
    // TSC-ENH2014-3-AMZ#END
    /**
     * 0=controlled, 4=uncontrolled
     **/
    public int ctrl = 4;
    /**
     * decimal places in currency
     **/
    public int dec;
    /**
     * tender flags
     **/
    public int flag;
    /**
     * function lockout mask
     **/
    public int flom;
    /**
     * second tender flag byte
     **/
    public int flg2;
    /**
     * item count available
     **/
    public int icnt;
    /**
     * cashdrawer number
     **/
    public int till = 1;
    /**
     * lead currency
     **/
    public int club;
    /**
     * foreign currency base for exchange value
     **/
    public int unit;
    /**
     * decimal places in exchange rate
     **/
    public int xflg;
    /**
     * auto discount rate
     **/
    public int rate;
    /**
     * auto surcharge rate
     **/
    public int xtra;
    /**
     * amount of money in cashdrawer
     **/
    public long alert;
    /**
     * foreign currency exchange value
     **/
    public  long value;
    /**
     * tender limitations
     **/
    public long limit[] = new long[9];
    private char type = 'A';
    /**
     * national currency symbol
     **/
    public char xsym = ' ';
    /**
     * international currency symbol (3 chars)
     **/
    public String symbol = "";
    /**
     * tender description (reports)
     **/
    public String text = "";
    /**
     * tender description (receipt)
     **/
    public String tx20 = "";
    /**
     * foreign currency rate description (8 chars)
     **/
    public String xtext;
    /**
     * denomination table
     **/
    public CshDenom dnom[] = new CshDenom[32];
    // TSC-MOD2014-AMZ#BEG
    /**
     * black list checking
     **/
    public boolean blackListed = false;
    /**
     * black list checking
     **/
    public boolean toSlip = false;

    /**
     * table base ref for lead currency access
     **/
    public static TndMedia tbl[] = null;

    /**************************************************************************
     * find tender of given type
     *
     * @param type
     *            tender type
     * @return tender number (0 = not found)
     ***************************************************************************/
    public static int find(char type) {
        int ind = tbl.length;
        while (--ind > 0 && tbl[ind].getType() != type)
            ;
        return ind;
    }

    /***************************************************************************
     * Constructor
     ***************************************************************************/
    public TndMedia() {
        int ind = dnom.length;
        while (ind > 0)
            dnom[--ind] = new CshDenom();
    }

    /**************************************************************************
     * initialize tender with params
     *
     * @param sc
     *            subcode
     * @param ptr
     *            S_REG data record
     ***************************************************************************/
    public void init(int sc, LocalREG ptr) {
        if (sc == 1) {
            text = ptr.text;
            icnt = ptr.flag & 3;
            dec = tbl[0].dec;
            xsym = tbl[0].xsym;
            symbol = tbl[0].symbol;
        } else if (sc < 6)
            ctrl = 0;
        if (sc == 8) {
            dec = ptr.rate % 10;
            club = ptr.rate / 10;
            xflg = ptr.tflg;
            xsym = ptr.text.charAt(11);
            xtext = ptr.text.substring(0, 8);
            symbol = ptr.text.substring(8, 11).trim();
            unit = ptr.block[0].items;
            value = unit > 0 ? ptr.block[0].total : 0;
        }
        if (sc == 15)
            rate = ptr.rate; /* auto discount */
        if (sc == 16)
            xtra = ptr.rate; /* auto surcharge */
    }

    /***************************************************************************
     * round tender to smallest coin
     *
     * @param total
     *            the monitary amount in tender currency
     * @return the rounded result (0 no, 1-4 down, 5-9 up)
     ***************************************************************************/
    public long round(long total) {
        if (coin < 2)
            return total;
        if (customerFavour) {
            if (GdSarawat.getRoundReturnsCustomerFavour()) {
                if (Struc.tra.bal < 0) {
                    return ((total / coin) + ((total % coin) > 0 ? 1 : 0)) * coin;
                }
            }
            return (total / coin) * coin;
        }
        return roundBy(total, coin) * coin;
    }

    /***************************************************************************
     * round change to smallest coin
     *
     * @param total
     *            the monitary amount in tender currency
     * @return the rounded result (1-5 down, 6-9 up, 0 no)
     ***************************************************************************/
    public long change(long total) {
        return change(total, false);
    }

    public long change(long total, boolean negativeTransaction) {
        if (customerFavour) {
            if (GdSarawat.getRoundReturnsCustomerFavour()) {
                return (total - coin + 1) / coin * coin;
            }
            return total > 0 ? (total + coin - 1) / coin * coin : (total - coin + 1) / coin * coin;

        }
        int mod = coin - 1 >> 1;

        if (mod < 1)
            return total;
        return (total < 0 ? total - mod : total + mod) / coin * coin;
    }

    /***************************************************************************
     * foreign currency exchange to home currency
     *
     * @param total
     *            the monitary amount in tender currency
     * @return the rounded result in home currency
     ***************************************************************************/
    public long fc2hc(long total) {
        if (unit < 1)
            return total;
        total *= 10;
        if (club > 0) {
            TndMedia lead = tbl[club];
            if (lead.unit > 0)
                total = total * lead.unit / lead.value;
        }
        return roundBy(total * unit / value, 10);
    }

    /***************************************************************************
     * home currency exchange to foreign currency
     *
     * @param total
     *            the monitary amount in home currency
     * @return the rounded result in tender currency
     ***************************************************************************/
    public long hc2fc(long total) {
        if (unit < 1)
            return total;
        total *= 10;
        if (club > 0) {
            TndMedia lead = tbl[club];
            if (lead.unit > 0)
                total = total * lead.value / lead.unit;
        }
        return roundBy(total * value / unit, 10);
    }

    /***************************************************************************
     * edit foreign currency exchange rate
     *
     * @param full
     *            including descriptive text and currency symbol if true
     * @return new String as defined by REG sc08
     ***************************************************************************/
    public String editXrate(boolean full) {
        String s = editDec(club > 0 ? value : unit, xflg & 0x0f);
        return full ? xtext + symbol + s : s;
    }

    /**
     * tender type (input sequence)
     **/
    public char getType() {
        if (ECommerceManager.getInstance().isTransactionStarted()) return 'A';
        return type;
    }

    public void setType(char type) {
        this.type = type;
    }
}
