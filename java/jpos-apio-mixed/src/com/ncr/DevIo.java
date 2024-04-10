package com.ncr;

import com.ncr.gpe.PosGPE;
import org.apache.log4j.Logger;

import java.util.Vector;

abstract public class DevIo extends Struc {
    private static final Logger logger = Logger.getLogger(DevIo.class);

    static int prin_id = 0, till_id = 0x10;
    static int drw_state, drw_timer = -1;

    static CusIo cdsp, odsp;
    static RdrIo rdr1, rdr2;
    static Wedge wdge;
    static BioIo biom;
    static PrnIo prin = null;
    static Device mfptr = new Device("MFPTR");
    static Device scale = new Device("SCALE");
    static LineMap lMap = new LineMap("PrtLine");

    //WINEPTS-CGA#A BEG
    private static Vector creditCardVoucher = new Vector();
    private static Vector voucherCopyNumber = new Vector();
    private static boolean voucherFiscalReceipt = false;
    private static final int PRINTNORMAL = 1;
    private static final int PRINTCOMMENTAFTERLOGO = 16;
    private static final int BEGINNORMAL = 0;
    private static final int ENDNORMAL = 2;
    private static final int PRINTFIXEDOUTPUT = 32;
    private static final int PRINTTRAILERLINE = 33;

    //WINEPTS-CGA#A END
    public static boolean needGraphic(String data) {
        int opt = options[O_Graph] << 8;

        if (opt > 0) for (int ind = data.length(); ind-- > 0; ) {
            if ((data.charAt(ind) & 0xff00) == opt) return true;
        }
        return false;
    }

    public static void tpmImage(int dev, String name) {
        if (dev != 2) return;
        prin.paperState();
        prin.bitmap(localFile("bmp", name).getPath());
    }

    public static void tpmPrint(int dev, int lfs, String data) {
        if (tra.slip > 0) dev &= ~2;
        if (!station(dev)) return;
        if (dev == 4) if (mfptr.state < 0) return;
        prin.lfeed(dev, lfs);
        if (needGraphic(data)) {
            if (dev != 2) return;
            String name = lMap.update(data);
            if (name != null) {
                prin.bitmap(name);
                return;
            }
        }
        StringBuffer sb = new StringBuffer(66);
        if (data.length() > 0) {
            if (data.charAt(1) == '@') {
                tpmImage(dev, data.substring(2).trim());
                return;
            }
            prin.ldata(dev, data, sb);
        }
        prin.write(sb.append('\n').toString());
    }

    public static void tpmLabel(int dev, String nbr) {
        char type = 'I';
        int len = nbr.length();

        if (!station(dev)) return;
        if (len == 13) type = 'C'; /* ean13 */
        if (len == 12) type = 'A'; /* upc-A */
        if (len == 9) type = 'E'; /* Code39 */
        if (len == 8) {
            if (nbr.charAt(0) == '0') {
                type = 'B'; /* upc-E */
                nbr = upcSpreadE(nbr);
            } else type = 'D'; /* ean-8 */
        }
        if (type == 'I') nbr = "{B" + nbr;
        prin.label(dev, type, nbr);
    }

    public static void tpmCheque(int ind, String nbr, long value) {
        int dec = tnd[ind].dec, base = 1;

        if (!station(4)) return;
        if (value < 0) value = -value;
        String dig[] = new String[6];
        String amt = editTxt(editDec(value, dec), 10).replace(' ', '*');

        while (dec-- > 0) base *= 10;
        value /= base;
        for (int x = (int) value; ++dec < dig.length; x /= 10)
            dig[dec] = chk_nbr[x % 10].substring(3);
        slpInsert(options[O_chk42]);
        if (mfptr.state > 0) prin.bold(1);
        if (prin.slpColumns > 60) {
            LinIo slpLine = new LinIo("SLP", 1, prin.recColumns == 44 ? 57 : 54);
            if (value > 999999) gui.clearLink(Mnemo.getInfo(2), 1);
            slpLine.init(" *" + dig[5] + dig[4] + dig[3] + dig[2] + dig[1] + dig[0])
                    .onto(35, tnd[ind].symbol).upto(51, amt).type(4);
            slpLine.init(' ').onto(12, tra.number).type(4);
            slpLine.init(' ').onto(12, chk_line).type(4);
            slpLine.init(' ').onto(12, editNum(ctl.tran, 4)).skip()
                    .push(editNum(ctl.sto_nbr, 4)).push('/')
                    .push(editKey(ctl.reg_nbr, 3)).push('/')
                    .push(editNum(ctl.ckr_nbr, 3)).type(4);
            slpLine.init(' ').upto(32, nbr).onto(35, editDate(ctl.date))
                    .upto(52, editTime(ctl.time / 100)).type(4);
        } else {
            LinIo slpLine = new LinIo("SLP", 1, 44);
            if (value > 9999) gui.clearLink(Mnemo.getInfo(2), 1);
            slpLine.init(dig[3] + dig[2] + dig[1] + dig[0])
                    .onto(23, tnd[ind].symbol).upto(40, amt).type(4);
            slpLine.init(tra.number).type(4);
            slpLine.init(chk_line).type(4);
            slpLine.init(' ').push(editNum(ctl.tran, 4)).skip()
                    .push(editNum(ctl.sto_nbr, 4)).push('/')
                    .push(editKey(ctl.reg_nbr, 3)).push('/')
                    .push(editNum(ctl.ckr_nbr, 3)).type(4);
            slpLine.init(' ').upto(20, nbr).onto(23, editDate(ctl.date))
                    .upto(40, editTime(ctl.time / 100)).type(4);
        }
        if (mfptr.state > 0) prin.bold(0);
        slpRemove();
        createVirtualVoucher(ind, nbr, amt, dig);  //WINEPTS-CGA#A
    }

