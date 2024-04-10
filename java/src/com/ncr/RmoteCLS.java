package com.ncr;

/*******************************************************************
 *
 * Access to customer sales data file (srv\inq\S_HSHCLS.DAT = hash data file)
 *
 *******************************************************************/
public class RmoteCLS extends LinIo {
    /**
     * timestamp
     **/
    int date, time;
    /**
     * total block (transaction counter, item counter, total amount)
     **/
    Total block = new Total();

    /***************************************************************************
     * Constructor
     *
     * @param id
     *            String (3 chars) used as unique identification
     ***************************************************************************/
    public RmoteCLS(String id) {
        super(id, 0, 60);
    }

    /***************************************************************************
     * search for sales data record remotely and parse data fields
     *
     * @param type
     *            lastSale=C00, rewards=CP0, points=CP1-8, variables=CV0-9
     * @param key
     *            customer number
     * @return record size - 2 (0 = not on file)
     ***************************************************************************/
    public int find(String type, String key) {
        int len = net.readHsh('I', type + editTxt(key, 13), this);
        if (len > 0)
            try {
                date = skip(17).scanNum(6);
                time = scan(':').scanNum(6);
                block.trans = (int) scanDec(7);
                block.items = (int) scanDec(8);
                block.total = scanDec(15);
            } catch (NumberFormatException e) {
                error(e, true);
            }
        return len;
    }
}
