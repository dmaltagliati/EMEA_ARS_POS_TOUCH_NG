package com.ncr;

/*******************************************************************
 *
 * Access to Invoice Printing Order file (at server only)
 *
 *******************************************************************/
public class RmoteBIL extends LinIo {
    /**
     * original terminal
     **/
    int reg;
    /**
     * transaction
     **/
    int tran;

    /***************************************************************************
     * Constructor
     *
     * @param id
     *            String (3 chars) used as unique identification
     ***************************************************************************/
    public RmoteBIL(String id) {
        super(id, 0, 56);
    }

    /***************************************************************************
     * write new order record
     *
     * @param type
     *            currency (0=home, >0=foreign currency tender)
     * @return <0=offline, 0=unavailable, >0=ok
     ***************************************************************************/
    public int write(int type) {
        index = 0;
        push(editKey(0, 3));
        push(':');
        push(editKey(reg, 3));
        push(':');
        push(editNum(0, 6));
        push(':');
        push(editNum(0, 6));
        push(':');
        push(editNum(tran, 4));
        push(':');
        push(editNum(0, 8));
        push(':');
        push(editKey(0, 3));
        push(':');
        push(editNum(0, 6));
        push(':');
        push(editNum(0, 6));
        push(':');
        push(editNum(type, 2));
        return net.updNews(0, this);
    }
}
