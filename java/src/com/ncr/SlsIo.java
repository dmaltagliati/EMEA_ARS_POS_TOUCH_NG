package com.ncr;

/*******************************************************************
 *
 * Access to local data files with sales totals (assuming a directory named data under the current directory) (assuming
 * all files to exist with the name S_???%REG%.DAT) (assuming remote mirror image data available at the server)
 *
 *******************************************************************/
class SlsIo extends DatIo {
    /**
     * 4-digit record access key (numeric or asterisk)
     **/
    int key;
    /**
     * 4-digit major layer access key (numeric or asterisk)
     **/
    int grp;
    /**
     * array of sales total blocks (transaction count, item count, amount)
     **/
    Total block[];

    /***************************************************************************
     * Constructor
     *
     * @param id
     *            String (3 chars) used as unique identification
     * @param fixSize
     *            size of the record header (descriptive part)
     * @param blocks
     *            number of total blocks (accumulating data part)
     ***************************************************************************/
    SlsIo(String id, int fixSize, int blocks) {
        super(id, fixSize, fixSize + Total.length * blocks + 2);
        block = new Total[blocks];
        while (blocks > 0)
            block[--blocks] = new Total();
        open("data", "S_" + id + REG + ".DAT", 1);
    }

    /***************************************************************************
     * read data record from local or remote file and prepare subsequent parse functions
     *
     * @param rec
     *            relative record number (1 <= rec <= records in file)
     * @param sel
     *            selection (>0=terminal/group, 0=all terminals, -1=local)
     * @return record size - 2 (0 = end of file)
     ***************************************************************************/
    int read(int rec, int sel) {
        if (sel == LOCAL)
            return super.read(rec);
        return net.readSls(rec, sel, this);
    }

    /***************************************************************************
     * read data record from local file, parse record key and selected sales total block
     *
     * @param rec
     *            relative record number (1 <= rec <= records in file)
     * @param blk
     *            block number
     * @return record size - 2 (0 = end of file)
     ***************************************************************************/
    int readSls(int rec, int blk) {
        int len = super.read(rec);
        if (len > 0)
            try {
                key = scanKey(4);
                index = fixSize + blk * Total.length;
                block[blk].scan(this);
            } catch (NumberFormatException e) {
                error(e, true);
            }
        return len;
    }

    /***************************************************************************
     * edit selected sales total block, overwrite the total block in local data file, send the total block to remote
     * mirror image
     *
     * @param rec
     *            relative record number (1 <= rec <= records in file)
     * @param blk
     *            block number
     ***************************************************************************/
    void writeSls(int rec, int blk) {
        index = 0;
        block[blk].edit(this);
        rewrite(rec, fixSize + blk * Total.length);
        net.writeSls(rec, blk, this);
    }
}
