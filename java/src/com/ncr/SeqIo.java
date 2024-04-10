package com.ncr;

/*******************************************************************
 *
 * Access to sequential data files (both local and remote) (assuming all files to be named data\S_???%REG%.DAT) (files
 * are locally read/write and created if non-existent)
 *
 *******************************************************************/
public class SeqIo extends DatIo {
    /***************************************************************************
     * Constructor
     *
     * @param id
     *            String (3 chars) used as unique identification
     * @param fixSize
     *            size of the record header (descriptive part)
     * @param recSize
     *            record size in bytes including separators CR/LF
     ***************************************************************************/
    public SeqIo(String id, int fixSize, int recSize) {
        super(id, fixSize, recSize);
        open("data", "S_" + id + REG + ".DAT", 1);
    }

    /***************************************************************************
     * read record from local or remote file
     *
     * @param rec
     *            relative record number (1 <= rec <= records in file)
     * @param sel
     *            selection (>0=terminal, -1=local)
     * @return record size - 2 (0 = end of file, -1 = LAN offline)
     ***************************************************************************/
    public int read(int rec, int sel) {
        if (sel == LOCAL)
            return super.read(rec);
        return net.readSeq(rec, sel, this);
    }
}
