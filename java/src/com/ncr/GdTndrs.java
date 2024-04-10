package com.ncr;

import com.ncr.common.data.special.SpecialItem;
import com.ncr.common.utilities.AutoSaleManager;
import com.ncr.ecommerce.ECommerce;
import com.ncr.ecommerce.ECommerceManager;
import com.ncr.eft.EftPluginManager;
import com.ncr.eft.MarshallEftPlugin;
import com.ncr.gpe.PosGPE;
import com.ncr.gui.BcrDlg;
import com.ncr.gui.ModDlg;
import com.ncr.loyalty.LoyaltyService;
import com.ncr.loyalty.LoyaltyServiceInterface;
import com.ncr.common.data.special.RedemptionItem;
import com.ncr.loyalty.aym.data.Variable;
import com.ncr.giftcard.psh.data.CustomTender;
import com.ncr.ssco.communication.entities.DataNeeded;
import com.ncr.ssco.communication.entities.pos.SscoTender;
import com.ncr.ssco.communication.manager.SscoPosManager;
import org.apache.log4j.Logger;

import static com.ncr.GiftCardPluginManager.GIFTCARD_TENDER_TYPES;

public class GdTndrs extends Action {
    private static final Logger logger = Logger.getLogger(GdTndrs.class);
    private static long amtByToneTag = 0L;// TONETAG-CGA#A


    static void tnd_symbol() {
        dspBmap = "TND_" + editNum(itm.tnd, 4);
        panel.dspSymbol(tnd[itm.tnd].symbol);
    }

    static int tnd_clear(int txt) {
        //AMAZON-COMM-CGA#A BEG
        logger.info("tender clear");
        if (ECommerce.isAlreadyAmzCommCalc() && input.key == input.CLEAR) {
            Itemdata itmOld = itm;
            int ris = ECommerce.automaticVoidItem();

            logger.info("ris: " + ris);
            if (ris > 0) {
                panel.clearLink(Mnemo.getInfo(ris), 1);
            }
            tnd_prompt();
            itm = itmOld;
        }
        //AMAZON-COMM-CGA#A END
        if (tra.bal == 0) {
            event.nxt = event.alt;
            dspLine.init(Mnemo.getMenu(tra.head = 50));
        } else if (txt > 0)
            showTotal(txt);
        GdTrans.itm_clear();
        tnd_symbol();
        event.dpos = tnd[itm.tnd = 1].dec;
        return 0;
    }

    static long tnd_limit(int rec, int rate) {
        TndMedia ptr = tnd[itm.tnd];
        long lim;

        if (rec > 0) {
            Sales sls = dpt.netSales(rec);
            rec = reg.findTnd(itm.tnd, 1);
            lim = roundBy(sls.total * rate, 1000);
            lim = ptr.hc2fc(lim) - reg.sales[rec - 1][0].total;
        } else {
            lim = ptr.hc2fc(tra.bal);
            // TSC-MOD2014-AMZ#BEG
            if (GdTsc.isRoundingEnabled()) {
                lim = ptr.hc2fc(GdTsc.roundDown(tra.bal, tnd[0].coin));
            }
            // TSC-MOD2014-AMZ#END
        }
        lim = roundBy(lim * 10000 / (1000 + ptr.rate), 10);
        lim = roundBy(lim * 10000 / (1000 - ptr.xtra), 10);
        return lim;
    }

    static void tnd_prompt() {
        long total = tnd_limit(0, 1000);

        event.dpos = tnd[itm.tnd].dec;
        oplLine.init(input.prompt = Mnemo.getText(36));
        if (total != 0)
            oplLine.upto(20, editMoney(itm.tnd, total));
    }

    static int tnd_range(String key) {
        int ind = 0, rec = 0;
        key = editTxt(key, 19);

        lCRD.open(null, "S_PLUCRD.DAT", 0);
        if (lCRD.file == null)
            return itm.tnd;

        while (lCRD.read(++rec) > 0) {
            try {
                if (key.compareTo(lCRD.scan(19)) < 0)
                    continue;
                if (key.compareTo(lCRD.scan(':').scan(19)) > 0)
                    continue;
                ind = lCRD.scan(':').scanNum(2);
            } catch (NumberFormatException e) {
                lCRD.error(e, false);
            }
        }
        lCRD.close();
        return ind;
    }

    static int tnd_hotval(String nbr) {
        int sts = netio.hotval(nbr);
        if (sts < 0) {
            itm.flag |= 0x100;
            return 0; /* offline */
        }
        if (--sts < 0)
            return 0; /* not found */
        stsLine.init(Mnemo.getText(13)).upto(20, editInt(sts)).show(2);
        return GdSigns.chk_autho(Mnemo.getInfo(40));
    }

    static void tnd_drawer() {
        int ind, rec, till = 0;

        while (till++ < 2)
            for (ind = tnd.length; --ind > 0; ) {
                if ((tnd[ind].till & till) == 0)
                    continue;
                if ((rec = reg.findTnd(ind, 1)) < 1)
                    continue;
                if (!reg.sales[rec - 1][0].isZero()) {
                    DevIo.drwPulse(till);
                    break;
                }
            }
    }

    static int tnd_ec_card() {
        int sts = tnd_hotval(ecn.bank + ecn.acct);

        if (sts > 0)
            return sts;
        itm.flag |= T_BNKREF;
        itm.stat = ecn.seqno;
        if (tnd[itm.tnd].getType() == 'B')
            event.nxt = event.alt;
        else
            tnd_prompt();
        return 0;
    }

    static void tnd_ec_bof() {
        int ind = 0;

        lBOF.open(null, "FORM_T" + editNum(itm.tnd, 2) + ".TMP", 2);
        lREG.read(reg.findTnd(itm.tnd, 1), lREG.LOCAL);
        int nbr = (lREG.tflg & 0x20) > 0 ? 2 : 1;
        lBOF.init("$copies=" + nbr).skip(40);
        lBOF.write();
        for (; ind < 10; ind++)
            if (bank_txt[ind] != null) {
                lBOF.init(bank_txt[ind]).skip(40);
                lBOF.write();
            }
        lBOF.init(Mnemo.getText(33)).upto(20, ecn.acct).skip(20);
        lBOF.write();
        lBOF.init(Mnemo.getText(34)).upto(20, ecn.bank).skip(20);
        lBOF.write();
        if (ecn.seqno > 0) {
            lBOF.init(Mnemo.getText(31)).upto(20, editInt(ecn.seqno)).skip(20);
            lBOF.write();
        }
        lBOF.init(Mnemo.getText(30)).upto(20, itm.number).skip(20);
        lBOF.write();
        lBOF.init('>' + tnd[itm.tnd].symbol).upto(21, editMoney(itm.tnd, itm.pos)).skip(19);
        lBOF.write();
        for (; ind < 16; ind++)
            if (bank_txt[ind] != null) {
                lBOF.init(bank_txt[ind]).skip(40);
                lBOF.write();
            }
        GdRegis.set_trailer();
        lBOF.onto(0, prtLine.toString(0, 40));
        lBOF.write();
        lBOF.close();
    }

    public static void tnd_wridc(char id, int code, int ctrl, int items, long total) {
        int subc = 0;
        String nbr = itm.number;

        if (code == 1) {
            if (ctrl > 0)
                code = 6;
            if (Itmdc.chk_tender(itm))
                subc = 4;
        }
        if (id == 'T')
            itm.dpt = ctl.ckr_nbr;
        if (tnd[itm.tnd].unit > 0) {
            Itmdc.IDC_write(id, code, subc | 1, itm.number, items, total);
            int ind = tnd[itm.tnd].club;
            nbr = tnd[itm.tnd].editXrate(false);
            if (ind > 0)
                if (tnd[ind].unit > 0)
                    nbr += editTxt(tnd[ind].editXrate(false), 8);
            subc |= 2;
            total = tnd[itm.tnd].fc2hc(total);
        }

        //AMAZON-COMM-CGA#A BEG
        logger.info("itm.tnd: " + itm.tnd);
        //if (id == 'T' && nbr.length() > 16) {
        if (id == 'T' && (nbr.length() > 16 || !ECommerce.getInstashopChoice().equals("0"))) {
            Itmdc.IDC_write(id, code, subc, "", items, total);
            if (tra.bal == tra.amt || !ECommerce.isPartialInstaPayment()) {  //INSTASHOP-MULTITENDER 
                Itmdc.IDC_write('t', code, subc, nbr, items, total);
            }
            char idQuery = 'A';
            int codeQuery = 1;

            try {
                if (!ECommerce.getInstashopChoice().equals("0")) {  //INSTASHOP-SELL-CGA#A
                    Itmdc.IDC_write('d', 0, 0, ECommerce.getInstashopChoice(), 0, 0);

                    //INSTASHOP-RECORD-CGA#A BEG
                    String ddqCode = ECommerce.getDdqCodeMap().get(ECommerce.getTndInstashop());
                    idQuery = ddqCode.charAt(0);
                    codeQuery = Integer.parseInt(ddqCode.charAt(1) + "");
                    //INSTASHOP-RECORD-CGA#A END
                }
            } catch (Exception e) {
                logger.error("Error: ", e);
            }

            //INSTASHOP-RECORD-CGA#A BEG
            lDDQ.recno = 1;
            if (Match.dd_query(idQuery, codeQuery) >= 0) {
                Itmdc.IDC_write('Q', itm.spf1, 0, itm.number, itm.cnt, 1);
            }
            //INSTASHOP-RECORD-CGA#A END
        } else {  //AMAZON-COMM-CGA#A END
            //INSTASHOP-FINALIZE-CGA#A BEG
            if (ECommerce.isResumeInstashop() && ECommerce.getCardTypeTnd() != 0) {
                int oldTnd = itm.tnd;
                itm.tnd = ECommerce.getCardTypeTnd();
            }
            //INSTASHOP-FINALIZE-CGA#A END
            Itmdc.IDC_write(id, code, subc, nbr, items, total);
        }

        //WINEPTS-CGA#A BEG
        logger.info("tnd[itm.tnd].type: " + tnd[itm.tnd].getType());
        logger.info("id: " + id);
        logger.info("tra.code: " + tra.code);
        if ((tnd[itm.tnd].getType() == 'P') && id == 'T') {
            if (tra.code == 0) {
                nbr = PosGPE.getPartialCardNumber(PosGPE.getLastEptsReceiptData().getCardNumber(), 16);
            }
        }

        //Itmdc.IDC_write(id, code, subc, nbr, total);
        if ((tra.code != 9) && (tra.code != 11)) {
            PosGPE.getInstance().GPEDataCollect(itm.tnd);
        }
        //WINEPTS-CGA#A END
        if (itm.gCardPayment) // PSH-ENH-001-AMZ#ADD -- idc record g
            Itmdc.IDC_write('g', 0, 2, itm.giftCardSerial, 0, 0); // PSH-ENH-001-AMZ#ADD -- idc record g
    }

