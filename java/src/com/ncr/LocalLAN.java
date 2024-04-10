package com.ncr;

/*******************************************************************
 *
 * Access to terminal control file (list of all terminals in cluster) Local file consists of one record only (this
 * terminal)
 *
 *******************************************************************/
public class LocalLAN extends SlsIo {
    /**
     * terminal type ('R'=register (PoS), 'C'=crt (B/O), 'D'=daemon, 'S'=server, ' '=spare)
     **/
    char type;
    /**
     * terminal characteristics (service mask, exit no)
     **/
    int flag, exit;
    /**
     * id mnemonics (terminal, reserve, invoice printer)
     **/
    String text, xtra, prin;
    /**
     * last transaction (operator, actioncode)
     **/
    int ckr, sts;
    /**
     * record counters of sequential files
     **/
    int idc, jrn, mnt, dtl, gpo, imp;
    /**
     * time stamp yymmdd hhmmss
     **/
    int date, time;
    /**
     * last reset yymmdd hhmmss
     **/
    int zdat, ztim;
    /**
     * org version (parameters) and dat version (reset counter)
     **/
    int org, dat;
    /**
     * LAN status x00-xFF
     **/
    int lan;
    /**
     * array of cashier numbers (pot allocation)
     **/
    int tbl[];

    /***************************************************************************
     * Constructor
     *
     * @param id
     *            String (3 chars) used as unique identification
     * @param blocks
     *            max number of cashiers active at this terminal
     ***************************************************************************/
    public LocalLAN(String id, int blocks) {
        super(id, 160 + blocks * 3, 0);
        tbl = new int[blocks];
    }

    /***************************************************************************
     * read data record from local or remote file and parse all terminal control data
     *
     * @param rec
     *            relative record number (1 <= rec <= records in file)
     * @param sel
     *            selection (>0=inquiry (rec=0), 0=remote, -1=local)
     * @return record size - 2 (0 = end of file)
     ***************************************************************************/
    public int read(int rec, int sel) {
        int len = super.read(rec, sel);

        if (len > 0)
            try {
                type = scan();
                scan(':');
                key = scanKey(3);
                scan(':');
                grp = scanKey(2);
                scan(':');
                flag = scanHex(2);
                scan(':');
                text = scan(20);
                xtra = scan(10);
                scan(':');
                exit = scanNum(3);
                scan(':');
                prin = scan(10);
                scan(':');
                ckr = scanNum(3);
                scan(':');
                sts = scanNum(2);
                scan(':');
                idc = scanNum(8);
                scan(':');
                jrn = scanNum(8);
                scan(':');
                mnt = scanNum(8);
                scan(':');
                dtl = scanNum(8);
                scan(':');
                gpo = scanNum(8);
                scan(':');
                imp = scanNum(8);
                scan(':');
                date = scanNum(6);
                scan(':');
                time = scanNum(6);
                scan(':');
                zdat = scanNum(6);
                scan(':');
                ztim = scanNum(6);
                scan(':');
                org = scanNum(4);
                scan(':');
                dat = scanNum(4);
                scan(':');
                lan = scanHex(2);
                scan(':');
                for (int ind = 0; ind < tbl.length; tbl[ind++] = scanNum(3))
                    ;
            } catch (NumberFormatException e) {
                error(e, true);
            }
        return len;
    }

    /***************************************************************************
     * edit descriptive fields and append complete record to file
     ***************************************************************************/
    public void write() {
        init(' ');
        push("R:" + REG + ":" + GRP + ":00:");
        push(editTxt("xxxxxxxxxx", 30) + ":000:          :");
        skip(dataLen() - index);
        super.write();
    }

    /***************************************************************************
     * edit data fields and overwrite status data in file
     *
     * @param rec
     *            relative record number (1 <= rec <= records in file)
     ***************************************************************************/
    public void rewrite(int rec) {
        index = 0;
        push(editNum(ckr, 3));
        push(':');
        push(editNum(sts, 2));
        push(':');
        push(editNum(idc, 8));
        push(':');
        push(editNum(jrn, 8));
        push(':');
        push(editNum(mnt, 8));
        push(':');
        push(editNum(dtl, 8));
        push(':');
        push(editNum(gpo, 8));
        push(':');
        push(editNum(imp, 8));
        push(':');
        push(editNum(date, 6));
        push(':');
        push(editNum(time, 6));
        push(':');
        push(editNum(zdat, 6));
        push(':');
        push(editNum(ztim, 6));
        push(':');
        push(editNum(org, 4));
        push(':');
        push(editNum(dat, 4));
        push(':');
        push(editHex(lan, 2));
        push(':');
        for (int ind = 0; ind < tbl.length; push(editNum(tbl[ind++], 3)))
            ;
        super.rewrite(rec, 58);
    }
}
