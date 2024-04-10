package com.ncr;

/*******************************************************************
 *
 * Access to register financial data (sales totals per terminal)
 *
 *******************************************************************/
public class LocalREG extends SlsIo {
    /**
     * flag byte (x00-xFF) defining properties of report total
     **/
    int flag;
    /**
     * percentage with one assumed decimal place
     **/
    int rate;
    /**
     * transaction flag (x00-xFF) defining operational properties
     **/
    int tflg;
    /**
     * description of financial total (12 chars)
     **/
    public String text;

    /***************************************************************************
     * Constructor
     *
     * @param id
     *            String (3 chars) used as unique identification
     * @param blocks
     *            number of total blocks (accumulating data part)
     ***************************************************************************/
    public LocalREG(String id, int blocks) {
        super(id, 28, blocks);
    }

    /***************************************************************************
     * read data record from local or remote file and parse descriptive part along with sales data part(s)
     *
     * @param rec
     *            relative record number (1 <= rec <= records in file)
     * @param sel
     *            selection (>0=terminal/group, 0=all terminals, -1=local)
     * @return record size - 2 (0 = end of file)
     ***************************************************************************/
    public int read(int rec, int sel) {
        int len = super.read(rec, sel);

        if (len > 0)
            try {
                key = scanNum(4);
                scan(':');
                text = scan(12);
                scan(':');
                flag = scanHex(2);
                scan(':');
                rate = scanNum(4);
                scan(':');
                tflg = scanHex(2);
                for (int ind = 0; ind < block.length; block[ind++].scan(this))
                    ;
            } catch (NumberFormatException e) {
                error(e, true);
            }
        return len;
    }
}
