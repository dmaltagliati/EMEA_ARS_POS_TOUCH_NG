package com.ncr.notes;

import com.ncr.*;
import com.ncr.gui.SelDlg;
import com.ncr.gui.SpyDlg;

public abstract class Notes extends Action {
	static int ack_note(int rec) {
		if (rNEW.read(rec) < 1)
			return 16;
		if (rNEW.reg2 == ctl.reg_nbr && rNEW.sts > 0) {
			rNEW.dat2 = ctl.date;
			rNEW.tim2 = ctl.time / 100;
			rNEW.sts = 0;
			if (rNEW.write(rec) < 1)
				return 16;
		}
		GdPos.panel.dspNotes(0, null);
		DevIo.oplSignal(3, mon.snd_mon == null ? 0 : 1);
		mon.alert = ERROR;
		return 0;
	}

	static int snd_note(int nbr) {
		rNEW.reg1 = ctl.reg_nbr;
		rNEW.ckr = ctl.ckr_nbr;
		rNEW.dat1 = ctl.date;
		rNEW.tim1 = ctl.time / 100;
		rNEW.reg2 = note_tbl[nbr];
		rNEW.dat2 = rNEW.tim2 = 0;
		rNEW.text = note_txt[rNEW.sts = nbr];
		return rNEW.write(0) > 0 ? 0 : 16;
	}

	public static int show_me() {
		int code = 0;

		if (ctl.lan > 2)
			return showHelp();
		GdPos.panel.display(1, Mnemo.getMenu(63));
		if (mon.rcv_mon != null) {
			if (lCTL.find(mon.rcv_ckr) > 0)
				GdPos.panel.display(2, lCTL.text);
			GdPos.panel.clearLink(mon.rcv_msg, 2);
			return ack_note(mon.rcv_dsp);
		}
		input.prompt = Mnemo.getText(15);
		input.init(0x00, 1, 1, 0);
		SelDlg dlg = new SelDlg(Mnemo.getText(22));
		while (++code < note_txt.length)
			dlg.add(1, Integer.toString(code), note_txt[code]);
		dlg.show("MSG");
		if (dlg.code > 0)
			return dlg.code;
		if (input.key == 0)
			input.key = input.CLEAR;
		if (input.num < 1 || input.key != input.ENTER)
			return 5;
		if ((code = input.adjust(input.pnt)) > 0)
			return code;

		code = input.scanNum(input.num);
		if (code < 1 || note_txt[code] == null)
			return 8;
		if (mon.snd_mon != null)
			return 7;
		if (note_tbl[code] == 0)
			return 7;
		return snd_note(code);
	}

	public static String getMessTxt(int ind) {
		String txt = mess_txt[ind];
		if (txt != null)
			switch (txt.charAt(0)) {
			case '@':
				return null;
			case '>':
				return txt.substring(1);
			}
		return txt;
	}

	public static void advertize() {
		int pos, size = 20;
		String txt;

		if (mon.adv_rec < 1) {
			mon.adv_dsp = 60;
		}
		if ((pos = mon.adv_dsp) == 60) {
			if (ctl.ckr_nbr < 1 || ctl.ckr_nbr > 799 || ctl.mode > 0) {
				txt = GdPos.panel.mnemo.getInfo(ctl.mode > 0 ? ctl.mode + 17 : 0);
				mon.adv_rec = 16;
				pos = 40 - size;
			} else {
				if (mon.adv_rec > 15) {
					mon.adv_rec = 0;
				}
				if ((txt = mess_txt[mon.adv_rec++]) == null) {
					return;
				}
				pos = 20 - size;
			}
			mon.adv_txt = rightFill(editTxt(txt, 59), 79, ' ');
		}
		if (pos == 40) {
			if (mon.adv_rec < 16) {
				if ((txt = mess_txt[mon.adv_rec]) != null) {
					mon.adv_rec++;
					pos = 0;
					mon.adv_txt = mon.adv_txt.substring(40, 59) + rightFill(txt, 60, ' ');
				}
			}
		}
		// COP-ENH-C5976 #D Begin
		// DevIo.cusDisplay(1, mon.adv_txt.substring(pos, pos + size));
		// mon.adv_dsp = pos + 1;
		// COP-ENH-C5976 #D End
		advertizeScroll(pos, size); // COP-ENH-C5976#A

	}

	static int advertizeTypeScroll = 0; // ENH-C5976#A

	static void advertizeScroll(int pos, int size) {
		int type = advertizeTypeScroll;

		switch (type) {
			case 1:
				if ((mon.tick % 2) == 0) {
					DevIo.cusDisplay(1, mon.adv_txt.substring(pos, pos + size));
					mon.adv_dsp = pos + 1;
				}
				break;

			case 2:
				if ((mon.tick % 2) == 0) {
					DevIo.cusDisplay(1, mon.adv_txt.substring(pos, pos + size));
					mon.adv_dsp = pos + 1;
				} else {
					DevIo.cusDisplay(1, editTxt("", 20));
				}

				break;

			case 3:
				DevIo.cusDisplay(1, editTxt("", 20));
				DevIo.cusDisplay(1, mon.adv_txt.substring(pos, pos + size));
				mon.adv_dsp = pos + 1;
				break;

			default:
				DevIo.cusDisplay(1, mon.adv_txt.substring(pos, pos + size));
				mon.adv_dsp = pos + 1;
				break;
		}
		// STD-ENH-ASR31CID-SBE#A BEG
		GdPos.panel.cid.display(0, mon.adv_txt.substring(pos, pos + size));
		// STD-ENH-ASR31CID-SBE#A END
	}

	public static void watch(int rec) {
		SpyDlg dlg = (SpyDlg) GdPos.panel.modal;
		GdElJrn area = dlg.area;
		int rows = area.rows;

		if (rec == 0) {
			if (area.bar.getValue() < area.bar.getMaximum() - rows)
				return;
			rec = mon.watch;
			while (lJRN.read(mon.watch, tra.comm) > 0) {
				dlg.add(lJRN.pb);
				rec = mon.watch++;
			}
			if (rec == mon.watch)
				return;
			area.bar.setValues(rec - rows, rows, 0, rec);
		} else
			for (int ind = 0; ind < rows; ind++) {
				dlg.add(lJRN.read(rec + ind, tra.comm) > 0 ? lJRN.pb : null);
			}
		area.repaint();
	}
}