    public static boolean tpmMICRead() {
        for (int sts = ERROR; sts != 0; slpRemove()) {
            gui.display(2, Mnemo.getInfo(60));
            prin.paperState();
            do {
                if (gui.clearLink(Mnemo.getInfo(18), 5) > 1) return false;
            } while ((prin.slipState() & 1) < 1);
            prin.select(mfptr.state = 4);
            if ((sts = prin.readMICR(prtLine)) == 0)
                continue;
            if (sts > 0) logConsole(0, "MICRstatus=" + sts, null);
            gui.display(2, Mnemo.getInfo(9));
        }
        return true;
    }

    public static void cutPaper() {
        if ((prin_id & 2) == 0) return;
        prin.knife(prin_id);
        if ((prin.paperState() & 2) > 0)
            gui.clearLink(Mnemo.getInfo(12), 1);
        if (prin.logo.exists()) prin.center("\u001d/\u0000");
    }

    public static void slpInsert(int lfs) {
        prin.paperState();
        mfptr.state = ERROR;
        do {
            if (gui.clearLink(Mnemo.getInfo(18), 5) > 1) return;
        } while ((prin.slipState() & 1) < 1);
        prin.select(mfptr.state = 4);
        prin.lfeed(4, lfs);
        prin.write("\u001bK" + (char) prin.slpTopzone);
    }

    public static void slpRemove() {
        if (mfptr.state > 0) {
            prin.paperState();
            prin.write("\u000c");
        } else gui.display(2, Mnemo.getInfo(23));
        mfptr.state = 0;
        do {
            gui.clearLink(Mnemo.getInfo(19), 1);
        } while ((prin.slipState() & 3) > 0);
        gui.display(2, editTxt("", 20));
        prin.select(2);
    }

    public static boolean station(int dev) {
        return (prin_id & dev) > 0;
    }

