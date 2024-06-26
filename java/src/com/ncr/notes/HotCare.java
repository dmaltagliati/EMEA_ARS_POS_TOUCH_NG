package com.ncr.notes;

import com.ncr.*;

import java.io.File;

public class HotCare extends Basis implements Runnable {
    int mnt_add = 0, mnt_chg = 0, mnt_del = 0;
    int mnt_tic = -1;

    public HshIo sPLU = new HshIo(lPLU.id, lPLU.fixSize, lPLU.record.length);
    public HshIo sCLU = new HshIo(lCLU.id, lCLU.fixSize, lCLU.record.length);
    public HshIo sGLU = new HshIo(lGLU.id, lGLU.fixSize, lGLU.record.length);
    public LocalREG sREG = new LocalREG("REG", 1);

    public void rfc_apply() {
        int rec, ic = 0, sc = 0;

        try {
            ic = rMNT.skip(20).scanNum(2);
            sc = rMNT.scanNum(2);
        } catch (NumberFormatException e) {
            rMNT.error(e, false);
        }
        if (ic < 11 || sc != 8)
            return;
        if ((rec = reg.find(ic, sc)) == 0)
            return;
        sREG.onto(0, rMNT.pb.substring(rMNT.index));
        sREG.rewrite(rec, 4);
        sREG.read(rec, LOCAL);
        TndMedia ptr = tnd[ic -= 10];
        ptr.init(sc, sREG);
        String s = ptr.editXrate(true);
        logConsole(2, "rfc " + editNum(ic, 2) + ":" + s, null);
        GdPos.panel.dspStatus(0, s, true, false);
    }

    public void fxf_apply() {
        int ind, sts = -1;
        String type = rMNT.skip().scan(3);

        if (type.equals("RFC")) {
            rfc_apply();
            return;
        }
        String src = localPath(rMNT.pb.substring(++rMNT.index).trim());
        String tar = null, tmp = "LAST_F2F.TMP";
        if (src.length() < 1)
            return;
        if ((ind = src.indexOf(' ')) > 0) {
            tar = src.substring(ind).trim();
            src = src.substring(0, ind);
        }
        if (type.equals("F2D")) {
            File f = localFile(null, src);
            if (!f.exists())
                return;
            GdPos.panel.dspStatus(0, src + "--->nul", true, false);
            logConsole(2, "del " + src, null);
            localMove(null, f);
        }
        if (type.equals("F2X")) {
            GdPos.panel.dspStatus(0, "run " + src, true, false);
            if (tar != null)
                src += " " + tar;
            sts = HotProc.exec("7052_F2X.BAT " + src);
            logConsole(2, "run " + src + " rc=" + sts, null);
        }
        if (type.equals("T2F")) {
            if ((ind = src.indexOf("REG")) >= 0) {
                String s = src.substring(0, ind) + REG + src.substring(ind + 3);
                if ((sts = netio.copyF2f(s, tmp, false)) > 0)
                    return;
            }
        } else if (!type.equals("F2F"))
            return;
        if (sts < 0)
            if (netio.copyF2f(src, tmp, false) != 0)
                return;
        src = src.substring(src.lastIndexOf(File.separatorChar) + 1);
        File f = localFile(tar, src);
        logConsole(2, "add " + f.getPath(), null);
        localMove(new File(tmp), f);
        if (tar == null)
            tar = ".";
        GdPos.panel.dspStatus(0, src + "--->" + tar, true, false);
    }

    public void cnt_show(int tic) {
        if (mnt_tic != tic) {
            mnt_tic = tic;
            cntLine.init(mnt_line.substring(20)).onto(1, editNum(mnt_add, 5)).onto(8, editNum(mnt_chg, 5)).onto(15,
                    editNum(mnt_del, 5));
            GdPos.panel.dspStatus(0, cntLine.toString(), true, false);
        }
    }

    public void run() {
        for (; ; Thread.yield()) {
            if (netio.readMnt('A', rMNT.recno, rMNT) > 0) {
                rMNT.recno++;
                try /* validate 10 bytes header */ {
                    int nbr = rMNT.skip().scanNum(4);
                    if (nbr > 0 && nbr != ctl.sto_nbr)
                        continue;
                    nbr = rMNT.scan(':').scanKey(3);
                    if (nbr != ctl.reg_nbr && nbr != ctl.grp_nbr)
                        if (nbr > 0)
                            continue;
                    rMNT.scan(':');
                } catch (NumberFormatException e) {
                    rMNT.error(e, false);
                    continue;
                }
                char id = rMNT.pb.charAt(rMNT.index);
                if (id == '*') {
                    fxf_apply();
                    continue;
                }
                HshIo io = id == 'C' ? sCLU : sPLU;
                if (id == 'G')
                    io = sGLU;
                String data = rMNT.pb.substring(rMNT.index);
                if (data.charAt(io.fixSize - 1) < '0')
                    continue;
                int sts = io.find(rMNT.scan(io.fixSize));
                if (sts < 0)
                    continue;
                if (rMNT.scan() != '-') {
                    io.push(data);
                    io.rewrite(io.recno, 0);
                    if (sts == 0)
                        mnt_add++;
                    else
                        mnt_chg++;
                } else if (sts > 0) {
                    io.delete(io.recno);
                    mnt_del++;
                }
                cnt_show(ctl.time % 100);
                continue;
            }
            if (rMNT.recno > 0) {
                sPLU.sync();
                sCLU.sync();
                sGLU.sync();
                cnt_show(-1);
                cntLine.recno = rMNT.recno - 1;
                rMNT.recno = 0;
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }
}
