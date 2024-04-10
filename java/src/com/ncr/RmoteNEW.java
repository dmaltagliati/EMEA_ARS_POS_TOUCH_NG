package com.ncr;

/*******************************************************************
 *
 * Access to news/messages data file (srv\data\S_NEW%SRV%.DAT = command/status/log file)
 *
 *******************************************************************/
public class RmoteNEW extends LinIo {
    /**
     * sending terminal, operator, date, time
     **/
    public int reg1, ckr, dat1, tim1;
    /**
     * receiving terminal, date, time
     **/
    public int reg2, dat2, tim2;
    /**
     * message status
     **/
    public int sts;
    /**
     * message text
     **/
    public String text;

    /***************************************************************************
     * Constructor
     *
     * @param id
     *            String (3 chars) used as unique identification
     ***************************************************************************/
    public RmoteNEW(String id) {
        super(id, 0, 78);
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
                tim1 = scanNum(4);
                scan(':');
                reg2 = scanKey(3);
                scan(':');
                dat2 = scanNum(6);
                scan(':');
                tim2 = scanNum(4);
                scan(':');
                sts = scanHex(1);
                scan(':');
                text = scan(40);
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
        push(editNum(tim1, 4));
        push(':');
        push(editKey(reg2, 3));
        push(':');
        push(editNum(dat2, 6));
        push(':');
        push(editNum(tim2, 4));
        push(':');
        push(editHex(sts, 1));
        push(':');
        push(text);
        return net.updNews(rec, this);
    }
}
