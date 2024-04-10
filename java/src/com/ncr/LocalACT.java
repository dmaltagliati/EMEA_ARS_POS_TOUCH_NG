package com.ncr;

/*******************************************************************
 *
 * Access to hourly activity data (sales totals per time slice)
 *
 *******************************************************************/
class LocalACT extends SlsIo {
    /**
     * flag byte (x00-xFF) not used by core
     **/
    int flag;

    /***************************************************************************
     * Constructor
     *
     * @param id
     *            String (3 chars) used as unique identification
     * @param blocks
     *            number of total blocks (accumulating data part)
     ***************************************************************************/
    LocalACT(String id, int blocks) {
        super(id, 12, blocks);
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
    int read(int rec, int sel) {
        int len = super.read(rec, sel);

        if (len > 0)
            try {
                key = scanKey(4);
                scan(':');
                grp = scanKey(4);
                scan(':');
                flag = scanHex(2);
                for (int ind = 0; ind < block.length; block[ind++].scan(this))
                    ;
            } catch (NumberFormatException e) {
                error(e, true);
            }
        return len;
    }
}