    static void tnd_line() {
        if ((tnd[itm.tnd].ctrl & 4) > 0)
            accumTnd(6, itm.cnt, itm.pos);
        accumTnd(1, itm.cnt, itm.pos);
        if (itm.pov != 0) /* cash back */ {
            accumTnd(11, itm.cnt, itm.pov);
            tra.csh_bck += tnd[itm.tnd].fc2hc(itm.pov);
            itm.pos -= itm.pov;
            itm.amt = tnd[itm.tnd].fc2hc(itm.pos);
        }
        if (itm.amt != 0) {
            itm.text = itm.cnt == 0 ? Mnemo.getText(27) : tnd[itm.tnd].tx20;
            TView.append('T', 0x00, itm.text, itm.number, "", editMoney(0, itm.amt), "");
        }
        if (tra.bal == itm.amt)
            return;
        if (tnd[1].change(tnd[1].hc2fc(tra.bal - itm.amt), tra.bal < 0) != 0) {
            if (tnd[itm.tnd].unit == 0)
                return;
            if ((tnd[itm.tnd].xflg & 0x40) > 0)
                return;
            if (itm.pos != tnd[itm.tnd].hc2fc(tra.bal))
                return;
        }
        int rec, sc = sc_value(M_RNDEXC);
        itm.dsc = tra.bal - itm.amt;
        tra.bal -= itm.dsc;
        if ((rec = reg.find(4, sc)) > 0) {
            lREG.read(rec, lREG.LOCAL);
            itm.cnt = signOf(itm.amt);
            itm.text = lREG.text;
            itm.dpt_nbr = keyValue(String.valueOf(itm.tnd));
            accumReg(4, sc, itm.cnt, -itm.dsc);
            Itmdc.IDC_write('D', sc, 0, "", itm.cnt, -itm.dsc);
            TView.append('D', 0x81, itm.text, "", "", editMoney(0, itm.dsc), "");
            prtLine.init(itm.text).onto(20, tnd[0].symbol).upto(40, editMoney(0, itm.dsc)).book(3);
            LoyaltyServiceInterface loyaltyService = LoyaltyService.getService();
            loyaltyService.getTransactionVariables().add(new Variable(LoyaltyService.ROUNDING_VARIABLE, editDec(itm.dsc, tnd[0].dec).trim()));
        }
    }

    static void crd_line(int sc) {
        int rec = reg.findTnd(itm.tnd, 10 + sc);
        long amt = sc == 5 ? itm.dsc : itm.crd;

        if (rec < 1 || amt == 0)
            return;
        Itemdata sav = itm;
        itm = new Itemdata();
        itm.tnd = sav.tnd;
        itm.cnt = signOf(sav.amt);
        accumReg(4, sc, itm.cnt, itm.dsc = 0 - amt);
        accumReg(10 + itm.tnd, 10 + sc, itm.cnt, itm.dsc);
        accumReg(10, 10 + sc, itm.cnt, tnd[itm.tnd].hc2fc(itm.dsc));
        lREG.read(rec, lREG.LOCAL);
        itm.text = lREG.text;
        itm.number = editRate(lREG.rate);
        itm.dpt_nbr = keyValue(String.valueOf(itm.tnd));
        Itmdc.IDC_write('D', sc, 0, itm.number, itm.cnt, amt);
        TView.append('D', 0x81, itm.text, "", itm.number, editMoney(0, itm.dsc), "");
        prtLine.init(itm.text).upto(25, itm.number).upto(40, editMoney(0, itm.dsc)).book(3);
        if (options[O_CandC] == 0) {
            itm.dec = (int) roundBy(amt * 10000 / tra.bal, 10);
            for (int ind = 0; ind < vat.length; ind++) {
                if ((rec = reg.find(7, 11 + ind)) < 1)
                    continue;
                Sales sls = reg.sales[rec - 1][0];
                if (sls.isZero())
                    continue;
                itm.amt = roundBy(itm.dec * sls.total, 1000);
                accumTax(10, ind, 0, itm.amt);
                amt -= itm.amt;
                itm.tnd = ind;
            }
            accumTax(10, itm.tnd, 0, amt);
        }
        tra.bal -= itm.dsc;
        itm = sav;
    }

    //WINEPTS-CGA#A BEG
    private static int GPEPayment() {
        logger.info("ENTER GPEPayment");

        long tenderAmount = itm.amt;
        int rtc = 0;

        logger.info("tra.spf1: " + tra.spf1);
        if (tra.spf1 != M_TRRTRN) {
            rtc = PosGPE.payGpe(tenderAmount);
        } else {
            rtc = PosGPE.refund(tenderAmount);  //transaction return handle
        }

        logger.info("rtc: " + rtc);
        if (rtc == -2) {
            rtc = PosGPE.continueDiscountedPayment(tenderAmount);
            logger.info("tra.bal: " + tra.bal);
            logger.info("tenderAmount: " + tenderAmount);
            if (tra.bal <= 0 && tenderAmount <= 0) {
                input.msk = 0;
                input.reset("000");

                logger.info("tra.tnd: " + tra.tnd);
                if (tra.tnd == 0) {
                    GdTrans.tra_total();
                }
                GdTrans.tra_finish();
                return -2;
            }
        }

        logger.info("rtc: " + rtc);
        if (rtc != 0) {
            PosGPE.getInstance().GPEDataCollect(7);
            logger.info("EXIT GPEPayment 1 - return " + rtc);

            return rtc;
        } else {
            int tnd = PosGPE.getLastEptsReceiptData().getPosTenderId().intValue();
            long pos = PosGPE.getLastEptsReceiptData().getAuthorizedAmount().longValue();

            logger.info("tnd = " + tnd);
            logger.info("pos = " + pos);
            logger.info("rtc = " + rtc);
            if (rtc == 0) {
                rtc = PosGPE.finalizeTransaction();
            }
        }
        if (rtc == 0) {
            itm.tnd = PosGPE.getLastEptsReceiptData().getPosTenderId().intValue();

            itm.pos = PosGPE.getLastEptsReceiptData().getAuthorizedAmount().longValue();

            if (tra.spf1 == M_TRRTRN) {
                itm.pos = -itm.pos;
            }
            itm.amt = tnd[itm.tnd].fc2hc(itm.pos);
            input.reset(String.valueOf(itm.pos));
            PosGPE.writeEptsVoidFlag();
        } else {
            PosGPE.getInstance().GPEDataCollect(7);
            WinEPTSVoucherManager.removeCreditCardVoucher();
        }

        logger.info("EXIT GPEPayment 2 - return " + rtc);
        return rtc;
    }
    //WINEPTS-CGA#A END

