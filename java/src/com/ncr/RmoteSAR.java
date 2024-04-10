package com.ncr;

/*******************************************************************
 *
 * Access to suspend/resume control file (srv\inq\S_HSHSAR.DAT = command/status/log file)
 *
 *******************************************************************/
class RmoteSAR extends LinIo {
    /**
     * transaction id (terminal number, transaction number)
     **/
    int reg, tran;
    /**
     * last procedure (operator, date, time)
     **/
    int ckr, date, time;
    /**
     * transaction status
     **/
    char stat;

    /***************************************************************************
     * Constructor
     *
     * @param id
     *            String (3 chars) used as unique identification
     ***************************************************************************/
    RmoteSAR(String id) {
        super(id, 0, 36);
    }

    /***************************************************************************
     * edit all control data fields and update record on remote file
     *
     * @param key
     *            String "Sxxxyyyy" (xxx=terminal yyyy=transaction)
     * @return record size - 2 (<1 = access error)
     ***************************************************************************/
    int find(String key) {
        pb = key + ':' + stat + ':' + editKey(reg, 3) + editNum(tran, 4) + ':' + editNum(ckr, 3) + ':'
                + editNum(date, 6) + ':' + editNum(time, 6);

        int len = net.readHsh('R', pb, this);
        if (len > 0)
            try {
                stat = skip(9).scan();
                reg = scan(':').scanKey(3);
                tran = scanNum(4);
                ckr = scan(':').scanNum(3);
                date = scan(':').scanNum(6);
                time = scan(':').scanNum(6);
            } catch (NumberFormatException e) {
                error(e, true);
            }
        return len;
    }
}
