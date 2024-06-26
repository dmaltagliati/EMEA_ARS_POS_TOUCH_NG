package com.ncr;

import com.ncr.ecommerce.ECommerce;
import com.ncr.ecommerce.ECommerceManager;
import com.ncr.zatca.ZatcaManager;
import java.awt.event.*;
import java.util.List;
import java.util.Set;

public abstract class ElJrn extends Basis {
    public static void view(boolean append) {
        GdElJrn area = GdPos.panel.journal;
        int rec, rows = area.rows;

        if (append) {
            rec = lJRN.getSize();

            if (rec <= area.bar.getMaximum())
                area.scroll(KeyEvent.VK_END);
            else
                area.bar.setValues(rec - rows, rows, lJRN.recno, rec);
            if (--rec > lJRN.recno) {
                if ((options[O_ElJrn] & 2) > 0) /* trx-wise */ {
                    lJRN.read(rec);
                    if (lJRN.pb.charAt(0) != ' ')
                        area.bar.setValues(rec, rows, lJRN.recno, rec + rows);
                }
            } else
                area.bar.setValues(rec, rows, lJRN.recno, rec + rows);
        }
        rec = area.bar.getValue() + 1;
        for (int ind = 0; ind < rows; ind++) {
            area.list[ind] = lJRN.read(rec + ind) > 0 ? lJRN.pb : null;
        }
        area.repaint();
        GdPos.panel.jrnPicture(null);
    }

    public static int roll(int vkey) {
        if (ctl.view == 0) {
            if (GdPos.panel.journal.scroll(vkey))
                view(false);
        } else
            TView.scroll(vkey);
        return 0;
    }

    public static int toggle() {
        if (ctl.ckr_nbr > 799)
            return 5;
        if ((options[O_ElJrn] & 0x08) == 0)
            return 7;
        GdPos.panel.pnlRoll.toFront(ctl.view ^= 1);
        if (ctl.view > 0)
            TView.select();
        return 0;
    }

    public static void write(int station, String data) {
        if ((station & 1) > 0) {
            lJRN.init(data).skip(lJRN.dataLen());
            lJRN.write();
            if ((options[O_Sync] & 0x01) > 0)
                lJRN.sync();
            view(true);
        }
        if ((station & 2) > 0)
            if ((tra.slip & 0x40) == 0) {
                lGPO.init(data).skip(lGPO.dataLen());
                lGPO.write();
                if ((options[O_Sync] & 0x02) > 0)
                    lGPO.sync();
            }

        ECommerceManager.getInstance().savePrinterInfo(data);
        ZatcaManager.getInstance().addJournal(data);
    }

    public static int second_cpy(int sel, int nbr, int cpy) {
        int tran, code, ind, line = 0, page = 1, rec;
        String trailer;


        for (rec = lGPO.getSize(); ; ) {
            if (rec < 1)
                return 7;
            lGPO.read(rec--);
            if (lGPO.scan() == ' ')
                continue;
            tran = lGPO.scanNum(4);
            code = lGPO.skip(34).scanNum(2);
            if (tran == nbr)
                break;
        }
        if ((code = slpFind(code)) < 0)
            return 7;
        for (trailer = lGPO.pb; rec > 0; rec--) {
            lGPO.read(rec);
            if (lGPO.scan() > ' ')
                break;
        }
        if (sel > 2) {
            oplLine.init(Mnemo.getText(45)).onto(12, editInt(page)).show(2);
            DevIo.slpInsert(line = slp[code].top - 1);
            if (slp[code].logo == 'L') {
                for (ind = 0; ind < 10; ind++)
                    if (head_txt[ind] != null) {
                        line++;
                        prtLine.init(head_txt[ind]).type(sel);
                    }
            }
        }
        printAdditionalHeader(tra.special);
        if (slp[code].code < 8) {
            for (ind = 10; ind < 16; ind++) {
                if (head_txt[ind] != null) {
                    line++;
                    prtLine.init(head_txt[ind]).type(sel);
                }
                if (options[O_CKRon] == ind) {
                    lCTL.read(ctl.ckr, lCTL.LOCAL);
                    line++;
                    prtLine.init(lCTL.text).type(sel);
                }
            }
        }
        if (cpy > 0)
            if (trailer.charAt(0) == '*') {
                line++;

                if (ECommerceManager.getInstance().mustPrinterInfo()) {
                    prtLine.init(Mnemo.getInfo(22)).type(sel);
                }
                if (ECommerce.getInstashopChoiceType().trim().startsWith("S")
                        || ECommerce.getInstashopChoiceType().trim().equals("F")) {
                    prtLine.init(ECommerce.getSecondCopyDelivery().get(ECommerce.getTndInstashop())).type(sel);
                }

                if (cpy_line != null) {
                    line++;

                    if (ECommerceManager.getInstance().mustPrinterInfo()) {
                        prtLine.init(cpy_line).type(sel);
                    }
                }
                prtLine.init(' ').type(sel);
            }
        while (trailer != null) {
            lGPO.read(++rec);
            if (lGPO.scan() == ' ') {
                if (sel > 2 && line > slp[code].end - 2) {
                    oplLine.onto(12, editInt(++page));
                    prtLine.init(' ').type(sel);
                    prtLine.init(oplLine.toString()).type(sel);
                    prtLine.init(trailer.substring(1)).type(sel);
                    DevIo.slpRemove();
                    oplLine.show(2);
                    DevIo.slpInsert(line = slp[code].top - 1);
                    prtLine.init(oplLine.toString()).type(sel);
                    line += 2;
                    prtLine.init(' ').type(sel);
                }
            } else
                trailer = null;
            line++;
            prtLine.init(lGPO.pb.substring(1)).type(sel);
        }
        if (lGPO.pb.charAt(0) != '*')
            if (DevIo.mfptr.state >= 0) {
                lGPO.onto(0, '*');
                lGPO.rewrite(rec, 0);
                lGPO.sync();
            }

        String optionData = ECommerceManager.getInstance().addPrinterInfo();
        if (optionData != null) {
            prtLine.init(' ').type(sel);
            prtLine.init(optionData).type(sel);
        }

        if (sel > 2)
            DevIo.slpRemove();
        else if (cpy > 0)
            GdRegis.hdr_print();
        return 0;
    }

    public static void printAdditionalHeader(Set<Integer> special) {
        for (int type : special) {
            prtBlock(2, specialHeader[type], 0, 10);
        }
    }

    public static void print_tnd(int sel, int lfs) {
        if (sel > 2)
            DevIo.slpInsert(--lfs);
        else
            DevIo.tpmPrint(sel, --lfs, "");
        prtLine.init(iht_line).type(sel);
        for (int rec = tra.gpo; lGPO.read(++rec) > 0; ) {
            prtLine.init(lGPO.pb.substring(1)).type(sel);
        }
        if (sel > 2)
            DevIo.slpRemove();
    }

    public static void tender_cpy() {
        if (tra.tslp > 0) {
            print_tnd(4, tra.tslp);
            slpStatus(2, tra.tslp);
        }
    }

    public static void tender_bof() {
        int dev = (options[O_copy2] & 4) > 0 ? 4 : 2;

        if (!DevIo.station(dev))
            return;
        for (int ind = 0; ++ind < tnd.length; ) {
            if ((tnd[ind].flag & T_ONSLIP) > 0)
                continue;
            if (tnd[ind].getType() != 'E' && tnd[ind].getType() != 'F')
                continue;
            if (reg.findTnd(ind, 1) < 1)
                continue;
            prtForm(dev, "FORM_T" + editNum(ind, 2));
        }
    }
}