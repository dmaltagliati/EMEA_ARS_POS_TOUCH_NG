package com.ncr;

/*******************************************************************
 *
 * Table of sales data records for departments (dpt) (for transaction-based accumulation of sales totals)
 *
 *******************************************************************/
public class TableDpt extends TableSls {
    /**
     * array of discountable sales totals
     **/
    long dsc_sls[] = new long[key.length];
    /**
     * array of counters of discountable sales
     **/
    int dsc_cnt[] = new int[key.length];

    /**
     * array of sales totals with possible points
     **/
    long pnt_sls[] = new long[key.length];

    /***************************************************************************
     * Constructor
     *
     * @param io
     *            provider of access to department data file
     ***************************************************************************/
    public TableDpt(SlsIo io) {
        super(io);
    }

    /***************************************************************************
     * reset the transaction-based sales totals
     ***************************************************************************/
    public void reset() {
        for (int rec = key.length; rec-- > 0; dsc_sls[rec] = pnt_sls[rec] = 0)
            ;
        super.reset();
    }
}
