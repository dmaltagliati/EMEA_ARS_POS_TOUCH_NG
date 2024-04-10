package com.ncr;

/*******************************************************************
 *
 * Single sales total structure (storage for item count and sales amount)
 *
 *******************************************************************/
public class Sales {
    /**
     * item count
     **/
    int items;

    /**
     * sales amount
     **/
    public long total;

    /***************************************************************************
     * set sales total
     *
     * @param items
     *            item count
     * @param total
     *            sales amount
     ***************************************************************************/
    void set(int items, long total) {
        this.items = items;
        this.total = total;
    }

    /***************************************************************************
     * add to sales total
     *
     * @param items
     *            item count
     * @param total
     *            sales amount
     ***************************************************************************/
    void add(int items, long total) {
        this.items += items;
        this.total += total;
    }

    /***************************************************************************
     * zero-check sales total
     *
     * @return true if sales total all zero
     ***************************************************************************/
    public boolean isZero() {
        return items == 0 && total == 0;
    }

    /***************************************************************************
     * write sales total to data file
     *
     * @param rec
     *            relative record number (1 <= rec <= records in file)
     * @param blk
     *            number of the total block within sales data record
     * @param io
     *            provider of access to sales data file
     ***************************************************************************/
    void write(int rec, int blk, SlsIo io) {
        if (io.readSls(rec, blk) > 0) {
            io.block[blk].update(items, total);
            io.writeSls(rec, blk);
            Delta.write(io, blk, 1, items, total);
        }
    }
}
