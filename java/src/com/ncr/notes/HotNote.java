package com.ncr.notes;

import com.ncr.Basis;
import com.ncr.DevIo;
import com.ncr.RmoteNEW;
import com.ncr.GdPos;
public class HotNote extends Basis implements Runnable {
    int recRcv = 0, recSnd = 0;
    RmoteNEW sNEW = new RmoteNEW(rNEW.id);

    public void run() {
        while (true) {
            if (netio.newsReset) {
                netio.newsReset = false;
                recRcv = recSnd = 0;
                mon.alert = -1;
                mon.snd_mon = null;
            }
            if (mon.alert < 0) {
                mon.alert = 0;
                mon.rcv_mon = null;
            }
            if (mon.snd_mon != null) {
                if (sNEW.read(recSnd) > 0) {
                    if (sNEW.reg2 == 0 || sNEW.reg2 >= 0xf00 || sNEW.sts == 0) {
                        mon.snd_dsp = 0;
                        mon.snd_mon = null;
                    }
                }
            }
            for (; mon.rcv_mon == null; recRcv++) {
                if (sNEW.read(recRcv + 1) < 1)
                    break;
                if (mon.snd_mon == null)
                    if (recRcv == recSnd)
                        if (sNEW.reg1 != ctl.reg_nbr || sNEW.sts == 0)
                            recSnd++;
                if (sNEW.sts == 0)
                    continue;
                if (sNEW.reg2 != ctl.reg_nbr) {
                    if (sNEW.reg1 == ctl.reg_nbr)
                        continue;
                    if (sNEW.reg2 > 0)
                        if (sNEW.reg2 != ctl.grp_nbr)
                            continue;
                }
                if (sNEW.text.endsWith("!"))
                    mon.alert++;
                mon.rcv_ckr = sNEW.ckr;
                mon.rcv_msg = sNEW.text;
                mon.rcv_mon = editKey(sNEW.reg1, 3) + '/' + editNum(sNEW.ckr, 3) + "  " + editTime(sNEW.tim1);
            }
            for (; mon.snd_mon == null; recSnd++) {
                if (mon.rcv_mon == null)
                    if (recSnd == recRcv)
                        break;
                if (sNEW.read(recSnd + 1) < 1)
                    break;
                if (sNEW.reg1 != ctl.reg_nbr || sNEW.sts == 0)
                    continue;
                mon.snd_mon = "#" + sNEW.sts + "->" + editKey(sNEW.reg2, 3) + "  " + editTime(sNEW.tim1);
            }
            if (mon.rcv_dsp != recRcv) {
                mon.rcv_dsp = recRcv;
                GdPos.panel.dspNotes(0, mon.rcv_mon);
            }
            if (mon.snd_dsp != recSnd) {
                mon.snd_dsp = recSnd;
                GdPos.panel.dspNotes(1, mon.snd_mon);
            }
            int lamp = mon.snd_mon == null ? 0 : 1;
            DevIo.oplSignal(3, mon.rcv_mon == null ? lamp : 2);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