    static long donate(int sc) {
        int rec = reg.find(5, sc);

        if (rec < 1)
            return 0;
        lREG.read(rec, lREG.LOCAL);
        int sts = lREG.rate % 10, lim = lREG.rate / 10;
        if (lim < 1)
            return 0;
        while (sts-- > 0)
            lim *= 10;
        long amt = Math.abs(tnd[itm.tnd].hc2fc(tra.bal)) % lim;
        if (amt < 1)
            return amt;
        tnd_symbol();
        if (DonationManager.getInstance().isEnabled()) {
            if (amt > DonationManager.getInstance().getMaxDonationAmount()) {
                amt = DonationManager.getInstance().getMaxDonationAmount();
            }
            int response = 0;
            if (SscoPosManager.getInstance().isUsed()) {
                DataNeeded dataNeeded = SscoPosManager.getInstance().sendDataNeeded("OkCancelDonation");
                response = SscoPosManager.getInstance().waitForDataneededReply(dataNeeded.getTimeout());
            } else {
                response = gui.clearLink(Mnemo.getMenu(115) + editMoney(0, amt), 3);
            }
            if (response  == SscoPosManager.OK_CANCEL_CLEAR) return 0;
            itm.round = tra.bal - itm.pos;
            if (amt == DonationManager.getInstance().getMaxDonationAmount()) {
                amt -= itm.round;
            }
            LoyaltyServiceInterface loyaltyService = LoyaltyService.getService();
            loyaltyService.getTransactionVariables().add(new Variable(LoyaltyService.DONATION_VARIABLE, editDec(-amt, tnd[0].dec).trim()));
            SscoPosManager.getInstance().setDonation((int) amt);
            if (amt == 0) return 0;
        } else {
            dspLine.init(Mnemo.getText(27)).upto(20, editMoney(itm.tnd, -itm.pos)).show(1);
            cusLine.init(Mnemo.getText(27)).show(10);
            cusLine.init(tnd[itm.tnd].symbol).upto(20, editMoney(itm.tnd, -itm.pos)).show(11);
            for (input.prompt = lREG.text; ; ) {
                ModDlg dlg = new ModDlg(Mnemo.getInfo(40));
                oplLine.init(input.prompt).upto(20, editDec(amt, tnd[itm.tnd].dec)).show(2);
                input.init(0x80, 8, 8, tnd[itm.tnd].dec);
                DevIo.oplSignal(15, 1);
                oplToggle(2, Mnemo.getInfo(40));
                dlg.show("DON");
                oplToggle(0, null);
                DevIo.oplSignal(15, 0);
                if (input.key == 0)
                    continue;
                if (input.key == ConIo.CLEAR)
                    continue;
                if ((sts = dlg.code) == 0) {
                    if (input.key != ConIo.ENTER)
                        sts = 5;
                    else
                        sts = input.adjust(input.pnt);
                    if (sts == 0) {
                        if (input.num < 1)
                            break;
                        if ((lim = input.scanNum(input.num)) < 1)
                            return 0;
                        if (lim != tnd[itm.tnd].change(lim))
                            sts = 8;
                        else if (lim > Math.abs(itm.pos))
                            sts = 46;
                        else
                            amt = lim;
                    }
                }
                if (sts == 0)
                    break;
                panel.clearLink(Mnemo.getInfo(sts), 1);
            }
        }
        if (itm.pos > 0)
            amt = -amt;
        Itemdata sav = itm;
        itm = new Itemdata();
        itm.tnd = sav.tnd;
        itm.text = lREG.text;
        itm.cnt = signOf(itm.pos = amt);
        itm.amt = tnd[itm.tnd].fc2hc(itm.pos);
        accumReg(itm.stat = 5, sc, itm.cnt, itm.amt);
        Itmdc.IDC_write('M', sc_value(tra.spf1), sc, "", itm.cnt, itm.amt);
        prtDline("Roa" + editNum(sc, 4));
        prtLine.init(itm.text).onto(20, tnd[itm.tnd].symbol).upto(40, editMoney(itm.tnd, -itm.pos)).book(3);
        TView.append('$', 0x40, itm.text, "", "", editMoney(itm.tnd, -itm.pos), "");
        tra.bal += itm.amt;
        itm = sav;
        return amt;
    }

