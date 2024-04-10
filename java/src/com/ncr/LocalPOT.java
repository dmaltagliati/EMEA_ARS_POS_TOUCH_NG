package com.ncr;

/*******************************************************************
 *
 * Access to cashier data (sales totals for up to 16 cashiers)
 *
 *******************************************************************/
public class LocalPOT extends SlsIo {
    /**
     * index into table of cashiers active at this terminal
     **/
    int blk;

    /***************************************************************************
     * Constructor
     *
     * @param id
     *            String (3 chars) used as unique identification
     * @param blocks
     *            number of total blocks (accumulating data part)
     * @param tb
     *            single total block for all read/writeSls operations
     ***************************************************************************/
    public LocalPOT(String id, int blocks, Total tb) {
        super(id, 4, blocks);
        while (blocks > 0)
            block[--blocks] = tb;
    }

    /***************************************************************************
     * append new record with key and 10 equal total blocks to file, where key (inherited from SlsIo) has been set to
     * ic/sc
     ***************************************************************************/
    public void write() {
        onto(0, editNum(key, 4));
        for (int ind = 0; ind < block.length; block[ind++].edit(this))
            ;
        super.write();
    }
}
