package com.ncr;

import java.io.IOException;

/*******************************************************************
 *
 * Access to hash files (random search) (assuming all files to have unique record keys) (assuming all files to be named
 * S_HSH???.DAT) (files are locally read/write and created if non-existent)
 *
 *******************************************************************/
public class HshIo extends DatIo {
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
    public HshIo(String id, int keySize, int recSize) {
        super(id, keySize, recSize);
        open("inq", "S_HSH" + id + ".DAT", 1);
    }

    /***************************************************************************
     * search sequentially starting at record as computed by hash formula
     *
     * @param key
     *            search argument = record key
     * @return record size - 2 (0 = not on file, -1 = file full)
     ***************************************************************************/
    public int find(String key) {
        int top = 0, del = 0, end = getSize();
        if (end < 8)
            return ERROR;
        int ind = fixSize + 5 & ~7, val = key.charAt(1) & 0x0f;
        String s = editTxt(key.substring(2), ind);
        while (val-- > 0)
            s = s.substring(1) + s.charAt(0);
        for (char tmp[] = new char[8]; ind-- > 0; ) {
            val = s.charAt(ind) & 0x0f;
            if (val > 9)
                val -= 6;
            tmp[ind & 7] = (char) (val + '0');
            if ((ind & 7) > 0)
                continue;
            top += Integer.parseInt(new String(tmp));
        }
        top %= end >> 3;
        recno = top <<= 3;
        while (super.read(++recno) > 0) {
            char c = pb.charAt(fixSize - 1);
            if (c < '0') {
                if (del == 0)
                    del = recno;
                if (c == ' ')
                    break;
            } else if (pb.startsWith(key))
                return pb.length();
            if (recno == end)
                recno = 0;
            if (recno == top)
                break;
        }
        if ((recno = del) > 0)
            return 0;
        error(new IOException("no space"), false);
        return ERROR;
    }

    /***************************************************************************
     * search locally first, then remotely, or viceversa
     *
     * @param key
     *            search argument = record key
     * @return record size - 2 (0 = not on file, -1 = LAN offline)
     ***************************************************************************/
    public int find(String key, boolean remoteFirst) {
        int sts = remoteFirst ? 0 : find(key);

        if (sts < 1)
            sts = net.readHsh('R', key, this);
        if (remoteFirst && sts < 1)
            if (find(key) > 0)
                return pb.length();
        return sts;
    }

    /***************************************************************************
     * erase record with all '-' characters
     *
     * @param rec
     *            relative record number (1 <= rec <= records in file)
     ***************************************************************************/
    public void delete(int rec) {
        for (index = 0; index < dataLen(); push('-'))
            ;
        rewrite(rec, 0);
    }
}
