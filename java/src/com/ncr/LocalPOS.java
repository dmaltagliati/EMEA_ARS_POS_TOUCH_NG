package com.ncr;

import java.io.IOException;

/*******************************************************************
 *
 * Access to local non resettable totals (assuming a CMOS-type block device/path in system property CMOS)
 *
 *******************************************************************/
public class LocalPOS extends DatIo {
    /**
     * transaction/customer counter
     **/
    int trans;
    /**
     * total amount
     **/
    long total;

    /***************************************************************************
     * Constructor
     *
     * @param id
     *            String (3 chars) used as unique identification
     ***************************************************************************/
    public LocalPOS(String id) {
        super(id, 0, 18);
        open(System.getProperty("CMOS"), "GdCmos." + id, 1);
        if (getSize() < 2)
            error(new IOException(pathfile.getPath() + " missing"), true);
    }

    /***************************************************************************
     * read data record and scan data fields (transaction count and total amount)
     *
     * @param rec
     *            relative record number (1 <= rec <= records in file)
     * @return record size - 2 (0 = end of file)
     ***************************************************************************/
    public int read(int rec) {
        int len = super.read(rec);

        if (len > 0)
            try {
                trans = (int) scanDec(5);
                total = scanDec(11);
            } catch (NumberFormatException e) {
                error(e, true);
            }
        return len;
    }

    /***************************************************************************
     * edit data fields and overwrite complete record in file
     *
     * @param rec
     *            relative record number (1 <= rec <= records in file)
     ***************************************************************************/
    public void rewrite(int rec) {
        index = 0;
        pushDec(trans, 5);
        pushDec(total, 11);
        super.rewrite(rec, 0);
        sync();
    }
}
