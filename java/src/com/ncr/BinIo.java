package com.ncr;

/*******************************************************************
 *
 * Access to local parameter files (binary search) (assuming all files to be sorted in ascending order) (assuming all
 * files to be named S_PLU???.DAT) (files are read-only and may or may not exist)
 *
 *******************************************************************/
public class BinIo extends DatIo {
    /***************************************************************************
     * Constructor
     *
     * @param id
     *            String (3 chars) used as unique identification
     * @param keySize
     *            size of the record key in bytes
     * @param recSize
     *            record size in bytes including separators CR/LF
     ***************************************************************************/
    public BinIo(String id, int keySize, int recSize) {
        super(id, keySize, recSize);
        open(null, "S_PLU" + id + ".DAT", 0);
    }

    /***************************************************************************
     * Constructor
     *
     * @param id
     *            String (3 chars) used as unique identification
     * @param keySize
     *            size of the record key in bytes
     * @param recSize
     *            record size in bytes including separators CR/LF
     * @param open
     * 	 *        if open or not the file
     ***************************************************************************/
    public BinIo(String id, int keySize, int recSize, boolean open) {
        super(id, keySize, recSize);
        if (open) {
            open(null, "S_PLU" + id + ".DAT", 0);
        }
    }

    /***************************************************************************
     * search by n iterative bisections (2 in the nth power = file size in records)
     *
     * @param key
     *            search argument = record key
     * @return record size - 2 (0 = not on file)
     ***************************************************************************/
    public int find(String key) {
        int sts, top = 0, end = getSize();

        while ((recno = (top + end) >> 1) < end) {
            if (super.read(++recno) <= 0)
                return ERROR;
            sts = key.compareTo(pb.substring(0, fixSize));
            if (sts == 0)
                return pb.length();
            if (sts < 0)
                end = recno - 1;
            else
                top = recno;
        }
        return 0;
    }

    /***************************************************************************
     * search for first occurrence of non unique key
     *
     * @param key
     *            search argument = record key
     * @return record size - 2 (0 = not on file)
     ***************************************************************************/
    public int start(String key) {
        int sts = find(key);

        if (sts <= 0)
            return sts;
        do {
            if (recno == 1)
                return sts;
            if (super.read(--recno) < sts)
                return ERROR;
        } while (pb.startsWith(key));
        return super.read(++recno);
    }

    /***************************************************************************
     * search for next occurrence of non-unique key
     *
     * @param key
     *            search argument = record key
     * @return record size - 2 (0 = not on file)
     ***************************************************************************/
    public int next(String key) {
        int sts = super.read(++recno);

        if (sts <= 0)
            return sts;
        return pb.startsWith(key) ? sts : 0;
    }
}
