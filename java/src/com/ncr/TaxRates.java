package com.ncr;

/*******************************************************************
 * vat/tax table entry
 *******************************************************************/
public class TaxRates extends FmtIo {
    /**
     * flat (environment) tax if > 0
     **/
    public int flat;
    /**
     * default constant rate (1 dec assumed)
     **/
    public int rate; /* default constant rate */
    /**
     * applied discounts on totals (SD, SI, ST)
     **/
    public long tld_amt;
    /**
     * tax description
     **/
    public String text;

    /**
     * reset all accumulators for new transaction
     **/
    public void reset() {
        tld_amt = 0;
    }

    /***************************************************************************
     * calculate tax amount
     *
     * @param total
     *            taxable sales amount
     * @return rounded tax amount
     ***************************************************************************/
    public long collect(long total) {
        return roundBy(total * rate, 1000);
    }

    /***************************************************************************
     * calculate tax amount included
     *
     * @param total
     *            gros sales amount
     * @return rounded tax amount
     ***************************************************************************/
    public long exempt(long total) {
        return roundBy(total * rate * 10 / (1000 + rate), 10);
    }
}
