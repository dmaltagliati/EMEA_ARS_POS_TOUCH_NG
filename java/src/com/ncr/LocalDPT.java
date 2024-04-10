package com.ncr;

/*******************************************************************
 *
 * Access to department data (sales totals per department)
 *
 *******************************************************************/
class LocalDPT extends SlsIo {
    /**
     * flag byte (x00-xFF) defining department properties
     **/
    int flag;
    /**
     * selective itemizer 0-9 used to limit empl/cust discount
     **/
    int sit;
    /**
     * vat code 0-7
     **/
    int vat;
    /**
     * sales category (51-58, 61-68, 71-78, 81-88, 91-98)
     **/
    int cat;
    /**
     * amount entry limitation (halo 00 - 99, lalo 00 - 99)
     **/
    int halo;
    /**
     * secondary flag byte (x00-xFF) defining department properties
     **/
    int flg2;
    /**
     * index of age control info (0 = no control)
     **/
    int ages;
    /**
     * item type
     **/
    int type;
    /**
     * department description (20 chars)
     **/
    String text;
    /**
     * reserve
     **/
    String xtra;

    /***************************************************************************
     * Constructor
     *
     * @param id
     *            String (3 chars) used as unique identification
     * @param blocks
     *            number of total blocks (accumulating data part)
     ***************************************************************************/
    LocalDPT(String id, int blocks) {
        super(id, 58, blocks);
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
                scan(':');
                sit = scanNum(1);
                vat = scanNum(1);
                cat = scanNum(2);
                scan(':');
                halo = scanHex(4);
                scan(':');
                flg2 = scanHex(2);
                ages = scanNum(1);
                type = scanNum(1);
                scan(':');
                text = scan(20);
                xtra = scan(10);
                for (int ind = 0; ind < block.length; block[ind++].scan(this))
                    ;
            } catch (NumberFormatException e) {
                error(e, true);
            }
        return len;
    }
}