    static int tnd_print() {

        int ind = itm.tnd, sts;

        // TSC-MOD2014-AMZ#BEG
        String tmp_number = itm.number;

        CreditNote.init(itm.amt);
        if (itm.tnd == CreditNote.getOption(CreditNote.CREDITNOTEPRINT)
                && !CreditNote.isPrinted()) {
            if ((sts = CreditNote.printText()) > 0) {
                event.nxt = event.alt;
                return tnd_clear(26);
            }
        } else {
            if (itm.tnd == CreditNote.getOption(CreditNote.CREDITNOTEUSE)) {
                if (tra.bal - itm.amt < 0) {
                    if (reg.find(10 + CreditNote.getOption(CreditNote.CREDITNOTEPRINT), 1) > 0) {
                        if (CreditNote.getOption(CreditNote.RETURN) > 0
                                && panel.clearLink(Mnemo.getInfo(141), 3) > 1) {
                            CreditNote.setAmount(itm.amt - tra.bal);
                            if ((sts = CreditNote.printText()) > 0) {
                                event.nxt = event.alt;
                                return tnd_clear(26);
                            }
                            CreditNote.setPrinted(true);
                        } else {
                            CreditNote.setRejected(true);
                        }
                    } else {
                        CreditNote.setRejected(true);
                    }
                }
            }
        }
        // TSC-MOD2014-AMZ#END
        if (tnd[ind].getType() == 'E') {
            if (itm.mark == ' ')
                tnd_ec_bof();
        }
        if (tnd[ind].getType() == 'F') /* cashback in itm.pov */ {
            if ((sts = EftIo.eftOrder(itm.pos)) > 0)
                return sts;
            itm.pos += itm.pov;
            itm.amt = tnd[ind].fc2hc(itm.pos);
        }
        if (tnd[ind].getType() == 'H') {
            if ((sts = BcrIo.bcrOrder(itm.pos)) > 0)
                return sts;
            itm.pos += itm.pov;
            itm.amt = tnd[ind].fc2hc(itm.pos);
            mon.money = -1;
        }
        if (tra.bal == 0) {
            if (ECommerce.isResumeInstashop() && ECommerce.getCurrentIndexTnd() > 0) {    //INSTASHOP-MULTITENDER
                ;
            } else {
                tra.tnd = 1;
                GdRegis.set_tra_top();
                prtTitle(tra.head);
                accumReg(8, 5, 1, -itm.amt);
            }

        }
        if (tra.tnd == 0) AutoSaleManager.getInstance().sellItem(itm);
        pit = itm;
        if (tra.tnd == 0) {
            GdTrans.tra_total();
        }
        if (tra.tnd == 2)
            GdTrans.tra_taxes();
        itm = pit;

        if (ECommerce.isResumeInstashop() && itm.pos < 0) {
            Itmdc.IDC_write('t', 0, ECommerce.getNumberTraResume(), ECommerce.getAccountInstashop(), 0, 0);
        }
        // TSC-MOD2014-AMZ#BEG
        if (itm.tnd == CreditNote.getOption(CreditNote.CREDITNOTEPRINT)) {
            itm.number = CreditNote.getNumber();
        }
        // TSC-MOD2014-AMZ#END

        tnd_wridc('T', tra.tnd, tnd[ind].ctrl, itm.cnt, itm.pos);
        tra.vItems.addElement('T', itm);
        prtDline("Tnd" + editNum(ind, 4));
        //INSTASHOP-FINALIZE-CGA#A END
        if (ECommerce.isResumeInstashop() && !ECommerce.getCardTypeDesc().isEmpty()) {
            prtLine.init(ECommerce.getCardTypeDesc());
        } else {//INSTASHOP-FINALIZE-CGA#A END
            prtLine.init(tnd[ind].tx20);
        }
        if (tnd[ind].unit > 0) {
            if ((tnd[ind].xflg & 0x20) > 0)
                prtLine.onto(20, tnd[ind].editXrate(true));
            prtLine.book(3);
            prtLine.init(tnd[ind].symbol).upto(17, editMoney(ind, itm.pos)).onto(20, tnd[0].symbol).upto(40,
                    editMoney(0, itm.amt));
        } else
            prtLine.onto(20, tnd[ind].symbol).upto(40, editMoney(ind, itm.amt));

        if (itm.mark == 'I') itm.mark = ' ';
        prtLine.push(itm.mark).book(3);

        //ECOMMERCE-SUSPEND-CGA#A BEG
        if (!ECommerceManager.getInstance().getCardNum().isEmpty()) {
            Itmdc.IDC_write('N', tra.tnd, 1, ECommerceManager.getInstance().getCardNum(), itm.cnt, itm.pos);
            prtLine.init(Mnemo.getText(31)).onto(20, ECommerceManager.getInstance().getCardNum()).book(3);
        }
        if (!ECommerceManager.getInstance().getAuthCode().isEmpty()) {
            prtLine.init(Mnemo.getText(80)).onto(20, ECommerceManager.getInstance().getAuthCode()).book(3);
        }
        //ECOMMERCE-SUSPEND-CGA#A END

        logger.info("tnd[itm.tnd].eftPlugin >" + tnd[itm.tnd].getType() + "<");
        eftPluginManager.writeInfo(tnd[itm.tnd].getType() + "", tra, itm);

        if (tnd[ind].getType() == 'F') {
            EftIo.eftDetails(); /* write IDC N-record sc 2 */
            if (itm.pov != 0) {
                prtLine.init(Mnemo.getText(66)).onto(20, tnd[ind].symbol + editMoney(ind, itm.pov)).book(3);
                Itmdc.IDC_write('N', tra.tnd, 3, "", itm.cnt, itm.pov);
            }
        }

        //INSTASHOP-SELL-CGA#A BEG
        logger.info("ECommerce.getInstashopChoiceType(): " + ECommerce.getInstashopChoiceType());
        //INSTASHOP-MULTITENDER BEG
        String choiceType = ECommerce.getInstashopChoiceType().trim();
        if (choiceType.equals("SC") || choiceType.equals("FC")) {
            logger.info("print false card value");

            String cardNum = "****";
            String authCode = "****";

            if (ECommerce.getCardNumberMap().get(choiceType + "." + ECommerce.getTndInstashop()) != null) {
                cardNum = ECommerce.getCardNumberMap().get(choiceType + "." + ECommerce.getTndInstashop());
            }
            if (ECommerce.getAuthCodeMap().get(choiceType + "." + ECommerce.getTndInstashop()) != null) {
                authCode = ECommerce.getAuthCodeMap().get(choiceType + "." + ECommerce.getTndInstashop());
            }
            if (ECommerce.isPrintCardNumber()) {
                prtLine.init(Mnemo.getText(31)).onto(20, cardNum).book(3);
            }
            if (ECommerce.isPrintAuthCode()) {
                prtLine.init(Mnemo.getText(80)).onto(20, authCode).book(3);
            }
        }
        //INSTASHOP-MULTITENDER END
        //INSTASHOP-SELL-CGA#A END

        if (tnd[ind].getType() == 'P'
                && tra.amt >= PosGPE.getMinAmountToPrint()
                && !PosGPE.getLastEptsReceiptData().getSignatureBitmap().isEmpty()) {
            logger.info("show message 133");
            panel.clearLink(Mnemo.getInfo(133), 1);
        }
        if (itm.tnd == CreditNote.getOption(CreditNote.CREDITNOTEPRINT)) {
            itm.number = tmp_number;
        }
        if (!itm.number.isEmpty()) {
            if (!ECommerce.getInstashopChoice().equals("0") && (itm.text.startsWith("ACCOUNT"))) {
                if (itm.text.startsWith("ACCOUNT") && !ECommerce.getAccount().trim().isEmpty()) {
                    prtLine.init(itm.text).onto(20, ECommerce.getAccount()).book(3);
                }
            } else if (!ECommerce.getTransactionResumed().isEmpty() && itm.text.startsWith("RECEIPT#")) {
                prtLine.init(itm.text).onto(20, ECommerce.getTransactionResumed()).book(3);
            } else {  //INSTASHOP-SELL-CGA#A END
                if (tra.eCommerce == 0) {    //ECOMMERCE-SSAM#A  //PORTING-SPINNEYS-ECOMMERCE-CGA#A
                    if (tra.bal == tra.amt || !ECommerce.isPartialInstaPayment()) {   //INSTASHOP-MULTITENDER 
                        logger.info("Print Account number in the receipt and journal");
                        if (itm.giftCardSerial.isEmpty()) {
                            prtLine.init(itm.text).onto(20, itm.number).book(3);
                        } else {
                            prtLine.init(Mnemo.getText(76)).onto(20, GiftCardPluginManager.getInstance().mask(itm.giftCardSerial)).book(3);
                        }
                    }
                }
            }
        } else {
            //INSTASHOP-SELL-CGA#A BEG
            if (ECommerce.isResumeInstashop() && itm.tnd == 1 && tra.amt == 0 && tra.bal != 0) {
                prtLine.init(Mnemo.getText(30)).onto(20, ECommerce.getTransactionResumed()).book(3);
            }
            //INSTASHOP-SELL-CGA#A END
        }

        //INSTASHOP-FINALIZE-CGA#A BEG
        if (ECommerce.isResumeInstashop() && tra.amt == 0 && tra.bal != 0) {
            if (!ECommerce.getAccount().trim().isEmpty()) {
                prtLine.init(Mnemo.getText(33)).onto(20, ECommerce.getAccount()).book(3);
            }
        }
        //INSTASHOP-FINALIZE-CGA#A END
        // PSH-ENH-002-AMZ#BEG -- payment aux line
        if (!itm.gCardDsc.isEmpty()) {
            prtLine.init(itm.gCardDsc).book(3);
            prtLine.init(Mnemo.getText(91)).onto(20, itm.gCardBal).book(3);
        }
        if (!itm.utilityEnglishText.isEmpty()) {
            GdPsh.getInstance().printText(itm.utilityEnglishText);
        }
        // PSH-ENH-002-AMZ#END -- payment aux line

        if ((itm.flag & T_BNKREF) > 0)
            if (!itm.serial.isEmpty()) {
                int opt = options[O_CardX] >> 4;
                String nbr = opt > 0 ? leftMask(itm.serial, opt, '*') : itm.serial;
                Itmdc.IDC_write('N', tra.tnd, 1, nbr, 0, itm.pos);
                opt = options[O_CardX] & 15;
                nbr = opt > 0 ? leftMask(itm.serial, opt, '*') : itm.serial;

                prtLine.init(Mnemo.getText(31)).onto(20, nbr).book(3);
                if (ECommerce.isResumeInstashop()) {
                    prtLine.init(Mnemo.getText(80)).onto(20, itm.authNum).book(3);
                }
            } else {
                String nbrs = ecn.cheque + ':' + editTxt(ecn.bank, 8) + editTxt(ecn.acct, 10);
                Itmdc.IDC_write('N', tra.tnd, 0, nbrs, 0, itm.pos);
                if (!ecn.acct.trim().isEmpty()) {
                    prtLine.init(Mnemo.getText(33)).onto(20, ecn.acct).book(3);
                }
                prtLine.init(Mnemo.getText(34)).onto(20, ecn.bank).book(3);
                if (!ecn.cheque.isEmpty()) {
                    prtLine.init(Mnemo.getText(32)).onto(20, ecn.cheque).push(" [" + editNum(itm.stat, 2) + "]")
                            .book(3);
                } else if (ecn.seqno > 0)
                    prtLine.init(Mnemo.getText(31)).onto(20, editNum(ecn.seqno, 2)).book(3);
            }


        //SAFP-20170224-CGA#A BEG
        if (!ecn.custom.isEmpty()) {
            prtLine.init(Mnemo.getText(80)).onto(20, ecn.custom).book(3);
            Itmdc.IDC_write('t', tra.tnd, 0, ecn.custom, 0, itm.pos);
        }
        //SAFP-20170224-CGA#A END
        // TSC-MOD2014-AMZ#BEG
        if (tnd[ind].toSlip) {
            GdTsc.tnd_slip();
        }
        // TSC-MOD2014-AMZ#END

        if ((itm.flag & T_ONSLIP) > 0) {
            String chk_nbr = itm.number;
            if (tnd[ind].getType() == 'B') {
                stsLine.init(Mnemo.getText(49)).upto(20, itm.number);
                chk_nbr = stsLine.toString();
            }
            if (tnd[ind].getType() == 'E')
                chk_nbr = '#' + ecn.acct + '/' + ecn.bank;
            DevIo.tpmCheque(ind, chk_nbr, itm.pos);
        }
        if (tra.bal != 0)
            for (int sc = 5; sc < 7; crd_line(sc++))
                ;
        tnd_line();
        tra.bal -= itm.amt;

        //INSTASHOP-MULTITENDER BEG
        if (ECommerce.isResumeInstashop()
                && !ECommerce.isFinalizeInstashop()
                && ECommerce.getCurrentIndexTnd() != -1
                && ECommerce.getTotalInstashop() != 0) {

            tra.bal = ECommerce.getTotalInstashop();
            ECommerce.setTotalInstashop(0);
        }
        //INSTASHOP-MULTITENDER END

        if ((ind = reg.find(9, 6)) > 0) {
            accumReg(9, 6, 1, sec_diff(tra.tim) - reg.sales[ind - 1][0].total);
            if ((ind = reg.find(9, 5)) > 0)
                accumReg(9, 6, 0, -reg.sales[ind - 1][0].total);
        }
        if (tra.mode == M_GROSS)
            Promo.payTender();
        itm = new Itemdata();
        itm.tnd = tnd_tbl[K_Change];
        // TSC-MOD2014-AMZ#BEG
        if (pit.tnd == CreditNote.getOption(CreditNote.CREDITNOTEUSE) && !CreditNote.isRejected()) {
            if (tra.bal < 0) {
                itm.tnd = CreditNote.getOption(CreditNote.CREDITNOTEPRINT);
            }
        }
        // TSC-MOD2014-AMZ#END
        PosGPE.deleteLastEptsReceiptData();   //WINEPTS-CGA#A
        if (tra.bal != 0 || tra.csh_bck != 0) {
            if ((tra.bal > 0) ^ ((tra.spf1 & M_TRVOID) > 0) || (pit.amt < 0) ^ ((tra.spf1 & M_TRVOID) > 0)
                    || (tnd[pit.tnd].flag & T_NOAUTO) > 0 || pit.mark != ' ') {
                event.nxt = event.alt;
                return tnd_clear(26);
            }
            tra.bal -= tra.csh_bck;
            itm.pos = tnd[itm.tnd].hc2fc(tra.bal);
            itm.pos = tnd[itm.tnd].change(itm.pos);
            if ((ind = tnd_tbl[K_DonaSc]) > 0)
                itm.pos += donate(ind) + itm.round;
            itm.amt = tnd[itm.tnd].fc2hc(itm.pos);
            if (itm.pos != 0) {
                tnd_wridc('T', tra.tnd, tnd[itm.tnd].ctrl, itm.cnt, itm.pos);
                //tra.vItems.addElement('T', itm);
                prtDline("TndCHGE");
                prtLine.init(Mnemo.getText(27));
                if (tnd[itm.tnd].unit > 0) {
                    if ((tnd[itm.tnd].xflg & 0x20) > 0)
                        prtLine.onto(20, tnd[itm.tnd].editXrate(true));
                    prtLine.book(3);
                    prtLine.init(tnd[itm.tnd].symbol).upto(17, editMoney(itm.tnd, itm.pos));
                }
                prtLine.onto(20, tnd[0].symbol).upto(40, editMoney(0, itm.amt)).book(3);
                LoyaltyServiceInterface loyaltyService = LoyaltyService.getService();
                loyaltyService.getTransactionVariables().add(new Variable(LoyaltyService.CHANGE_VARIABLE, editDec(itm.pos, tnd[0].dec).trim()));

                if (tra.bal - itm.pos != 0) {
                    Itemdata tim = itm;
                    int rec, sc = sc_value(M_RNDEXC);
                    itm.dsc = tra.bal - itm.pos;
                    tra.bal -= itm.dsc;
                    if ((rec = reg.find(4, sc)) > 0) {
                        lREG.read(rec, lREG.LOCAL);
                        itm.cnt = signOf(itm.amt);
                        itm.text = lREG.text;
                        itm.dpt_nbr = keyValue(String.valueOf(itm.tnd));
                        accumReg(4, sc, itm.cnt, -itm.dsc);
                        Itmdc.IDC_write('D', sc, 0, "", itm.cnt, -itm.dsc);
                        TView.append('D', 0x81, itm.text, "", "", editMoney(0, itm.dsc), "");
                        prtLine.init(itm.text).onto(20, tnd[0].symbol).upto(40, editMoney(0, itm.dsc)).book(3);
                        loyaltyService.getTransactionVariables().add(new Variable(LoyaltyService.ROUNDING_VARIABLE, editDec(itm.dsc, tnd[0].dec).trim()));
                        if (SscoPosManager.getInstance().isUsed()) {
                            SscoPosManager.getInstance().updateRoundDiscount(-(int) itm.dsc);
                        }
                    }
                    itm = tim.copy();
                }
            }
            tnd_line();
        }
        if (tra.mode == M_GROSS) {
            //PORTING-SPINNEYS-ECOMMERCE-CGA#A BEG
            //ECOMMERCE-SSAM#A BEG
            if (ECommerceManager.getInstance().isEnabled()) {
                tra.print = true;
            } else {  //ECOMMERCE-SSAM#A END  //PORTING-SPINNEYS-ECOMMERCE-CGA#A END
                if (GdBindawood.getInstance().isEreceiptEnabled()) {
                    tra.print = GdBindawood.getInstance().queryUserForElectronicReceipt();
                } else {
                    tra.print = ReceiptPrintManager.getInstance().ask(Mnemo.getMenu(114), "OkCancelReceiptPrint");
                }
            }
        }
        tnd_symbol();
        tra.bal = 0 - tra.bal;
        if (itm.tnd != 1) {
            dspLine.init(Mnemo.getText(27)).upto(20, editMoney(itm.tnd, -itm.pos));
            cusLine.init(Mnemo.getText(27)).show(10);
            cusLine.init(tnd[itm.tnd].symbol).upto(20, editMoney(itm.tnd, -itm.pos)).show(11);
            if (tra.bal != 0) {
                hdrLine.init(tnd[0].symbol).upto(20, editMoney(0, tra.bal)).show(0);
            }
        } else
            showTotal(27);
        tnd_drawer();
        return GdTrans.tra_finish();
    }