    /***************************************************************************
     *  sound tone using utility SPEAKER (wedge or 3rd-party),
     *  if unavailable by Toolkit (sound device or speaker)
     *
     *  @param type  0 = error, 1 = alert
     ***************************************************************************/
    public static void alert(int type) {
        if (!wdge.kbdTone(type)) {
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
        // System.err.print ('\7'); /* by Java Console */
    }

    public static boolean drwOpened() {
        if ((till_id & 0x10) > 0) return true;
        dspLine.show(1);
        return prin.tillState() < 1;
    }

    /***************************************************************************
     *  open cashdrawer
     *
     *  @param nbr   cashdrawer id 1 or 2 (0=both)
     ***************************************************************************/
    public static void drwPulse(int nbr) {
        if (ctl.mode > 0) {
            if (ctl.mode == M_RENTRY) return;
            else if ((till_id & 2) == 0) return;
        }
        drw_state = nbr > 0 ? nbr : 3;
        if ((till_id & 0x10) > 0) return;
        prin.paperState();
        if (nbr < 1) {
            prin.pulse(nbr = 1);
        }
        prin.pulse(--nbr);
    }

    public static void drwCheck(int ticks) {
        if (drw_state > 0) if (drwOpened()) {
            drw_timer = ticks;
            gui.clearLink(Mnemo.getInfo(10), (till_id & 0x11) > 0 ? 0x11 : 0x10);
            drw_state = 0;
            drw_timer = ERROR;
        }
        if (mon.adv_rec < 0) {
            cdsp.clear();
            mon.adv_rec = 0;
        }
//    power_check ();
        if (station(1)) {
            if ((prin.paperState() & 1) > 0)
                gui.clearLink(Mnemo.getInfo(11), 1);
        }
    }

    public static boolean drwWatch(int ticks) {
        if (drw_timer >= 0) {
            if (!drwOpened()) return true;
            if (drw_timer > 0) if (--drw_timer < 1) {
                drw_timer = ticks;
                alert(1);
            }
        }
        return false;
    }

    public static void cusDisplay(int line, String data) {
        cdsp.write(line, data);
    }

    public static void oplDisplay(int line, String data) {
        if (data.length() != 20)
            data = rightFill(data, 20, ' ');
        if (odsp != null) odsp.write(line, data);
    }

    public static void oplSignal(int lamp, int mode) {
        if (odsp != null) odsp.blink(lamp, mode);
    }

    public static boolean hasKeylock() {
        return wdge.keyLock();
    }

    public static void start() {
        logger.debug("Enter");
        RdrIo.scale = scale;
        wdge = new Wedge();
        biom = new BioIo();
        odsp = new CusIo(0);
        cdsp = new CusIo(1);
        rdr1 = new RdrIo(1);
        rdr2 = new RdrIo(2);
        wdge.init(); /* Jpos devices msr and scanner */
        biom.init(); /* Jpos device biometrics */
        prin = new PrnIo(mfptr);
        if (prin.port != null) {
            prin_id = 0xDF;
            if (mfptr.version >= 7190) prin_id &= ~0x04;
            if (mfptr.version != 7162) prin_id &= ~0x01;
            till_id = options[O_xTill];
        }
        prin_id &= ~Integer.parseInt(System.getProperty("NOP", "0"), 16);
        if ((prin_id & 0x10) == 0) if (prin.recCompressed == 0)
            prin.recCompressed = prin.recColumns - 2;
        logger.debug("Exit");
    }

    public static void stop() {
        logger.debug("Enter");
        if (prin != null) prin.stop();
        biom.stop();
        rdr1.stop();
        rdr2.stop();
        cdsp.stop();
        odsp.stop();
        wdge.stop();
        logger.debug("Exit");
    }

    public static void setAlerted(int nbr) {
        int msk = Integer.getInteger("RDR_BEEP", 0).intValue();
        RdrIo.alert |= 1 << nbr & msk;
    }

    public static void setEnabled(boolean state) {
        if (rdr1 != null) rdr1.setEnabled(state);
        if (rdr2 != null) rdr2.setEnabled(state);
        wdge.setEnabled(state);
    }

    //WINEPTS-CGA#A BEG
    public static void createVirtualVoucher(int ind, String nbr, String amt, String dig[]) {
        LinIo slpLine = new LinIo("SLP", 1, prin.recColumns == 44 ? 57 : 54);
        CreditCardVoucher LineToAdd = new CreditCardVoucher();

        LineToAdd.setTypeOfLine('B');
        LineToAdd.setPrintedLineDescription("");
        pushVirtualVoucherElements(LineToAdd);
        CreditCardVoucher LineToAdd1 = new CreditCardVoucher();

        LineToAdd1.setTypeOfLine('D');
        slpLine.init(" *").onto(2, dig[5]).push(dig[4]).push(dig[3]).push(dig[2]).push(dig[1]).push(dig[0])
                .onto(35, tnd[ind].symbol).upto(51, amt);
        LineToAdd1.setPrintedLineDescription(slpLine.toString());
        pushVirtualVoucherElements(LineToAdd1);
        CreditCardVoucher LineToAdd2 = new CreditCardVoucher();

        LineToAdd2.setTypeOfLine('D');

        slpLine.init(' ').onto(12, tra.number);
        LineToAdd2.setPrintedLineDescription(slpLine.toString());
        pushVirtualVoucherElements(LineToAdd2);
        CreditCardVoucher LineToAdd3 = new CreditCardVoucher();

        LineToAdd3.setTypeOfLine('D');
        slpLine.init(' ').onto(12, chk_line);
        LineToAdd3.setPrintedLineDescription(slpLine.toString());
        pushVirtualVoucherElements(LineToAdd3);
        CreditCardVoucher LineToAdd4 = new CreditCardVoucher();

        LineToAdd4.setTypeOfLine('D');
        slpLine.init(' ').onto(12, editNum(ctl.tran, 4)).skip().push(editNum(ctl.sto_nbr, 4)).push('/')
                .push(editKey(ctl.reg_nbr, 3)).push('/').push(editNum(ctl.ckr_nbr, 3));
        LineToAdd4.setPrintedLineDescription(slpLine.toString());
        pushVirtualVoucherElements(LineToAdd4);
        CreditCardVoucher LineToAdd5 = new CreditCardVoucher();

        LineToAdd5.setTypeOfLine('D');
        slpLine.init(' ').upto(32, nbr).onto(35, editDate(ctl.date)).skip(3).push(editTime(ctl.time / 100));
        LineToAdd5.setPrintedLineDescription(slpLine.toString());
        pushVirtualVoucherElements(LineToAdd5);
        CreditCardVoucher LineToAdd6 = new CreditCardVoucher();

        LineToAdd6.setTypeOfLine('E');
        LineToAdd6.setPrintedLineDescription("");
        pushVirtualVoucherElements(LineToAdd6);

    }

    public static void removeCreditCardVoucher() {
        if (creditCardVoucher.isEmpty()) {
            return;
        }

        creditCardVoucher.removeAllElements();
        voucherCopyNumber.removeAllElements();
    }

    public static boolean ThereIsVoucher() {
        return !creditCardVoucher.isEmpty();
    }

    public static int getVoucherCopyNumber(boolean firstcopyonreceipt) {
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
        // First see if there's anything in the vector. Quit if so.
        if (((tra.mode & M_CANCEL) > 0) || ((tra.mode & M_SUSPND) > 0)) {
            firstcopyonreceipt = false;
        }
        if (creditCardVoucher.isEmpty()) {
            return false;
        }
        if (tra.mode != 2) {
            PosGPE.deleteEptsVoidFlag();
        }

        // Number of voucher copy to print
        int NumberofVoucher, printtype = 0;
        int maxNumberOfVoucher = 0;

        NumberofVoucher = getVoucherCopyNumber(firstcopyonreceipt);
        if (!firstcopyonreceipt) {
            logger.info("NumberofVoucher = " + NumberofVoucher);
            if (((tra.mode & M_CANCEL) > 0) || ((tra.mode & M_SUSPND) > 0)) {
                NumberofVoucher = 2;
            }
            maxNumberOfVoucher = NumberofVoucher;
            logger.info("NumberofVoucher = " + NumberofVoucher);
            printtype = PRINTNORMAL;
        } else {
            if (tra.mode != M_VOID && tra.mode != M_SUSPND) {
                NumberofVoucher = 1;
                maxNumberOfVoucher = NumberofVoucher;
                printtype = PRINTCOMMENTAFTERLOGO;

                DevIo.tpmPrint(2, 0, "");
            }
        }
        Vector tmp = new Vector();
        int nov = 0;

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
            if (NumberofVoucher == 0) {

                return (creditCardVoucher.size() > 0);
            }
        }
        while ((NumberofVoucher--) > 0) {
            for (int counter = 0; counter < tmp.size(); counter++) {
                CreditCardVoucher ccv = (CreditCardVoucher) tmp.elementAt(counter);

                logger.info("ccv.getTypeOfLine () = " + ccv.getTypeOfLine());
                if (ccv.getPrintedLineDescription().equals("SKIP VOUCHER")) {
                    if ((!firstcopyonreceipt) && ((NumberofVoucher + 1) != maxNumberOfVoucher)) {
                        break;
                    }
                }
                switch (ccv.getTypeOfLine()) {
                    case 'B':
						/*if (printerObject.GetCapSlpPresent()) {
							slpInsert(options[O_chk42]);
						} else {*/
                        if (!firstcopyonreceipt) {
                            DevIo.tpmPrint(2, 0, "");
                        }
                        //}
                        break;

                    case 'E':
                        GdRegis.set_trailer();
						/*if (printerObject.GetCapSlpPresent()) {
							DevIo.tpmPrint(4, 0, prtLine.toString());
							slpRemove();
						} else {*/
                        if (!firstcopyonreceipt) {
                            DevIo.tpmPrint(2, 0, prtLine.toString());
                            DevIo.tpmPrint(2, 0, ccv.getPrintedLineDescription());
                        }
                        //}
                        break;

                    case 'D':
                    default:
						/*if (printerObject.GetCapSlpPresent()) {
							DevIo.tpmPrint(4, 0, ccv.getPrintedLineDescription());
						} else {*/
                        DevIo.tpmPrint(2, 0, ccv.getPrintedLineDescription());
                        //}
                        break;
                }
            }
            GdRegis.hdr_print();
        }


        return (creditCardVoucher.size() > 0 && (!firstcopyonreceipt));
    }

    public static void pushVirtualVoucherElements(CreditCardVoucher element) {
        creditCardVoucher.addElement(element);
    }

    public static void addVoucherCopyNumber(int copyNumber) {
        voucherCopyNumber.add(new Integer(copyNumber));
    }

    public static void printCreditCardVoucher() {
        while (PrintCCV(voucherFiscalReceipt)) {
        }
    }

    public static void printCreditCardVoucher(int inFiscalReceipt) {

        if ((inFiscalReceipt == 0 && voucherFiscalReceipt) || (inFiscalReceipt == 1)) {
            while (PrintCCV((inFiscalReceipt == 0 && voucherFiscalReceipt))) {
            }
        }
    }
}