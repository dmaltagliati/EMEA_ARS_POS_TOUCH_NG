package com.ncr;

import com.ncr.gpe.PosGPE;
import org.apache.log4j.Logger;

import java.util.Vector;

public class WinEPTSVoucherManager extends DevIo {
    private static final Logger logger = Logger.getLogger(WinEPTSVoucherManager.class);
    private static Vector creditCardVoucher = new Vector();
    private static Vector voucherCopyNumber = new Vector();
    private static boolean voucherFiscalReceipt = false;
    private static final int PRINTNORMAL = 1;
    private static final int PRINTCOMMENTAFTERLOGO = 16;
    private static final int BEGINNORMAL = 0;
    private static final int ENDNORMAL = 2;
    private static final int PRINTFIXEDOUTPUT = 32;
    private static final int PRINTTRAILERLINE = 33;

    public static void createVirtualVoucher(int ind, String nbr, String amt, String dig[]) {
        logger.info("ENTER createVirtualVoucher");
        logger.info("ind: " + ind);
        logger.info("amt: " + amt);
        logger.info("nbr: " + nbr);

        LinIo slpLine = new LinIo("SLP", 1, prin.recColumns == 44 ? 57 : 54);
        CreditCardVoucher LineToAdd = new CreditCardVoucher();

        LineToAdd.setTypeOfLine('B');
        LineToAdd.setPrintedLineDescription("");
        logger.info("linedescr 1 [" + LineToAdd.getPrintedLineDescription() + "]");
        pushVirtualVoucherElements(LineToAdd);
        CreditCardVoucher LineToAdd1 = new CreditCardVoucher();

        LineToAdd1.setTypeOfLine('D');
        slpLine.init(" *").onto(2, dig[5]).push(dig[4]).push(dig[3]).push(dig[2]).push(dig[1]).push(dig[0])
                .onto(35, tnd[ind].symbol).upto(51, amt);
        LineToAdd1.setPrintedLineDescription(slpLine.toString());
        logger.info("linedescr 2 [" + LineToAdd1.getPrintedLineDescription() + "]");
        pushVirtualVoucherElements(LineToAdd1);
        CreditCardVoucher LineToAdd2 = new CreditCardVoucher();

        LineToAdd2.setTypeOfLine('D');

        slpLine.init(' ').onto(12, tra.number);
        LineToAdd2.setPrintedLineDescription(slpLine.toString());
        logger.info("linedescr 3 [" + LineToAdd2.getPrintedLineDescription() + "]");
        pushVirtualVoucherElements(LineToAdd2);
        CreditCardVoucher LineToAdd3 = new CreditCardVoucher();

        LineToAdd3.setTypeOfLine('D');
        slpLine.init(' ').onto(12, chk_line);
        LineToAdd3.setPrintedLineDescription(slpLine.toString());
        logger.info("linedescr 4 [" + LineToAdd3.getPrintedLineDescription() + "]");
        pushVirtualVoucherElements(LineToAdd3);
        CreditCardVoucher LineToAdd4 = new CreditCardVoucher();

        LineToAdd4.setTypeOfLine('D');
        slpLine.init(' ').onto(12, editNum(ctl.tran, 4)).skip().push(editNum(ctl.sto_nbr, 4)).push('/')
                .push(editKey(ctl.reg_nbr, 3)).push('/').push(editNum(ctl.ckr_nbr, 3));
        LineToAdd4.setPrintedLineDescription(slpLine.toString());
        logger.info("linedescr 5 [" + LineToAdd4.getPrintedLineDescription() + "]");
        pushVirtualVoucherElements(LineToAdd4);
        CreditCardVoucher LineToAdd5 = new CreditCardVoucher();

        LineToAdd5.setTypeOfLine('D');
        slpLine.init(' ').upto(32, nbr).onto(35, editDate(ctl.date)).skip(3).push(editTime(ctl.time / 100));
        LineToAdd5.setPrintedLineDescription(slpLine.toString());
        logger.info("linedescr 6 [" + LineToAdd5.getPrintedLineDescription() + "]");
        pushVirtualVoucherElements(LineToAdd5);
        CreditCardVoucher LineToAdd6 = new CreditCardVoucher();

        LineToAdd6.setTypeOfLine('E');
        LineToAdd6.setPrintedLineDescription("");
        logger.info("linedescr 7 [" + LineToAdd6.getPrintedLineDescription() + "]");
        pushVirtualVoucherElements(LineToAdd6);

        logger.info("EXIT createVirtualVoucher");
    }

    public static void removeCreditCardVoucher() {
        logger.info("ENTER removeCreditCardVoucher");

        if (creditCardVoucher.isEmpty()) {
            logger.info("EXIT removeCreditCardVoucher - isEmpty");
            return;
        }

        creditCardVoucher.removeAllElements();
        voucherCopyNumber.removeAllElements();

        logger.info("EXIT removeCreditCardVoucher");
    }

    public static boolean thereIsVoucher() {
        return !creditCardVoucher.isEmpty();
    }

    public static int getVoucherCopyNumber(boolean firstcopyonreceipt) {
        logger.info("ENTER getVoucherCopyNumber");
        logger.info("firstcopyonreceipt: " + firstcopyonreceipt);

        int num = 0;

        if (!voucherCopyNumber.isEmpty()) {
            if (firstcopyonreceipt) {
                num = ((Integer) voucherCopyNumber.elementAt(0)).intValue();
                for (int i = 0; i < voucherCopyNumber.size(); i++) {
                    voucherCopyNumber
                            .setElementAt(new Integer(((Integer) voucherCopyNumber.elementAt(i)).intValue() - 1), i);
                }
            } else {
                num = ((Integer) voucherCopyNumber.remove(0)).intValue();
            }
        }

        logger.info("ENTER getVoucherCopyNumber - return " + num);
        return num;
    }

