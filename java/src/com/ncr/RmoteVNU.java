package com.ncr;

/*******************************************************************
 *
 * Access to VariousNumberLookup file (at server only)
 *
 *******************************************************************/
public class RmoteVNU extends LinIo {
    /***************************************************************************
     * Constructor
     *
     * @param id
     *            String (3 chars) used as unique identification
     ***************************************************************************/
    public RmoteVNU(String id) {
        super(id, 0, 26);
    }

    /***************************************************************************
     * read record and increment its counter
     *
     * @param key
     *            file access key
     * @return <0=offline, 0=not found, >0=unique counter
     ***************************************************************************/
    public int find(String key) {
        init(' ').push(id.charAt(0)).upto(16, key);
        int sts = net.readHsh('I', toString(), this);
        if (sts > 0)
            try {
                sts = skip(16).scanNum(10);
            } catch (NumberFormatException e) {
                error(e, true);
            }
        return sts;
    }
}
