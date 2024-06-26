package com.ncr;

import java.awt.event.ActionEvent;
import java.io.*;

import com.ncr.ecommerce.ECommerceManager;
import com.ncr.ssco.communication.manager.SscoPosManager;
import jpos.*; // JavaPOS generics
import org.apache.log4j.Logger;

abstract public class DevIo extends Struc {
    private static final Logger logger = Logger.getLogger(DevIo.class);
    static int prin_id = 0, till_id = 0x10;
    static int drw_state, drw_timer = -1;

    static CusIo cdsp, odsp;
    static RdrIo rdr1, rdr2;
    public static Wedge wdge;
    static BioIo biom;
    static PrnIo prin = null;
    static Device mfptr = new Device("MFPTR");
    static Device scale = new Device("SCALE");
    static LineMap lMap = new LineMap("PrtLine");

    public static boolean needGraphic(String data) {
        int opt = options[O_Graph] << 8;

        if (opt > 0)
            for (int ind = data.length(); ind-- > 0; ) {
                if ((data.charAt(ind) & 0xff00) == opt)
                    return true;
            }
        return false;
    }

    public static void tpmImage(int dev, String name) {
        if (dev != 2)
            return;
        prin.bitmap(localFile("bmp", name).getPath());
    }

    public static void tpmPrint(int dev, int lfs, String data) {
        if (tra.slip > 0)
            dev &= ~2;
        if (!station(dev))
            return;
        if (dev == 4)
            if (mfptr.state < 0)
                return;
        prin.lfeed(dev, lfs);
        if (needGraphic(data)) {
            if (dev != 2)
                return;
            String name = lMap.update(data, "." + editNum(++tra.bmpSequence, 4));
            if (name != null) {
                logger.info("tpmPrint: BitmapName: " + name);
                prin.bitmap(name);
                return;
            }
        }
        StringBuffer sb = new StringBuffer(66);
        if (data.length() > 0) {
            logger.info("tpmPrint: BitmapName: " + data);
            if (data.charAt(1) == '@') {
                logger.info("tpmPrint: data.chartAt(1)=" + data.charAt(1));
                if (SscoPosManager.getInstance().isEnabled() && !data.contains("qr.bmp")) {
                    logger.info("tpmPrint: SscoPosManager is Enabled?" + SscoPosManager.getInstance().isEnabled());
                    if (data.length() >= 5 && data.charAt(5) == '@') {
                        logger.info("Printing data: " + data);
                        String position = data.substring(2, 5);
                        logger.info("Position= " + position);
                        logger.info("dev= " + dev);
                        prin.logo(dev, "top".equals(position));
                        return;
                    }
                }
                logger.info("Execute tmpImage");
                tpmImage(dev, data.substring(2).trim());
                return;
            }
            prin.ldata(dev, data, sb);
        }
        prin.write(dev, sb.append('\n').toString());
    }

    public static void tpmQrLabel(int dev, String nbr, int width, int height) {
        int type = prin.PTR_BCS_QRCODE;
        prin.labelQR(dev, type, nbr, width, height);
    }

    public static void tpmLabel(int dev, String nbr) {
        logger.info("Call tpmLabel(dev,nbr,110)");
        tpmLabel(dev, nbr, prin.PTR_BCS_Code128);
    }

    public static void tpmLabel(int dev, String nbr, int type) {
        int len = nbr.length();
        logger.info("ENTER tpmLabel()");
        if (!station(dev)) {
            logger.info("EXIT tpmLabel() !station(dev)");
            return;
        }
        if (len == 13)
            type = prin.PTR_BCS_EAN13;
        if (len == 12)
            type = prin.PTR_BCS_UPCA;
        if (len == 9)
            type = prin.PTR_BCS_Code39;
        if (len == 8) {
            if (nbr.charAt(0) == '0') {
                type = prin.PTR_BCS_UPCE;
                nbr = upcSpreadE(nbr);
            } else
                type = prin.PTR_BCS_EAN8;
        }
        if (type == prin.PTR_BCS_Code128)
            nbr = "{B" + nbr;
        if (type == prin.PTR_BCS_Code128_Parsed)
            nbr = "{C" + nbr;
        logger.info("CAll prin.label(" + dev + ", " + type + ", " + nbr);
        prin.label(dev, type, nbr);
        logger.info("EXIT tpmLabel() ok");
    }

