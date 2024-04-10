package com.ncr;

import java.util.Calendar;
import java.util.Date;

/*******************************************************************
 *
 * Access to cashier control file (list of cashiers and supervisors)
 *
 *******************************************************************/
public class LocalCTL extends SlsIo {
    /**
     * name (20 chars)
     **/
    public String text, xtra;
    /**
     * employee number (8 digits)
     **/
    public int pers;
    /**
     * age of operator (yymmdd)
     **/
    public int age;
    /**
     * functional specs (flom, roll, location)
     **/
    public int flag, lvl, pin;
    /**
     * lock indicator (1=locked, 2=forced close)
     **/
    public int lck;
    /**
     * terminal number (last open)
     **/
    public int reg;
    /**
     * drawer number
     **/
    public int drw;
    /**
     * secret number
     **/
    public int sec;
    /**
     * status (00=inactive, 01=opened, 02=closed, 19=settled)
     **/
    int sts;
    /**
     * last password change yymmdd, hhmm
     **/
    int datePwd, timePwd;
    /**
     * last settlement yymmdd, hhmm
     **/
    int dateBal, timeBal;
    /**
     * last sign in yymmdd, hhmm
     **/
    int dateOpn, timeOpn;
    /**
     * last close yymmdd, hhmm
     **/
    int dateCls, timeCls;

    /***************************************************************************
     * Constructor
     *
     * @param id
     *            String (3 chars) used as unique identification
     ***************************************************************************/
    public LocalCTL(String id) {
        super(id, 128, 0);
    }

    /***************************************************************************
     * read data record from local or remote file and parse all operator control data
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
                key = scanNum(4);
                scan(':');
                pers = scanNum(8);
                scan(':');
                flag = scanHex(2);
                scan(':');
                text = scan(20);
                xtra = scan(10);
                scan(':');
                age = scanNum(6);
                scan(':');
                lvl = scanNum(3);
                scan(':');
                pin = scanKey(3);
                scan(':');
                lck = scanNum(1);
                scan(':');
                reg = scanKey(3);
                scan(':');
                drw = scanNum(3);
                scan(':');
                sec = scanNum(4);
                scan(':');
                sts = scanNum(2);
                scan(':');
                datePwd = scanNum(6);
                scan(':');
                timePwd = scanNum(4);
                scan(':');
                dateBal = scanNum(6);
                scan(':');
                timeBal = scanNum(4);
                scan(':');
                dateOpn = scanNum(6);
                scan(':');
                timeOpn = scanNum(4);
                scan(':');
                dateCls = scanNum(6);
                scan(':');
                timeCls = scanNum(4);
            } catch (NumberFormatException e) {
                error(e, true);
            }
        return len;
    }

    /***************************************************************************
     * edit data fields and overwrite complete record in file
     *
     * @param rec
     *            relative record number (1 <= rec <= records in file)
     ***************************************************************************/
    public void rewrite(int rec) {
        index = 0;
        push(editNum(key, 4));
        push(':');
        push(editNum(pers, 8));
        push(':');
        push(editHex(flag, 2));
        push(':');
        push(text);
        push(xtra);
        push(':');
        push(editNum(age, 6));
        push(':');
        push(editNum(lvl, 3));
        push(':');
        push(editKey(pin, 3));
        push(':');
        push(editNum(lck, 1));
        push(':');
        push(editKey(reg, 3));
        push(':');
        push(editNum(drw, 3));
        push(':');
        push(editNum(sec, 4));
        push(':');
        push(editNum(sts, 2));
        push(':');
        push(editNum(datePwd, 6));
        push(':');
        push(editNum(timePwd, 4));
        push(':');
        push(editNum(dateBal, 6));
        push(':');
        push(editNum(timeBal, 4));
        push(':');
        push(editNum(dateOpn, 6));
        push(':');
        push(editNum(timeOpn, 4));
        push(':');
        push(editNum(dateCls, 6));
        push(':');
        push(editNum(timeCls, 4));
        super.rewrite(rec, 0);
    }

    /***************************************************************************
     * check duration of password
     *
     * @param days
     *            lifetime of secret number
     * @return true = expired
     ***************************************************************************/
    public boolean pwdOlder(int days) {
        Calendar c = Calendar.getInstance();
        Date date = c.getTime();

        if (days < 1)
            return false;
        int yyyy = c.get(c.YEAR) / 100 * 100 + datePwd / 10000;
        if (yyyy > c.get(c.YEAR))
            yyyy -= 100;
        c.set(yyyy, datePwd / 100 % 100 - 1, datePwd % 100, timePwd / 100, timePwd % 100, 0);
        c.add(c.DATE, days);
        return c.getTime().before(date);
    }

    /***************************************************************************
     * synchronize spare checkers with remote file
     ***************************************************************************/
    public void update() {
        while (read(++recno, LOCAL) > 0) {
            if (key < 1000)
                continue;
            if (read(recno, 0) < 1 || key > 999)
                break;
            reg = sts = 0;
            dateOpn = timeOpn = dateCls = timeCls = 0;
            rewrite(recno);
            logConsole(2, "update " + id + " key=" + editNum(key, 3), null);
        }
        --recno;
    }

    /***************************************************************************
     * search for checker locally
     *
     * @param ckr
     *            checker number (cashier 001-799, supervisors 801-999)
     * @return relative record number (0 = not on file)
     ***************************************************************************/
    public int find(int ckr) {
        for (int rec = 0; read(++rec, LOCAL) > 0; ) {
            if (key == ckr)
                return rec;
        }
        return 0;
    }
}