    /**
     * tender clear
     **/
    public int action0(int spec) {
        mon.money = -1;
        return tnd_clear(26);
    }

    /**
     * tender type
     **/
    public int action1(int spec) {
        int ind = event.alt;
        TndMedia ptr = tnd[itm.tnd];

        if (spec > 0)
            return action5(tnd_tbl[spec]);

        dspLine.init(ptr.text);
        tnd_symbol();
        long round = ptr.round(tra.bal);
        if (SscoPosManager.getInstance().isEnabled()) {
            SscoPosManager.getInstance().setTenderRounding((int) (tra.bal - ptr.round(tra.bal)));
            SscoPosManager.getInstance().tenderSelectedResponse();
        }
        if (mon.total >= 0) {
            showTotal(0);
            hdrLine.init(Mnemo.getText(26)).upto(20, editMoney(0, tra.bal)).show(0);
        }
        cusLine.init(Mnemo.getText(26)).show(10);
        cusLine.init(ptr.symbol).upto(20, editMoney(itm.tnd, ptr.hc2fc(tra.bal))).show(11);
        event.read(ind);
        itm.text = Mnemo.getText(event.alt);
        if ((ptr.flag & T_BNKREF) > 0) {
            if (ptr.getType() == 'B' || ptr.getType() == 'E')
                oplLine.init(Mnemo.getText(33)).upto(20, ecn.acct);
            if (ptr.getType() == 'F')
                event.dpos = tnd[tnd_tbl[K_Change]].dec;
        }
        if ((tra.spf1 & M_TRVOID) > 0) {
            if ((ptr.flag & T_VOIDNO) > 0) {
                event.nxt = ind;
                return 0;
            }
        } else if ((ptr.flag & T_ONSLIP) > 0)
            itm.flag = T_ONSLIP;
        if (!GdTsc.isAuthCodeEnabled(ptr.getType()) && (ptr.flag & T_NUMBER) > 0) { // TSC-MOD2014-AMZ#ADD
            if (ptr.getType() == 'B')
                oplLine.init(Mnemo.getText(31)).upto(20, ecn.card);
            event.nxt = ind;
            return 0;
        }
        if (GIFTCARD_TENDER_TYPES.indexOf(ptr.getType()) >= 0 && (ptr.flag & T_BNKREF) > 0) {
            event.nxt = ind;
            return 0;
        }

        if (ptr.getType() == 'G')
            return 0;
        do {
            ind = event.next(ind);
        } while (event.key != ConIo.ENTER);

        logger.info("ptr.type >" + ptr.getType() + "<");
        if ((ptr.flag & T_BNKREF) > 0 && EftPluginManager.EFT_TENDER_IDS.indexOf(ptr.getType()) < 0)
            event.nxt = event.alt;
        else
            tnd_prompt();
        ECommerce.resetAlreadyAmzCommCalc();   //AMAZON-COMM-CGA#A

        return 0;
    }