    public static void tpmCheque(int ind, String nbr, long value) {
        int dec = tnd[ind].dec, base = 1;

        if (!station(4))
            return;
        if (value < 0)
            value = -value;
        String dig[] = new String[6];
        String amt = editTxt(editDec(value, dec), 10).replace(' ', '*');

        while (dec-- > 0)
            base *= 10;
        value /= base;
        for (int x = (int) value; ++dec < dig.length; x /= 10)
            dig[dec] = chk_nbr[x % 10].substring(3);
        prin.setQuality(4, true);
        slpInsert(options[O_chk42]);
        if (prin.slpColumns > 60) {
            LinIo slpLine = new LinIo("SLP", 1, prin.recColumns == 44 ? 57 : 54);
            if (value > 999999)
                GdPos.panel.clearLink(Mnemo.getInfo(2), 1);
            slpLine.init(" *" + dig[5] + dig[4] + dig[3] + dig[2] + dig[1] + dig[0]).onto(35, tnd[ind].symbol)
                    .upto(51, amt).type(4);
            slpLine.init(' ').onto(12, tra.number).type(4);
            slpLine.init(' ').onto(12, chk_line).type(4);
            slpLine.init(' ').onto(12, editNum(ctl.tran, 4)).skip().push(editNum(ctl.sto_nbr, 4)).push('/')
                    .push(editKey(ctl.reg_nbr, 3)).push('/').push(editNum(ctl.ckr_nbr, 3)).type(4);
            slpLine.init(' ').upto(32, nbr).onto(35, editDate(ctl.date)).upto(52, editTime(ctl.time / 100)).type(4);
        } else {
            LinIo slpLine = new LinIo("SLP", 1, 44);
            if (value > 9999)
                GdPos.panel.clearLink(Mnemo.getInfo(2), 1);
            slpLine.init(dig[3] + dig[2] + dig[1] + dig[0]).onto(23, tnd[ind].symbol).upto(40, amt).type(4);
            slpLine.init(tra.number).type(4);
            slpLine.init(chk_line).type(4);
            slpLine.init(' ').push(editNum(ctl.tran, 4)).skip().push(editNum(ctl.sto_nbr, 4)).push('/')
                    .push(editKey(ctl.reg_nbr, 3)).push('/').push(editNum(ctl.ckr_nbr, 3)).type(4);
            slpLine.init(' ').upto(20, nbr).onto(23, editDate(ctl.date)).upto(40, editTime(ctl.time / 100)).type(4);
        }
        slpRemove();
        prin.setQuality(4, false);
        WinEPTSVoucherManager.createVirtualVoucher(ind, nbr, amt, dig);
    }

    public static void cutPaper() {
        if ((prin_id & 2) == 0)
            return;
        prin.knife(prin_id);
        if (prin.paperState(2)) {
            logger.warn("Paper state error");
            if (!ECommerceManager.getInstance().isEnabled() || ECommerceManager.getInstance().getBasket() == null)
                GdPos.panel.clearLink(Mnemo.getInfo(12), 1);
        }
        if (prin.logo.exists())
            prin.write(2, "\u001b|1B");
    }

    public static void slpInsert(int lfs) {
        prin.waitIdle();
        while (true) {
            try {
                prin.prn1.beginInsertion(2000);
                prin.prn1.endInsertion();
                break;
            } catch (JposException je) {
                if (GdPos.panel.clearLink(Mnemo.getInfo(18), 5) > 1) {
                    mfptr.state = ERROR;
                    return;
                }
            }
        }
        prin.lfeed(mfptr.state = 4, lfs);
    }

    public static void slpRemove() {
        if (mfptr.state > 0)
            prin.waitIdle();
        else
            GdPos.panel.display(2, Mnemo.getInfo(23));
        while (true) {
            try {
                prin.prn1.beginRemoval(2000);
                prin.prn1.endRemoval();
                break;
            } catch (JposException je) {
                if (je.getErrorCodeExtended() == prin.JPOS_EPTR_SLP_EMPTY)
                    break;
                GdPos.panel.clearLink(Mnemo.getInfo(19), 1);
            }
        }
        GdPos.panel.display(2, editTxt("", 20));
        mfptr.state = 0;
    }

    public static boolean station(int dev) {
        return (prin_id & dev) > 0;
    }