    public static void haveToPrintCreditCardVoucher() {
        voucherFiscalReceipt = false;
    }

    public static void hateToPrintCreditCardVoucher(boolean firstcopyonreceipt) {
        while (PrintCCV(firstcopyonreceipt)) {
        }
    }

    public static boolean PrintCCV(boolean firstcopyonreceipt) {
        logger.info("ENTER PrintCCV");
        logger.info("firstcopyonreceipt: " + firstcopyonreceipt);
        // First see if there's anything in the vector. Quit if so.
        if (((tra.mode & M_CANCEL) > 0) || ((tra.mode & M_SUSPND) > 0)) {
            firstcopyonreceipt = false;
        }
        if (creditCardVoucher.isEmpty()) {
            logger.info("EXIT PrintCCV 1");

            return false;
        }
        if (tra.mode != 2) {
            PosGPE.deleteEptsVoidFlag();
        }

        // Number of voucher copy to print
        int vouchersNumber, printtype = 0;
        int maxVouchersNumber = 0;

        vouchersNumber = getVoucherCopyNumber(firstcopyonreceipt);
        if (!firstcopyonreceipt) {
            logger.info("vouchersNumber = " + vouchersNumber);
            if (((tra.mode & M_CANCEL) > 0) || ((tra.mode & M_SUSPND) > 0)) {
                vouchersNumber = 2;
            }
            maxVouchersNumber = vouchersNumber;
            logger.info("vouchersNumber = " + vouchersNumber);
            printtype = PRINTNORMAL;
        } else {
            if (tra.mode != M_VOID && tra.mode != M_SUSPND) {
                vouchersNumber = 1;
                maxVouchersNumber = vouchersNumber;
                printtype = PRINTCOMMENTAFTERLOGO;

                DevIo.tpmPrint(2, 0, "");
            }
        }
        Vector tmp = new Vector();
        int nov = 0;

        logger.info("creditCardVoucher.size(): " + creditCardVoucher.size());
        while (nov < creditCardVoucher.size()) {
            CreditCardVoucher ccv = (CreditCardVoucher) creditCardVoucher.elementAt(nov);

            tmp.add(ccv);
            if (!firstcopyonreceipt) {
                creditCardVoucher.remove(ccv);
                nov--;
                if (ccv.getTypeOfLine() == 'E') {
                    break;
                }
            }
            nov++;
        }
        if (!firstcopyonreceipt) {
            if (vouchersNumber == 0) {
                logger.info("EXIT PrintCCV 2");

                return (creditCardVoucher.size() > 0);
            }
        }
        while ((vouchersNumber--) > 0) {
            for (int counter = 0; counter < tmp.size(); counter++) {
                CreditCardVoucher ccv = (CreditCardVoucher) tmp.elementAt(counter);

                logger.info("ccv.getTypeOfLine () = " + ccv.getTypeOfLine());
                if (ccv.getPrintedLineDescription().equals("SKIP VOUCHER")) {
                    if ((!firstcopyonreceipt) && ((vouchersNumber + 1) != maxVouchersNumber)) {
                        break;
                    }
                }

                logger.info("ccv.getTypeOfLine(): " + ccv.getTypeOfLine());
                switch (ccv.getTypeOfLine()) {
                    case 'B':
                        if (!firstcopyonreceipt) {
                            DevIo.tpmPrint(2, 0, "");
                        }
                        break;

                    case 'E':
                        GdRegis.set_trailer(-1);

                        if (!firstcopyonreceipt) {
                            DevIo.tpmPrint(2, 0, rightFill(prtLine.toString(), 41, ' '));
                            DevIo.tpmPrint(2, 0, rightFill(ccv.getPrintedLineDescription(), 41, ' '));
                        }
                        break;

                    case 'D':
                    default:
                        DevIo.tpmPrint(2, 0, rightFill(ccv.getPrintedLineDescription(), 41, ' '));
                        break;
                }
            }
            GdRegis.hdr_print();
        }

        logger.info("EXIT PrintCCV");
        return (creditCardVoucher.size() > 0 && (!firstcopyonreceipt));
    }

    public static void pushVirtualVoucherElements(CreditCardVoucher element) {
        creditCardVoucher.addElement(element);
    }

    public static void addVoucherCopyNumber(int copyNumber) {
        voucherCopyNumber.add(new Integer(copyNumber));
    }

    public static void printCreditCardVoucher() {
        logger.info("ENTER printCreditCardVoucher 1");

        while (PrintCCV(voucherFiscalReceipt)) {
        }
    }

    public static void printCreditCardVoucher(int inFiscalReceipt) {
        logger.info("ENTER printCreditCardVoucher 2");
        logger.info("inFiscalReceipt: " + inFiscalReceipt);

        if ((inFiscalReceipt == 0 && voucherFiscalReceipt) || (inFiscalReceipt == 1)) {
            while (PrintCCV((inFiscalReceipt == 0 && voucherFiscalReceipt))) {
            }
        }
    }
    //WINEPTS-CGA#A END

}