    /**
     * card/account/receipt number
     **/
    public int action2(int spec) {
        int sts;
        TndMedia ptr = tnd[itm.tnd];

        switch (spec) {
            case 1:
                if (input.num > 0)
                    ecn.card = input.pb;
                else if (ecn.card.length() == 0)
                    return 5;
                itm.number = ecn.card;
                oplLine.init(Mnemo.getText(33)).upto(20, ecn.acct);
                break;
            case 2:
                if (input.num > 0)
                    ecn.acct = leftFill(input.pb, 10, '0');
                else if (ecn.acct.length() == 0)
                    return 5;
                if (ptr.getType() == 'E')
                    if ((sts = forceCard(0x20)) > 0)
                        return sts;
                oplLine.init(Mnemo.getText(34)).upto(20, ecn.bank);
                return ecn.seqno = 0;
            case 3:
                if (input.num > 0)
                    ecn.bank = leftFill(input.pb, 8, '0');
                else if (ecn.bank.length() == 0)
                    return 5;
                return tnd_ec_card();
            case 4:
                ecn.cheque = input.pb;
                return 0;
            case 5:
                itm.stat = input.num > 0 ? input.scanNum(input.num) : 11;
                if (itm.stat != 11)
                    if ((sts = GdSigns.chk_autho(null)) > 0)
                        return sts;
                tnd_prompt();
                return 0;
            case 6:
                if ((sts = forceCard(0x80)) > 0)
                    return sts;
                plu = new Itemdata();
                GdPrice.scan_ean();
                sts = GdPrice.src_plu(" C" + plu.number.substring(2));
                if (sts > 0)
                    return sts;
                if (plu.mmt > 0)
                    if (plu.mmt != itm.tnd)
                        return 8;
                if ((sts = GdPrice.chk_pluspc()) > 0)
                    return sts;
                plu.amt = plu.price;
                if (plu.qty > 0)
                    plu.amt *= plu.qty;
                if (plu.amt < 1)
                    return 8;
                if (plu.amt >= 1000000000)
                    return 34;
                plu.pos = ptr.hc2fc(plu.amt);
                if (plu.unit > 0) {
                    if (plu.dpt < 1)
                        return 28;
                    if (plu.pos > Math.abs(tnd_limit(plu.dpt, plu.unit)))
                        return 8;
                }
                itm.number = plu.eanupc.trim();
                itm.text = plu.text;
                input.pnt = ptr.dec;
                input.prompt = ptr.symbol;
                input.reset(Long.toString(plu.pos));
                return action3(0);
            case 7:
                itm.giftCardSerial = input.pb;
                break;
            case 9:
                itm.authNum = input.pb; //INSTASHOP-FINALIZE-CGA#A
                Itmdc.IDC_write('z', Struc.tra.tnd, 0, input.pb, 1, 0);

                //INSTASHOP-FINALIZE-CGA#A BEG
                if (ECommerce.isResumeInstashop()) {
                    ECommerce.chooseCardType();
                }
                //INSTASHOP-FINALIZE-CGA#A END

			/*sts = input.scanNum(input.num - 2);
			if (sts < 1 || sts > 12)
				return 8;
			ecn.yymm = sts + input.scanNum(2) * 100;
			if (cmpDates(ecn.yymm * 100 + 31, ctl.date) < 0)
				return 47;*/  //forzatura
                tnd_prompt();
                return 0;
            case 18:
                if (ptr.getType() == 'E')
                    if (ecn.nation != ecn.home)
                        return 31;
                if (ecu_line.indexOf(ptr.getType() + editNum(ecn.currency, 3)) < 0)
                    return 31;
                ecn.acct = input.pb;
                return tnd_ec_card();
            case 19:
                itm.serial = input.pb;
                if ((sts = forceCard(0x10)) > 0)
                    return sts;
                int range = tnd_range(itm.serial);
                if (range != itm.tnd) {
                    logger.info("Problem in range: [" + itm.serial + "] [" + itm.tnd + "] [" + range + "]");
                    return 31;
                }
                if ((sts = tnd_hotval(itm.serial)) > 0)
                    return sts;
                itm.flag |= T_BNKREF;
                tnd_prompt();
                return 0;
            default:
                // TSC-MOD2014-AMZ#BEG
                if (itm.tnd == CreditNote.getOption(CreditNote.CREDITNOTEUSE)) {
                    CreditNote.init(input.pb);
                    itm.number = CreditNote.getNumber();
                    if ((sts = CreditNote.getStatus()) == 0) {
                        input.reset(Long.toString(CreditNote.getAmount()));
                        sts = action3(0);
                    }
                    return sts;
                }
                if (itm.tnd == Voucher.getOption(Voucher.TNDVOUCHER)) {
                    itm.number = input.pb;
                    sts = Voucher.gluCheck(itm.number, 1);
                    if (sts == 0) {
                        input.reset(Long.toString(Voucher.getAmount()));
                        event.next(event.nxt);
                        if ((sts = action3(0)) > 0) {
                            Voucher.gluCheck(itm.number, 0);
                        }
                    }
                    return sts;
                }

                // TSC-MOD2014-AMZ#END
                itm.number = input.pb;
                if (ptr.getType() == 'D') {
                    if ((sts = forceCard(0x40)) > 0)
                        return sts;
                    if ((sts = tnd_hotval(itm.number)) > 0)
                        return sts;
                }
                dspLine.init(Mnemo.getText(15)).upto(20, itm.number);
        }
        if ((ptr.flag & T_BNKREF) > 0 && ptr.getType() != 'S')
            event.nxt = event.alt;
        else
            tnd_prompt();

        logger.info("itm.tnd: " + itm.tnd);

        if (ECommerce.getTenderProperties().keySet().contains(itm.tnd)) {
            tra.cleanPo = true;
            tra.special.add(Transact.ECOMMERCE);
            ECommerce.setAccount(input.pb.trim());
            if (tra.amt > 0) {
                Itemdata itmOld = itm;
                int ris = ECommerce.automaticSaleItem();

                logger.info("ris: " + ris);
                if (ris > 0) {
                    panel.clearLink(Mnemo.getInfo(ris), 1);
                }
                tnd_prompt();
                itm = itmOld;
            }
        }
        return 0;
    }

