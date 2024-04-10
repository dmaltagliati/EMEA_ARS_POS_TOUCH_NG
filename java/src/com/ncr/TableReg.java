package com.ncr;

/*******************************************************************
 *
 * Table of sales data records for financial reports (ckr / reg) (for transaction-based accumulation of sales totals)
 *
 *******************************************************************/
public class TableReg extends TableSls {
    /***************************************************************************
     * Constructor
     *
     * @param io
     *            provider of access to sales data file
     ***************************************************************************/
    public TableReg(SlsIo io) {
        super(io);
    }

    /***************************************************************************
     * search for the access key given as ic / sc
     *
     * @param ic
     *            the itemcode
     * @param sc
     *            the subcode
     * @return the relative record number (0 = not found)
     ***************************************************************************/
    public int find(int ic, int sc) {
        return super.find(ic * 100 + sc);
    }

    /***************************************************************************
     * search for the access key given as tender / sc
     *
     * @param ind
     *            the tender number (0 = all tender total)
     * @param sc
     *            the subcode
     * @return the relative record number (0 = not found)
     ***************************************************************************/
    public int findTnd(int ind, int sc) {
        return find(10 + ind, sc);
    }
}