    /***************************************************************************
     * sound tone using JavaPos ToneIndicator (wedge or speaker), if not configured by Toolkit (sound device or speaker)
     *
     * @param type
     *            0 = error, 1 = alert
     ***************************************************************************/
    public static void alert(int type) {
        if (!wdge.kbdTone(type)) {
            if (File.separatorChar == '/')
                java.awt.Toolkit.getDefaultToolkit().beep();
            else {
                try {
                    Runtime.getRuntime().exec("BEEP");
                } catch (Exception e) {
                }
            }
        }
        // System.err.print ('\7'); /* by Java Console */
    }
    public static void alert() {
        if (!wdge.kbdTone(1)) {
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
    }

    public static boolean drwOpened() {
        if ((till_id & 0x10) > 0)
            return true;
        return prin.tillState();
    }

    /***************************************************************************
     * open cashdrawer
     *
     * @param nbr
     *            cashdrawer id 1 or 2 (0=both)
     ***************************************************************************/
    public static void drwPulse(int nbr) {
        if (ctl.mode > 0) {
            if (ctl.mode == M_RENTRY)
                return;
            else if ((till_id & 2) == 0)
                return;
        }
        drw_state = nbr > 0 ? nbr : 3;
        if ((till_id & 0x10) > 0)
            return;
        prin.waitIdle();
        if (nbr < 1) {
            prin.pulse(nbr = 1);
        }
        prin.pulse(--nbr);
    }

    public static void drwCheck(int ticks) {
        if (SscoPosManager.getInstance().isEnabled()) return;

        if (drw_state > 0)
            if (drwOpened()) {
                drw_timer = ticks;
                GdPos.panel.clearLink(Mnemo.getInfo(10), (till_id & 0x11) > 0 ? 0x11 : 0x10);
                drw_state = 0;
                drw_timer = ERROR;
            }
        if (mon.adv_rec < 0) {
            cdsp.clear();
            mon.adv_rec = 0;
        }
        // power_check ();
        if (station(1)) {
            if (prin.paperState(1))
                GdPos.panel.clearLink(Mnemo.getInfo(11), 1);
        }
    }

    public static boolean drwWatch(int ticks) {
        if (drw_timer >= 0) {
            if (!drwOpened())
                return true;
            if (drw_timer > 0)
                if (--drw_timer < 1) {
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
        if (odsp != null)
            odsp.write(line, data);
    }

    public static void oplSignal(int lamp, int mode) {
        if (odsp != null)
            odsp.blink(lamp, mode);
    }

    public static boolean hasKeylock() {
        return wdge.keyLock();
    }

    public static void start() {
        RdrIo.scale = scale;
        wdge = new Wedge(); /* Jpos devices keylock and tone */
        biom = new BioIo(); /* Jpos device biometrics */
        odsp = new CusIo(0); /* Jpos device operator display */
        cdsp = new CusIo(1); /* Jpos device customer display */
        rdr1 = new RdrIo(1); /* Jpos device scanner/scale */
        rdr2 = new RdrIo(2); /* Jpos device scanner/scale */
        wdge.init(); /* Jpos devices msr and scanner */
        biom.init(); /* Jpos device biometrics */
        prin = new PrnIo(mfptr);
        if (prin.jposActive(prin.prn1))
            prin_id = prin.init(0x50);
        till_id = options[O_xTill];
        if (!prin.jposActive(prin.drw1))
            if (!prin.jposActive(prin.drw2))
                till_id |= 0x10;
        prin_id &= ~Integer.parseInt(System.getProperty("NOP", "0"), 16);
        if ((prin_id & 0x10) == 0)
            prin.setPitch(56);
    }

    public static void stop() {
        if (prin != null)
            prin.stop();
        biom.stop();
        rdr1.stop();
        rdr2.stop();
        cdsp.stop();
        odsp.stop();
        wdge.stop();
    }

    public static void setAlerted(int nbr) {
        int msk = Integer.getInteger("RDR_BEEP", 0).intValue();
        RdrIo.alert |= 1 << nbr & msk;
    }

    public static void setEnabled(boolean state) {
        if (rdr1 != null)
            rdr1.setEnabled(state);
        if (rdr2 != null)
            rdr2.setEnabled(state);
        wdge.setEnabled(state);
    }


    public static void postInput(String cmd, byte[] data) {

        if (data != null) {
            cmd += new String(data);
        }
        if (cmd.startsWith("SELECT")) {
            UtilLog4j.logDebug(DevIo.class, "cmd=" + cmd);
        } else {
            UtilLog4j.logInformation(DevIo.class, "cmd=" + cmd);
        }
        ActionEvent e = new ActionEvent(GdPos.panel.idle, ActionEvent.ACTION_PERFORMED, cmd);
        if (cmd.startsWith("CODE")) {
            GdPos.panel.innerList.add(e);
        }
        GdPos.panel.queue.postEvent(e);

    }

}

