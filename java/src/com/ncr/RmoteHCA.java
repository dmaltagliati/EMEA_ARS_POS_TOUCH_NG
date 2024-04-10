package com.ncr;

/*******************************************************************
 *
 * Access to High-order CustomerTransaction Accounts (srv\data\S_HCA%SRV%.DAT = command/status/log file)
 *
 *******************************************************************/
public class RmoteHCA extends LinIo {
    /**
     * requesting terminal, operator, date, time
     **/
    int reg1, ckr, dat1, tim1;
    /**
     * responding terminal, date, time
     **/
    int reg2, dat2, tim2;
    /**
     * processing status (0=active, 1=ready, 2=cancelled)
     **/
    int sts;
    /**
     * message key = customer number (13 digits)
     **/
    String key;

    /***************************************************************************
     * Constructor
     *
     * @param id
     *            String (3 chars) used as unique identification
     ***************************************************************************/
    public RmoteHCA(String id) {
        super(id, 0, 56);
    }

    /***************************************************************************
     * read data record from remote file and parse all message data
     *
     * @param rec
     *            relative record number (1 <= rec <= records in file)
     * @return record size - 2 (0 = end of file)
     ***************************************************************************/
    public int read(int rec) {
        int len = net.readSeq(rec, 0, this);

        if (len > 0)
            try {
                reg1 = scanKey(3);
                scan(':');
                ckr = scanNum(3);
                scan(':');
                dat1 = scanNum(6);
                scan(':');
                tim1 = scanNum(6);
                scan(':');
                reg2 = scanKey(3);
                scan(':');
                dat2 = scanNum(6);
                scan(':');
                tim2 = scanNum(6);
                scan(':');
                sts = scanHex(2);
                scan(':');
                key = scan(13);
            } catch (NumberFormatException e) {
                error(e, true);
            }
        return len;
    }

    /***************************************************************************
     * edit all message data fields and update record on remote file
     *
     * @param rec
     *            relative record number (1 <= rec <= records in file)
     * @return record size - 2 (<1 = access error)
     ***************************************************************************/
    public int write(int rec) {
        index = 0;
        push(editKey(reg1, 3));
        push(':');
        push(editNum(ckr, 3));
        push(':');
        push(editNum(dat1, 6));
        push(':');
        push(editNum(tim1, 6));
        push(':');
        push(editKey(reg2, 3));
        push(':');
        push(editNum(dat2, 6));
        push(':');
        push(editNum(tim2, 6));
        push(':');
        push(editHex(sts, 2));
        push(':');
        push(editTxt(key, 13));
        return net.updNews(rec, this);
    }
}
