package com.ncr;

/*******************************************************************
 *
 * Table of sales data records in memory (act / reg / dpt / slm) (for transaction-based accumulation of sales totals)
 *
 *******************************************************************/
public class TableSls {
    /**
     * provider of access to sales data file
     **/
    SlsIo io;

    /**
     * number of total blocks within sales data record
     **/
    int blocks;

    /**
     * array of access keys (search argument)
     **/
    int key[];

    /**
     * array of relative record numbers (reference to major)
     **/
    int grp[];

    /**
     * array of sets of sales totals
     **/
    public Sales[][] sales;


    /***************************************************************************
     * Constructor
     *
     * @param io
     *            provider of access to sales data file
     ***************************************************************************/
    public TableSls(SlsIo io) {
        int ind, rec = (this.io = io).getSize();
        key = new int[rec];
        grp = new int[rec];
        sales = new Sales[rec][blocks = io.block.length];
        while (rec-- > 0) {
            for (ind = blocks; ind-- > 0; sales[rec][ind] = new Sales())
                ;
        }
    }

    /***************************************************************************
     * initialize complete table reading the sales data file and prepare subsequent search functions
     ***************************************************************************/
    public void init() {
        int rec = 0;
        while (io.read(rec + 1, io.LOCAL) > 0) {
            key[rec] = io.key;
            grp[rec++] = io.grp;
        }
        while (rec-- > 0) {
            if (grp[rec] > 0)
                grp[rec] = find(grp[rec]);
        }
    }

    /***************************************************************************
     * search for the given access key
     *
     * @param code
     *            the access key
     * @return the relative record number (0 = not found)
     ***************************************************************************/
    public int find(int code) {
        for (int rec = 0; rec < key.length; ) {
            if (key[rec++] == code)
                return rec;
        }
        return 0;
    }

    /***************************************************************************
     * reset the transaction-based sales totals
     ***************************************************************************/
    public void reset() {
        int ind, rec = key.length;
        while (rec-- > 0) {
            for (ind = blocks; ind-- > 0; sales[rec][ind].set(0, 0))
                ;
        }
    }

    /***************************************************************************
     * accumulate one sales total throughout all major levels
     *
     * @param rec
     *            relative record number (1 <= rec <= records in file)
     * @param blk
     *            number of the total block within sales data record
     * @param items
     *            item count
     * @param total
     *            sales amount
     ***************************************************************************/
    public void addSales(int rec, int blk, int items, long total) {
        for (; rec-- > 0; rec = grp[rec]) {
            sales[rec][blk].add(items, total);
        }
    }

    /***************************************************************************
     * get the sum of all total blocks within one sales data record
     *
     * @param rec
     *            relative record number (1 <= rec <= records in file)
     * @return new sales total object
     ***************************************************************************/
    public Sales netSales(int rec) {
        Sales ptr[] = sales[rec - 1], sls = new Sales();
        for (int ind = blocks; ind-- > 0; sls.total += ptr[ind].total) {
            if (ind == 0)
                sls.items += ptr[ind].items;
        }
        return sls;
    }

    /***************************************************************************
     * update all active sales totals of one sales data record to the data file
     *
     * @param rec
     *            relative record number (1 <= rec <= records in file)
     ***************************************************************************/
    public void write(int rec) {
        for (int ind = 0; ind < blocks; ind++) {
            Sales sls = sales[rec - 1][ind];
            if (sls.isZero())
                continue;
            sls.write(rec, ind, io);
        }
    }
}