    /**
     * tender amount
     **/
    public int action3(int spec) {
        int ind, sts;
        TndMedia ptr = tnd[itm.tnd];
        long lim = ptr.limit[L_MaxTnd], value;

         //INSTASHOP-MULTITENDER BEG
        if (ECommerce.isContinueInstaPayment()) {
            itm.tnd = ECommerce.getTndInstashop();
        }

        if (spec == 0 && ECommerce.getInstashopChoiceMap().get(itm.tnd) != null &&
                !ECommerce.getInstashopChoiceMap().get(itm.tnd).isEmpty()
                && tra.spf1 == M_TRRTRN) {

            ECommerce.setIsReturnInstashop(true);
        }
        //INSTASHOP-MULTITENDER END

        //INSTASHOP-FINALIZE-CGA#A BEG
        if (ECommerce.isFinalizeInstashop()) {
            logger.info("input.pb: " + input.pb);

            if (ECommerce.searchInstashopSuspend(input.pb) < 0
                && ECommerce.getCurrentIndexTnd() == -1) {  //INSTASHOP-MULTITENDER
                logger.info("EXIT amazon0, return 8");
                return 8;
            }

            ECommerce.setCurrentIndexTnd(1);

            tra.head = 100;
            tra.special.add(Transact.ECOMMERCE);
            tra.slm_nbr = 1;
            tra.mode = 0;
			//INSTASHOP-MULTITENDER BEG
            input.pb = String.valueOf(ECommerce.getAmountTndInstashop().get(ECommerce.getCurrentIndexTnd()));
			tra.amt = -ECommerce.getTotalInstashop();
            itm.tnd = Integer.parseInt(ECommerce.getTenderInstashopUsed().get(ECommerce.getCurrentIndexTnd()).toString());
            itm.pos = Long.parseLong(ECommerce.getAmountTndInstashop().get(ECommerce.getCurrentIndexTnd()).toString());
            //input.pb = String.valueOf(ECommerce.getAmountInstashop());
            //itm.pos = tra.amt = -ECommerce.getAmountInstashop();
            //itm.tnd = ECommerce.getTenderInstashopUsed();
            if (ECommerce.getCurrentIndexTnd() == ECommerce.getTenderInstashopUsed().size()-1) {
                ECommerce.setIsFinalizeInstashop(false);
            }
            //INSTASHOP-MULTITENDER END

            input.num = input.pb.trim().length();
        }
        //INSTASHOP-FINALIZE-CGA#A END

        //INSTASHOP-SELL-CGA#A BEG
        ECommerce.setContinueInstaPayment(false);  //INSTASHOP-MULTITENDER

        if (spec == 0 && ECommerce.getInstashopChoiceMap().get(itm.tnd) != null &&
                !ECommerce.getInstashopChoiceMap().get(itm.tnd).isEmpty() && !ECommerce.isResumeInstashop()) {

            if (!input.pb.isEmpty() && !input.pb.equals(String.valueOf(tra.bal))) {
                //INSTASHOP-MULTITENDER BEG
                long amtEnter = Long.parseLong(input.pb.trim());
                if (amtEnter > Math.abs(tra.bal)) {
                    return 7;
                }
                ECommerce.setContinueInstaPayment(true);
                ECommerce.setIsPartialInstaPayment(true);
                //return 7;
                //INSTASHOP-MULTITENDER END
            }
        } else {
            ECommerce.setIsPartialInstaPayment(false);  //INSTASHOP-MULTITENDER
        }
        //INSTASHOP-SELL-CGA#A END

        if (spec > 0) {
            if ((value = csh_tbl[spec - 1]) == 0)
                return 5;
            if (input.num > 0)
                value *= input.scanNum(input.num);
            input.reset(Long.toString(value));
        }
        if ((ptr.flag & T_NOAMNT) > 0)
            if (input.num > 0)
                return 2;
        if ((ptr.flag & T_NOSKIP) > 0)
            if (input.num < 1)
                return 3;
        if (input.num == 0) {
            itm.pos = Math.abs(tnd_limit(0, 1000));
            if ((ptr.xflg & 0x40) == 0
                    && !ECommerce.isResumeInstashop()) {  //INSTASHOP-FINALIZE-CGA#A
                itm.pos = ptr.round(itm.pos);
            }
            input.reset(Long.toString(itm.pos));
        } else if (!ECommerce.isFinalizeInstashop()) {  //INSTASHOP-MULTITENDER
            itm.pos = input.scanNum(input.num);
        }
        if (itm.pos != ptr.round(itm.pos)
                && !ECommerce.isResumeInstashop())   //INSTASHOP-FINALIZE-CGA#A
            return 8;
        if ((itm.amt = ptr.fc2hc(itm.pos)) == 0)
            return 8;
        if (ptr.getType() == 'P' && itm.pos > tra.bal)
            return 46;
        if (itm.pos < ptr.limit[L_MinTnd])
            return 46;
        if ((itm.flag & 0x100) > 0) /* offline */ {
            if (itm.pos < ptr.limit[L_MinOfl])
                return 46;
            if (ptr.limit[L_MaxOfl] > 0)
                if (itm.pos >= ptr.limit[L_MaxOfl])
                    return 46;
        }
        itm.dsc = -roundBy(itm.amt * ptr.rate, 1000);
        itm.crd = roundBy((itm.amt + itm.dsc) * ptr.xtra, 1000);
        value = ptr.hc2fc(Math.abs(tra.bal + itm.dsc + itm.crd));
        value = ptr.round(value);
        if (tra.bal != 0) {
            if (!ECommerceManager.getInstance().isTransactionBasket()) {  //ECOMMERCE-CGA#A
                if (itm.pov > 0) /* either cashback or change */
                    if (itm.pos > value)
                        return 5;
                if (ptr.limit[L_MaxChg] > 0)
                    if (itm.pos >= value + ptr.limit[L_MaxChg])
                        return 46;
            }
        }
        if (tra.bal <= 0) {
            itm.pov = -itm.pov;
            itm.pos = -itm.pos;
            itm.amt = -itm.amt;
            //TAU-20160816-SBE#A BEG
            if ((itm.flg2 & T_NEGTRA) == 0 && itm.pos + tnd[itm.tnd].coin < tra.bal
                    && tra.mode == M_GROSS) {  //FIX-20170213-CGA#A
                return 105;
            }
            //TAU-20160816-SBE#A END
        }
        if (ptr.getType() == 'H') {
            if (itm.pos > BcrIo.getDeposit(itm.tnd))
                return 46;
        }
        if (Math.abs(itm.pos) >= lim) {
            if (tra.stat > 1)
                if (ctl.mode != M_RENTRY) {
                    ind = reg.findTnd(itm.tnd, 1) - 1;
                    value = reg.sales[ind][0].total;
                    value = ptr.fc2hc(value + itm.pos);
                    if (ptr.getType() == 'B') {
                        if (rCLS.find("C" + editNum(itm.tnd, 2), cus.getNumber()) > 0)
                            value += rCLS.block.total;
                        lim = cus.getLimchk() > 0 ? Math.abs(value) - cus.getLimchk() + 1 : 0;
                    }
                    if (ptr.getType() == 'D') {
                        if (rCLS.find("C" + editNum(itm.tnd, 2), cus.getNumber()) > 0)
                            value += rCLS.block.total;
                        lim = cus.getLimcha() > 0 ? Math.abs(value) - cus.getLimcha() + 1 : 0;
                    }
                }
            if (lim > 0) {
                sts = GdSigns.chk_autho(Mnemo.getInfo(46));
                if (sts > 0)
                    return sts;
            }
        }
        // TSC-MOD2014-AMZ#BEG
        if (GdTsc.isAuthCodeEnabled(ptr.getType()) && (ptr.flag & T_NUMBER) > 0) {
            if ((sts = GdTsc.authorizationCode()) > 0) {
                return sts;
            }
        }
        // TSC-MOD2014-AMZ#END
        itm.cnt = signOf(itm.pos);

        if ((ptr.flag & T_NEGTND) > 0)
            itm.cnt = 0 - itm.cnt;

        if (tra.tnd == 0) AutoSaleManager.getInstance().addCommission(itm);

        if (!ECommerceManager.getInstance().getAuthCode().equals("")) {
            Itmdc.IDC_write('z', Struc.tra.tnd, 0, ECommerceManager.getInstance().getAuthCode(), 1, itm.amt);
        }

        if (tnd[itm.tnd].getType() == 'M') {
            logger.info("It's a verifone tender");
            if (!SscoPosManager.getInstance().isUsed()) {
                oplLine.init(Mnemo.getText(81)).show(1);
                panel.clearLink(Mnemo.getInfo(74), 2);
            }
            MarshallEftPlugin eftPlugin = (MarshallEftPlugin) eftPluginManager.getPlugin(tnd[itm.tnd].getType()+"");
            sts = eftPlugin.doTransaction(itm.pos, String.valueOf(ctl.tran));
            if (sts != 0) {
                logger.debug("EXIT - return error: " + sts);
                return sts;
            }
            itm.tnd = eftPluginManager.getEftTender(eftPlugin.getCardType(), itm.tnd); // AMZ-2017-002#ADD
        }

        logger.info("Tender type: " + tnd[itm.tnd].getType());
        sts = eftPluginManager.pay("" + tnd[itm.tnd].getType(), itm, ctl, oplLine);
        if (sts != 0) {
            logger.info("return sts >" + sts + "<");
            return sts;
        }

        if (GIFTCARD_TENDER_TYPES.indexOf(ptr.getType()) >= 0) {
            CustomTender account = GiftCardPluginManager.getInstance().getCustomTender(itm.tnd);
            logger.debug("Pay with Gift Card");

            if (account != null) {
                itm.accountType = account.getAccountType();
                logger.debug("AccountType = " + itm.accountType);
                itm.accountNumberRequired = account.isAccountNumberRequired();
                logger.debug("accountNumberRequired = " + itm.accountNumberRequired);
            }

            Itemdata temp = itm.copy();
            //itm = temp.copy();
            int gcTndCustom = GiftCardPluginManager.getInstance().getCustomTenderByGCSerialNumber(temp.giftCardSerial);
            if (gcTndCustom != 0) {
                itm.tnd = gcTndCustom;
                //GiftCardPluginManager.getInstance().addPayment(itm);
            }
            SpecialItem specialItem = null;
            if (tra.amt >= 0) {
                logger.debug("Pay with Gift Card");
                specialItem =  GiftCardPluginManager.getInstance().redemptionGiftCard(temp, tra);
            } else {
                logger.debug("Refund Gift Card");
                specialItem =  GiftCardPluginManager.getInstance().refundGifCard(temp, ctl);
            }


            if (specialItem.getStatus() > 0) return specialItem.getStatus();
            itm.amt = itm.pos = specialItem.getAmount();
            if (ptr.getType() == 'S') {
                if (specialItem.getStatus() == 0) {
                    Itmdc.IDC_write('N', tra.tnd, 8, GiftCardPluginManager.getInstance().mask(itm.giftCardSerial) , itm.cnt, itm.pos);
                }
            }
        }
        if (ptr.getType() == 'R') {
            RedemptionItem redemptionItem = GdBindawood.getInstance().redeemReward(itm.pos);
            if (redemptionItem.getStatus() > 0) return redemptionItem.getStatus();
            itm.amt = itm.pos = redemptionItem.getAmount();
        }

        logger.info("tnd[itm.tnd].type: " + tnd[itm.tnd].getType());
        if (tnd[itm.tnd].getType() == 'P') {
            logger.info("call GPEPayment()");
            sts = GPEPayment();

            logger.info("sts: " + sts);
            if (sts > 0) {
                return sts;
            } else {
                if (sts == -2) {
                    return 0;
                } else {
                    if (sts == -1) {
                        return sts;
                    }
                }
            }
        }

        if (ECommerceManager.getInstance().getBasket() == null) {
            //INSTASHOP-SELL-CGA#A BEG
            if (ECommerce.getInstashopChoiceMap().get(itm.tnd) != null &&
                    !ECommerce.getInstashopChoiceMap().get(itm.tnd).equals("")
                    && !ECommerce.isResumeInstashop()) {

                boolean suspend = ECommerce.handleInstashopPayment();
                logger.info("ECommerce.getInstashopChoiceType(): " + ECommerce.getInstashopChoiceType());
                logger.info("suspend: " + suspend);

                ECommerce.setTndInstashop(itm.tnd);
                try {
                    if (suspend) {
                        logger.info("on delivery");

                        //INSTASHOP-MULTITENDER BEG
                        if (tra.spf1 == M_TRRTRN) {
                            return 7;
                        }

                        int tend = 1;
                        if (ECommerce.getInstashopChoiceType().trim().equals("SC")) {
                            tend = ECommerce.getAutomaticTndCard().get(itm.tnd);
                        } else {
                            tend = ECommerce.getAutomaticTndCash().get(itm.tnd);
                        }
                        logger.info("itm.tnd: " + tend);
                        logger.info("call writeInstashopSuspend() function ");

                        String lineSuspend = ECommerce.searchTransInstashop();

                        if (lineSuspend.equals("")) {
                            if (ECommerce.isPartialInstaPayment() && tra.bal != tra.amt) {
                                return 7;
                            } else {
                                itm.tnd = tend;
                                ECommerce.writeInstashopSuspend();
                            }
                        } else {
                            itm.tnd = tend;
                            ECommerce.addTenderTransInstashop(lineSuspend);
                        }

                    } else {
                        logger.info("online");
                        String lineSuspend = ECommerce.searchTransInstashop();

                        if (!lineSuspend.equals("")) {
                            return 7;
                        }
                        else {
                            String choiceType = ECommerce.getInstashopChoiceType().trim();
                            itm.tnd = ECommerce.getAutomaticTndOnline().get(choiceType + "." + itm.tnd);
                        }
                    }
                    //INSTASHOP-MULTITENDER END
                } catch (Exception e) {

                }

                //INSTASHOP-MULTITENDER BEG
                if (ECommerce.isContinueInstaPayment() || ECommerce.isReturnInstashop()) {
                    int oldTnd = itm.tnd;
                    itm.tnd = ECommerce.getTndInstashop();
                    input.pb = ECommerce.getAccount();

                    action2(0);

                    itm.tnd = oldTnd;
                }
                //INSTASHOP-MULTITENDER END

                return tnd_print();
            }
            //INSTASHOP-SELL-CGA#A END
        }

        int amount = (int) itm.amt;
        int changeDue = (int) (tra.bal - itm.amt);
        int totalAmount = (int) tra.amt;
        int itemCount = (int) tra.cnt;
        if (changeDue > 0) changeDue = 0;

        //INSTASHOP-FINALIZE-CGA#A BEG
        logger.info("itm.pos: " + itm.pos);
        logger.info("input.pb: " + input.pb);
        logger.info("tra.amt: " + tra.amt);


        if (!input.pb.isEmpty()
               && ECommerce.getTotalInstashop() != 0 && !ECommerce.isFinalizeInstashop()) {  //INSTASHOP-MULTITENDER
            if (Integer.parseInt(input.pb) >= Math.abs(tra.bal)) {
                tra.amt = 0;
                ECommerce.handleInstashopResume(ECommerce.getNumberTraResume());
            } else {
                return 8;
            }
            //INSTASHOP-MULTITENDER END
        }
        //INSTASHOP-FINALIZE-CGA#A END

        int ret = tnd_print();

        if (SscoPosManager.getInstance().isUsed()) {
            SscoTender tender = SscoPosManager.getInstance().getCurrentSscoTender();
            tender.setAmount(amount);

            SscoPosManager.getInstance().addTender(tender);
            SscoPosManager.getInstance().updateTotalAmount(totalAmount, (int) tra.bal, itemCount, changeDue);
            SscoPosManager.getInstance().tenderResponse();
        }

        while (ECommerce.isFinalizeInstashop()) {
            tra.bal = 0;
            int success = action3(0);

            if (success != 0) {
                return success;
            }
        }

        return ret;
    }

