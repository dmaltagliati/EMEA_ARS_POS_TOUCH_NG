package com.ncr.common.utilities;

import com.ncr.Basis;
import com.ncr.Mnemo;
import org.apache.log4j.Logger;

import java.io.FileInputStream;

import static com.ncr.FmtIo.editNum;
import static com.ncr.Motor.input;
import static com.ncr.Struc.ctl;
import static com.ncr.Struc.tra;

public class ReferenceManager {
    private static final Logger logger = Logger.getLogger(ReferenceManager.class);

    private static ReferenceManager instance = null;

    public static ReferenceManager getInstance() {
        if (instance == null) {
            instance = new ReferenceManager();
        }
        return instance;
    }

    private ReferenceManager() {
    }

    public int insertReferenceNumber() {
        if (!tra.getReferenceNumber().isEmpty()) return -1;
        if (!Basis.acceptNbr(Mnemo.getMenu(128), 102, 11, 18, 11, 0))
            return 5;
        if (input.pb.length() == 11) {
            tra.setReferenceNumber(input.pb);
        } else if (input.pb.length() == 12 || input.pb.length() == 13) {
            return buildReferenceInfoFromArs(input.pb);
        } else if (input.pb.length() == 18) {
            return buildReferenceInfoFromPsh(input.pb);
        }
        return -1;
    }

    public int buildReferenceInfoFromPsh(String barcode) {
        logger.debug("Enter. Barcode: " + barcode);
        if (!barcode.startsWith("20")) return 5;

        String date = barcode.substring(0, 8);
        if (!date.matches("^((2[0-9])[0-9]{2})(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])$"))
            return 5;

        String store = "0" + barcode.substring(8, 11);
        String terminal = barcode.substring(11, 14);
        String transaction = barcode.substring(14, 18);
        logger.debug("Reference Number: " + store + terminal + transaction);
        tra.setReferenceNumber(store + terminal + transaction);
        logger.debug("Reference Date: " + date);
        tra.setReferenceDate(date);
        return 0;
    }

    public int buildReferenceInfoFromArs(String barcode) {
        logger.debug("Enter. Barcode: " + barcode);
        if (!barcode.startsWith("1")) return 5;

        String date = "20" + editNum(ctl.date, 6).substring(0, 2) + barcode.substring(1, 5);
        if (!date.matches("^((2[0-9])[0-9]{2})(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])$"))
            return 5;

        String store = editNum(ctl.sto_nbr, 4);
        String terminal = barcode.substring(5, 8);
        String transaction = barcode.substring(8, 12);
        logger.debug("Reference Number: " + store + terminal + transaction);
        tra.setReferenceNumber(store + terminal + transaction);
        logger.debug("Reference Date: " + date);
        tra.setReferenceDate(date);
        return 0;
    }

    public int insertReferenceDate() {
        if (!tra.getReferenceDate().isEmpty()) return 0;
        if (!Basis.acceptNbr(Mnemo.getMenu(129), 37, 8, 8, 8, 0))
            return 5;
        if (!input.pb.matches("^((2[0-9])[0-9]{2})(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])$"))
            return 5;
        tra.setReferenceDate(input.pb);
        return 0;
    }
}