    /**
     * error correct
     **/
    public int action4(int spec) {
        if (TView.syncIndex(pit.index) != pit.index)
            return 7;
        // PSH-ENH-009-AMZ#BEG -- cancel payment
        if (pit.gCardPayment) {
            int ret = GiftCardPluginManager.getInstance().cancelPayGiftCard(pit, tra);
            if (ret != 0) {
                return ret;
            }
        }
        // PSH-ENH-009-AMZ#BEG -- cancel payment
        itm = pit.copy();
        itm.mark = Mnemo.getText(60).charAt(6);
        itm.amt = -itm.amt;
        itm.cnt = -itm.cnt;
        itm.crd = -itm.crd;
        itm.com = -itm.com;
        itm.pos = -itm.pos;
        itm.flag &= ~T_ONSLIP;
        int sts = tnd_print();
        if (sts > 0)
            GdTrans.itm_clear();
        return sts;
    }

    /**
     * type selection
     **/
    public int action5(int spec) {
        int ic, sts;
        long amt = tra.bal;

        // TSC-MOD2014-AMZ#BEG
        if (tnd[spec].blackListed) {
            if ((sts = BlackList.checkCustomerStatus()) > 0) {
                return sts;
            }
        }
        if (spec != 0) {
            if (spec == CreditNote.getOption(CreditNote.CREDITNOTEUSE)) {
                if ((tra.spf1 & M_TRVOID) > 0 || (tra.spf1 & M_TRRTRN) > 0) {
                    return 7;
                }
                if (ctl.lan > 0) {
                    // return 80; // TSC-MOD2014-AMZ#DEL --nmemo remap NOTE NUMBER
                    return 137; // TSC-MOD2014-AMZ#ADD --nmemo remap
                } else {
                    tnd[spec].setType('H');
                }
            } else if (spec == CreditNote.getOption(CreditNote.CREDITNOTEPRINT)) {
                CreditNote.setPrinted(false);
            }
        }
        // TSC-MOD2014-AMZ#END
        //WINEPTS-CGA#A BEG
        boolean isDirectFullPayment = false;
        if (input.num == 0 && tnd[spec].getType() == 'P') {
            logger.info("isDirectFullPayment true");
            isDirectFullPayment = true;
        }
        //WINEPTS-CGA#A END
        //INSTASHOP-RESUME-CGA#A BEG
        logger.info("ECommerce.getInstashopChoiceMap().get(spec): " + ECommerce.getInstashopChoiceMap().get(spec));
        logger.info("ECommerce.isResumeInstashop(): " + ECommerce.isResumeInstashop());
        if (ECommerce.isResumeInstashop() && ECommerce.getInstashopChoiceMap().get(spec) != null &&
                !ECommerce.getInstashopChoiceMap().get(spec).equals("")) {
            return 7;
        }
        //INSTASHOP-RESUME-CGA#A END

        if (spec < 1 || spec >= tnd.length)
            return 5;
        if ((sts = sc_checks(ic = 10 + spec, 1)) > 0)
            return sts;
        if ((tra.spf1 & M_TRVOID) > 0)
            amt = -amt;
        // TSC-MOD2014-AMZ#BEG
        if (spec == Voucher.getOption(Voucher.TNDVOUCHER) && amt < 0) {
            return 7;
        }
        // TSC-MOD2014-AMZ#END
        if (amt > 0 && (tnd[spec].flag & T_NEGTND) > 0)
            return 7;
        if (spec / 10 == 2) {
            if (tra.stat > 2 && cus.getSpec() > 0)
                if (spec != cus.getSpec() + 19)
                    return 7;
        }
        if (tnd[spec].getType() >= 'C' && tnd[spec].getType() <= 'F') {
            if (reg.sales[reg.find(ic, 1) - 1][0].items != 0 && !ECommerce.isResumeInstashop())
                return 7;
            itm.number = editNum(lREG.block[0].trans + 1, 4);
        }
        if (tnd[spec].getType() == 'H') {
            if (!BcrIo.isActive())
                return 7;
            mon.money = spec;
        }
        if (tnd[spec].till > 0) {
            if (amt < 0)
                DevIo.drwPulse(tnd[spec].till);
        }
        ecn.cheque = "";

        input.reset(String.valueOf((int) tnd[itm.tnd = spec].getType()));

        //WINEPTS-CGA#A BEG
        //return group[0].action1(0);

        int retSts = group[0].action1(0);
        logger.info("retSts: " + retSts);
        if (retSts == 0) {
            if (isDirectFullPayment) {
                //input.reset(Long.toString(tra.bal));
                //return Action.group[event.act / 10].exec();
            }
        }

        return retSts;
        //WINEPTS-CGA#A END
    }

    /**
     * cash back
     **/
    public int action6(int spec) {
        long value = input.scanNum(input.num);

        if ((itm.pov = value) > 0) {
            if (value != tnd[1].round(value))
                return 8;
            if (value < tnd[itm.tnd].limit[L_MinCsh])
                return 46; /* minimum cashback */
            if (tnd[itm.tnd].limit[L_MaxCsh] > 0) /* maximum cashback */
                if (value >= tnd[itm.tnd].limit[L_MaxCsh])
                    return 46;
            if (Math.abs(tra.bal) < tnd[itm.tnd].limit[L_MinSls]) /* min sales */
                if (tra.bal != 0)
                    return 7;
            itm.pov = tnd[1].fc2hc(value);
            if ((itm.pov = tnd[itm.tnd].hc2fc(itm.pov)) < 1)
                return 8;
            dspLine.init(Mnemo.getText(66)).upto(20, editMoney(1, value));
        }
        tnd_prompt();
        return 0;
    }

    /**
     * BCR services
     **/
    public int action7(int spec) {
        if (tnd[itm.tnd].getType() != 'H')
            return -1;
        BcrDlg dlg = new BcrDlg(System.getProperty("BCR"));
        {
            for (int ind = 0; ind < dnom_tbl.length; ind++) {
                CshDenom ptr = tnd[itm.tnd].dnom[ind];
                if (ptr.value < 1)
                    break;
                dlg.add(ptr.text.trim(), "CSH_" + editNum(itm.tnd * 100 + ind, 4));
            }
        }
        input.init(0x80, 0, 0, 0);
        DevIo.oplSignal(15, 1);
        dlg.show("BCR");
        DevIo.oplSignal(15, 0);
        if (dlg.code > 0)
            return dlg.code;
        if (input.key == 0)
            input.key = input.CLEAR;
        return input.key == input.JRNAL ? 0 : 5;
    }


    /**
     * tender copy on slip
     **/
    public int action9(int spec) {
        if (!DevIo.station(4))
            return 7;
        if (tra.tslp > 0) {
            if (input.num > 0)
                return 5;
            slpStatus(2, tra.tslp);
            return 0;
        }
        if (input.num == 0) {
            if ((tra.slip & 0x10) > 0) {
                slpStatus(1, 1);
                input.key = input.CLEAR;
            }
            return 5;
        }
        int nbr = input.scanNum(input.num);
        if (nbr < 1 || nbr > 32)
            return 8;
        slpStatus(3, nbr);
        return 0;
    }


    // TONETAG-CGA#A BEG
    public static long getAmountByToneTag() {
        return amtByToneTag;
    }

    public static void setAmountByToneTag(long amountByToneTag) {
        amtByToneTag = amountByToneTag;
    }
    // TONETAG-CGA#A END


}
