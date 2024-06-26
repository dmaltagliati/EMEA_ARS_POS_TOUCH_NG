package com.ncr;

import com.ncr.ecommerce.ECommerce;
import com.ncr.ecommerce.ECommerceManager;
import com.ncr.eft.EftPluginManager;
import com.ncr.gpe.PosGPE;
import com.ncr.gui.Dynakey;
import com.ncr.notes.Notes;
import com.ncr.ssco.communication.manager.SscoPosManager;
import com.ncr.zatca.ZatcaManager;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;

/*******************************************************************
 * central dispatch of actions and monitors
 *******************************************************************/
public abstract class Action extends Basis {
	/** all instances of appl classes with methods = actions **/
	public static final Action group[] = new Action[23];    //ECOMMERCE-SBE#A
	private static long eptsCheckDeltaMilliSec = 0;
	private static long milliSec = System.currentTimeMillis();
	private static final Logger logger = Logger.getLogger(Action.class);

	/***************************************************************************
	 * instanciate all application classes / initialize device i/o
	 *
	 * @param panel
	 *            reference to gui provider
	 ***************************************************************************/
	//static void init(GdPos panel) {
	static void initialize() {
		//gui = Action.panel;

		logger.debug("Initializing Actions");
		group[0] = new GdSigns();
		group[1] = new GdRegis();
		group[2] = new GdCusto();
		group[3] = new GdTrans();
		group[4] = new GdSales();
		group[5] = new GdPrice();
		group[6] = new GdMoney();
		group[7] = new GdTndrs();
		group[8] = new GdCashc();
		group[9] = new GdSuper();
		group[10] = new GdMaint();
		group[11] = GdSarawat.getInstance(); // SARAWAT-ENH-20150507-CGA#A
		group[12] = GdPsh.getInstance(); // PSH-ENH-003-AMZ#ADD
		group[13] = GdSpinneys.getInstance();
		group[14] = GdUmniah.getInstance();
		group[15] = EftPluginManager.getInstance();
		group[16] = new ECommerce();  //INSTASHOP-FINALIZE-CGA#A
		group[17] = new PosGPE();   //WINEPTS-CGA#A
		group[18] = ECommerceManager.getInstance();   //ECOMMERCE-SBE#A
		group[19] = new GdTsc(); // TSC-MOD2014-AMZ#ADD
		group[20] = GdBindawood.getInstance();
		group[21] = GiftCardPluginManager.getInstance();
		group[22] = ZatcaManager.getInstance();


		// MMS-R10#A BEGIN
		// STD-ENH-ASR31TRV-SBE#A BEG
		if (GdPos.panel.journalTable != null) {
			GdPos.panel.journalTable.setActiveModel(JournalTable.MODEL_OLD);
		}
		if (GdPos.panel.journalTable2Screen != null) {
			GdPos.panel.journalTable2Screen.setActiveModel(JournalTable.MODEL_OLD);
		}
		GdPos.panel.journal.init(lJRN, options[O_ElJrn]);
		for (int rec = 1; rec <= lJRN.getSize(); rec++) {
			if (lJRN.read(rec) >= 0) {
				JournalAndDetail.getInstance().addLine(lJRN.pb);

			}
		}
		// STD-ENH-ASR31TRV-SBE#A END
		// MMS-R10#A END

		timeFormat = trl_line.substring(20, 35);
		ctl.setDatim();
//		panel.journal.init(lJRN, options[O_ElJrn]);
//		panel.trxView.init(options[O_ElJrn]);
//		panel.jrnPicture(project);
        GdPos.panel.jrnPicture("GdPos");
//		panel.cid.init();
 		GdPos.panel.cid.init();
//		if (((input.optAuth = options[O_Autho]) & 2) < 2)
//			panel.dspStatus(2, null, true, false);
		dspLine.init("PointOfSales Program");
		DevIo.start();
		netio.start(lFile, version[1], ctl.zero);
		showHeader(false);
		Promo.initialize();
		lLAN.read(1, lLAN.LOCAL);
		if (chk_reboot(lLAN.ckr))
			dspLine.init(Mnemo.getInfo(62)).upto(20, editNum(lLAN.ckr, 3));
		if (lLAN.sts < 90) {
			EftIo.initialize();
			BcrIo.initialize();
			WghIo.initialize(DevIo.scale);
		}
		GdPos.panel.splashThread.interrupt();
    }

	// AMZ-2017#BEG
	static int chkBrokenTransaction() {
		if (SscoPosManager.getInstance().isEnabled()) {
			ExtResume.writeFlagFile("");
			return 0;
		}

		String resume = ExtResume.readFlagFile();
		logger.debug("TSC exit");
		if (resume != null) {
			int key;
			while (true) {
				ExtResume.writeLogFile("Supervisor requested");
				key = GdPos.panel.clearLink(Mnemo.getInfo(108) + " " + resume, 0x83);
				if(!ExtResume.supervisor){
					break;
				}
				if ((input.lck & 0x04) > 0) {
					break;
				}
				return 1;
			}
			if (key == 1) {
				ExtResume.writeFlagFile("");
				ExtResume.writeLogFile("Supervisor cancelled auto resume");
			} else {
				ExtResume.writeLogFile("Supervisor authorized auto resume");
			}
			ExtResume.writeLogFile("------------------------");
		}
		return 0;
	}

	/***************************************************************************
	 * terminate device i/o
	 ***************************************************************************/
	static void stop() {
		Promo.terminate();
		EftIo.terminate();
		//TODO EFT KNET CLOSE
		eftPluginManager.stop(ctl);
		BcrIo.terminate();
		WghIo.terminate();
		netio.stop();
		DevIo.stop();
	}

	/***************************************************************************
	 * run all appl monitor functions (up to 4 times a second)
	 ***************************************************************************/
	static void idle() {
		ctl.setDatim();
		//WINEPTS-CGA#A BEG
		eptsCheckDeltaMilliSec += (System.currentTimeMillis() - milliSec);
		milliSec = System.currentTimeMillis();
		//WINEPTS-CGA#A END
		if (netio.state != ctl.lan)
			if (netio.state < 3) {
				ctl.lan = netio.state;
				String s = ctl.lan == 1 ? "--//--" : netio.lanHost(SRV);
				GdPos.panel.dspStatus(1, s, true, ctl.lan == 2);
				DevIo.oplSignal(0, ctl.lan);
			}
		if (mon.autho > 0)
			if ((input.lck & 0x04) > 0)
				GdPos.panel.innerVoice(input.CLEAR);
		if (mon.adv_rec >= 0)
			Notes.advertize();
		if ((++mon.tick & 3) > 0)
			return;
		input.tic++;
		if (mon.clock >= 0) {
			int mm = (ctl.time / 100) % 100;
			if (mm != mon.clock) {
				hdrLine.init(' ').onto(6, editDate(ctl.date)).onto(15, editTime(ctl.time / 100)).show(0);
				mon.clock = mm;
			}
		}
		if (mon.total >= 0) {
			if ((mon.total += 2) > 5)
				showTotal(-1);
		}
		if (mon.odisp >= 0) {
			DevIo.oplDisplay(mon.odisp & 1, mon.odisp < 2 ? GdPos.panel.dspArea[mon.odisp + 1].getText() : mon.opd_alt);
			mon.odisp ^= 2;
		}
		if (mon.money >= 0)
			BcrIo.watch(mon.money);
		if (DevIo.drwWatch(2))
			GdPos.panel.innerVoice(input.CLEAR);
		if (DevIo.scale.state > 0)
			WghIo.control(DevIo.scale.state);
		if ((input.tic & 5) == 0) {
			if (ctl.ckr_nbr > 0)
				if (mon.alert > 0)
					DevIo.alert(1);
		}
		if (input.isEmpty() && GdPos.panel.modal == null) {
			if (event.menu == 0)
				if (event.act > 0) {
					int code = group[event.act / 10].exec();
					if (code > 0)
						GdPos.panel.innerVoice(code);
				}
		} else if (mon.lan99 > 0) {
			if ((input.tic & 3) == 0)
				if (lLAN.read(mon.lan99, 0) > 0)
					if (lLAN.sts >= 90)
						GdPos.panel.innerVoice(input.CLEAR);
		} else if (mon.hocus > 0) {
			if ((input.tic & 1) == 0)
				if (HoCus.isReady())
					GdPos.panel.innerVoice(input.CLEAR);
		} else if (mon.image > 0) {
			if (netio.isCopied(mon.image))
				GdPos.panel.innerVoice(input.CLEAR);
		} else if (mon.watch > 0) {
			if ((input.tic & 3) == 0)
				Notes.watch(0);
		} else if (ctl.ckr_nbr == 0)
			if (input.tic > 30)
				GdPos.panel.innerVoice(input.CLEAR);
		//WINEPTS-CGA#A BEG
		if (eptsCheckDeltaMilliSec > 5000) {
			eptsCheckDeltaMilliSec = 0;
			PosGPE.checkEptsUPB(false);
		}
		//WINEPTS-CGA#A END

		//ECOMMERCE-SSAM#A BEG
		ECommerceManager.getInstance().sendHeartBeatMessage();
		//ECOMMERCE-SSAM#A END
	}

	/***************************************************************************
	 * dispatch all-time valid function keys
	 *
	 * @param sts
	 *            default error index (5 = input error)
	 * @return final error index
	 ***************************************************************************/
	static int spec(int sts) {
		if (input.key == input.CLEAR) {
			input.sel = 0;
			return sts;
		}
		if (ctl.ckr_nbr > 0) {
			String nod = input.prompt;
			if (input.key == input.NORTH)
				sts = ElJrn.roll(KeyEvent.VK_UP);
			if (input.key == input.SOUTH)
				sts = ElJrn.roll(KeyEvent.VK_DOWN);
			if (input.key == input.JRNAL)
				sts = ElJrn.toggle();
			if (input.key == input.AUTHO)
				sts = GdSigns.key_autho();
			if (input.key == input.PAUSE)
				sts = GdSigns.chk_pause();
			if (input.key == input.NOTES)
				sts = Notes.show_me();
			input.prompt = nod;
		} else {
			if (input.key == input.NOTES)
				sts = showHelp();
			if (input.key == input.AUTHO)
				sts = GdSigns.key_autho();
		}
		if (sts > 0)
			return sts;
		input.key = input.CLEAR;
		return 5;
	}

	/***************************************************************************
	 * dispatch actions via event table processing
	 *
	 * @return status 0=done, >0=error index, <0=try next in section
	 ***************************************************************************/
	final int exec() {
		if (event.key > 0)
			showShort("BSY", event.act);
		try {
			switch (event.act % 10) {
			default:
				return action0(event.spc);
			case 1:
				return action1(event.spc);
			case 2:
				return action2(event.spc);
			case 3:
				return action3(event.spc);
			case 4:
				return action4(event.spc);
			case 5:
				return action5(event.spc);
			case 6:
				return action6(event.spc);
			case 7:
				return action7(event.spc);
			case 8:
				return action8(event.spc);
			case 9:
				return action9(event.spc);
			}
		} catch (Exception e) {
			String msg = "< action" + editNum(event.act, 3) + " failed >";
			logConsole(0, msg, "section base=" + editNum(event.base, 3) + " key=0x" + editHex(event.key, 4) + " spec="
					+ editNum(event.spc, 4));
			e.printStackTrace();
			GdPos.panel.clearLink(msg, 0x81);
			return 7;
		}
	}

	public int action0(int spec) {
		return 0;
	}

	public int action1(int spec) {
		return 0;
	}

	public int action2(int spec) {
		return 0;
	}

	public int action3(int spec) {
		return 0;
	}

	public int action4(int spec) {
		return 0;
	}

	public int action5(int spec) {
		return 0;
	}

	public int action6(int spec) {
		return 0;
	}

	public int action7(int spec) {
		return 0;
	}

	public int action8(int spec) {
		return 0;
	}

	public int action9(int spec) {
		return 0;
	}

	/***************************************************************************
	 * show terminal/operator state information
	 *
	 * @param onDuty
	 *            true = ready state, false = closed state
	 ***************************************************************************/
//	static void showHeader(boolean onDuty) {
//		idsLine.init(Mnemo.getText(3)).upto(17, editKey(ctl.reg_nbr, 3)).show(3);
//		if (ctl.ckr_nbr < 1)
//			idsLine.init(Mnemo.getText(11)).upto(17, editNum(ctl.sto_nbr, 4));
//		else
//			idsLine.init(Mnemo.getText(ctl.ckr_nbr < 800 ? 1 : 2)).upto(17, editNum(ctl.ckr_nbr, 3));
//		idsLine.show(4);
//		panel.display(5, null);
//		mon.clock = 60;
//		mon.opd_sts = 0;
//		dspBmap = "CKR_" + editNum(ctl.ckr_nbr, 4);
//		panel.dspSymbol(onDuty ? tnd[0].symbol : "");
//		panel.journal.setScrollbar(onDuty);
//		panel.cid.clear();
//		showAutho();
//		if (!onDuty) {
//			cusLine.init(Mnemo.getText(3)).upto(20, editKey(ctl.reg_nbr, 3)).show(11);
//		}
//	}

	static void showHeader(boolean onDuty) {

		idsLine.init(GdPos.panel.mnemo.getText(3)).upto(17, editKey(ctl.reg_nbr, 3)).show(3);
		if (!onDuty) {
			idsLine.init(GdPos.panel.mnemo.getText(11)).upto(17, editNum(ctl.sto_nbr, 4));
		} else {
			idsLine.init(GdPos.panel.mnemo.getText(ctl.ckr_nbr < 800 ? 1 : 2)).upto(17, editNum(ctl.ckr_nbr, 3));
		}
		idsLine.show(4);
		mon.clock = 60;
		dspBmap = "CKR_" + editNum(ctl.ckr_nbr, 4);
		GdPos.panel.dspSymbol(onDuty ? tnd[0].symbol : "");

	}

	/***************************************************************************
	 * show bonuspoints to operator and customer
	 *
	 * @param value
	 *            score
	 ***************************************************************************/
	static void showPoints(int value) {
		if (GdTsc.isStandardFidelity()) return;
		int ind = (options[O_Custo] & 0x04) == 0 ? 39 : 0;
		cusLine.init(Mnemo.getText(ind)).upto(20, editInt(value)).show(12);
		GdPos.panel.dspPoints(cusLine.toString());
	}

	/***************************************************************************
	 * show authorization state information
	 ***************************************************************************/
	static void showAutho() {
		if ((input.optAuth & 2) > 0) {
			input.lck &= 0xF0;
			input.lck |= ctl.ckr_nbr < 800 && (input.lck & 0x10) == 0 ? 1 : 4;
		}
		GdPos.panel.dspStatus(3, null, (input.lck & 0x10) > 0, (input.lck & 0x20) > 0);
		DevIo.oplSignal(4, (input.lck & 0x20) > 0 ? 2 : input.lck >> 2 & 1);
		DevIo.oplSignal(5, input.lck >> 4 & 1);
	}

	/***************************************************************************
	 * prepare state dependent dynakey inscription
	 *
	 * @param ind
	 *            index of dynakey (0 - 7)
	 ***************************************************************************/
	static void showDynakey(Dynakey d) {

		String rule = input.dkyRule(d.getIndex());
		char fnc = rule.charAt(0);
		int ind, key = Integer.parseInt(rule.substring(1));

		d.reset(JLabel.CENTER);
		if (fnc == 'C') {
			if (csh_tbl[--key] > 0) {
				// d.text = editTxt(editMoney(0, csh_tbl[key]), 8);
				d.setLineTop(editTxt(editMoney(0, csh_tbl[key]), 8));
			}
		}
		if (fnc == 'D') {
			// d.text = d.txt2 = "";
			// d.align = JLabel.LEFT;
			d.setLineTop("");
			d.setLineBottom("");
			// if ((ind = dir_tbl[--key]) > 0) {//EMEA-00046-DSA
			if ((ind = dir_tbl[--key]) != 0) {
				// d.text = ind < 0xffff ? dir_txt[key] : plu_tbl[key];
				// d.text = ind > 0 ? dir_txt[key] : plu_tbl[key]; //
				// EMEA-00046-DSA
				d.setLineTop(ind > 0 ? dir_txt[key] : plu_tbl[key]);
			}
			if (plu_txt[key] != null) {
				// d.txt2 = plu_txt[key];
				d.setLineBottom(plu_txt[key]);
			}
		}
		if (fnc == 'K') {
			// d.text = GdPos.panel.mnemo.getText(key);
			d.setLineTop(GdPos.panel.mnemo.getText(key));
		}
		if (fnc == 'M') {
			String s = GdPos.panel.mnemo.getMenu(key);

			// d.text = s.substring(0, 10);
			d.setLineTop(s.substring(0, 10));

			// d.txt2 = s.substring(10);
			d.setLineBottom(s.substring(10));
		}
		if (fnc == 'T') {
			// d.text = tnd[key].text;
			d.setLineTop(tnd[key].text);
		}
		if (fnc == 'Y') {
			String s = GdPos.panel.mnemo.getDyna(key);
			d.setLineTop(s.substring(0, 10));
			d.setLineBottom(s.substring(10));
		}

	}

	/***************************************************************************
	 * prepare dynakey inscription with list selector
	 *
	 * @param ind
	 *            index of dynakey (0 - 7)
	 ***************************************************************************/
//	static void showDynaTch(int ind) {
//		Dynakey d = panel.dyna.keys[ind];
//		d.setText(0, sel_tbl[input.sel - 1][ind], sel_txt[input.sel - 1][ind]);
//		d.setEnabled(true);
//	}

	static void showDynaTch(Dynakey d) {
		// d.setLineTop(sel_tbl[input.sel - 1][d.getIndex()]);
		// d.setLineBottom(sel_txt[input.sel - 1][d.getIndex()]);
		// d.setEnabled(true);
		d.setLineTop(sel_tbl[input.sel - 1][d.getIndex()]);
		d.setLineBottom(sel_txt[input.sel - 1][d.getIndex()]);
		d.setEnabled(sel_tbl[input.sel - 1][d.getIndex()] != null);
	}

	// EMEA-00046-DSA#A END
	/***************************************************************************
	 * show short instructions from S_PLUSIN.DAT
	 *
	 * @param id
	 *            first three characters of access key
	 * @param nbr
	 *            four last digits of file access key
	 ***************************************************************************/
	public static void showShort(String id, int nbr) {

		String txt1 = null, txt2 = null;

		if (lSIN.find(id + editNum(nbr, 4)) > 0) {
			txt1 = lSIN.skip(8).scan(40);
			txt2 = lSIN.scan(40);
		}
		GdPos.panel.dspShort(0, txt1);
		GdPos.panel.dspShort(1, txt2);

	}
	/***************************************************************************
	 * show versions and instructions above electronic journal
	 ***************************************************************************/
	public static int showHelp() {
		int ind, line = 8;

		while (line > 4) {
			ind = --line - 4;
			stsLine.init(vrs_tbl[ind]).upto(20, editVersion(version[ind], ind < 2));
			GdPos.panel.dspShopper(line, stsLine.toString());
		}
		if (!GdPos.panel.sinArea[0].isShowing()) {
			GdPos.panel.dspShopper(1, GdPos.panel.sinArea[0].getText());
			GdPos.panel.dspShopper(2, GdPos.panel.sinArea[1].getText());
			GdPos.panel.dspShopper(3, editTxt("[" + dspSins + "]", 42));
		} else
			while (--line > 0)
				GdPos.panel.dspShopper(line, null);
		GdPos.panel.dspShopper(0, null);
		return 0;
	}

	/***************************************************************************
	 * show alerting indication for negative sales amounts
	 *
	 * @param b
	 *            true=negative, false=no indication
	 ***************************************************************************/
	public static void showMinus(boolean b) {
		GdPos.panel.dspArea[1].setAlerted(b);
	}

	/***************************************************************************
	 * show customer properties above electronic journal
	 ***************************************************************************/
	public static void showShopper() {
		LinIo info = new LinIo("INF", 0, 42);

		GdPos.panel.dspShopper(4, null);
		info.init(Mnemo.getText(54)).upto(22, editNum(cus.getSpec(), 2) + '/' + editNum(cus.getBranch(), 2))
				.onto(24, Mnemo.getText(20)).push(editRate(cus.getRate()));
		GdPos.panel.dspShopper(5, info.toString());
		info.init(Mnemo.getText(52)).upto(22, editMoney(0, cus.getLimchk())).onto(24, Mnemo.getText(50))
				.push(editRate(cus.getDscnt()));
		GdPos.panel.dspShopper(6, info.toString());
		info.init(Mnemo.getText(53)).upto(22, editMoney(0, cus.getLimcha())).onto(24, Mnemo.getText(51))
				.push(editRate(cus.getExtra()));
		GdPos.panel.dspShopper(7, info.toString());
		GdPos.panel.dspShopper(3, cus.getCity());
		GdPos.panel.dspShopper(2, cus.getAdrs());
		GdPos.panel.dspShopper(1, cus.getNam2());
		GdPos.panel.dspShopper(0, cus.getName());
	}

	/***************************************************************************
	 * show transaction balance (with currency toggle)
	 *
	 * @param cmd
	 *            0=stop, >0=text number to start, <0=toggle on
	 ***************************************************************************/
	public static void showTotal(int cmd) {
		int ind = (options[O_DWide] & 0x40) > 0 ? tnd_tbl[K_AltCur] : 0;
		long tmp_bal = tra.bal; // TSC-MOD2014-AMZ#ADD
		if (cmd > 0) /* start */
		{
			// TSC-MOD2014-AMZ#BEG
			if (GdTsc.isRoundingEnabled()) {
				tra.bal = GdTsc.roundDown(tra.bal, tnd[0].coin);
			}
			// TSC-MOD2014-AMZ#END
			if (cmd == 24)
				if ((tra.spf3 & 1) > 0)
					cmd = 55;
			mon.tot_txt = Mnemo.getText(cmd);
			long balance = tra.bal;
			if (cmd == 27 && tnd[1].customerFavour) {
				balance = tnd[1].roundUp(tra.bal, tnd[1].coin) * tnd[1].coin;
			}
			dspLine.init(mon.tot_txt).upto(20, editMoney(0, balance));
			cusLine.init(tnd[0].symbol).upto(20, editMoney(0, balance)).show(11);
			if (tra.bal != 0)
				showMinus(tra.bal < 0);
		}
		if (cmd == 0) /* stop */
		{
			mon.total = ERROR;
			return;
		}
		if (ind > 0)
			mon.total = mon.total + 1 & 1;
		if (mon.total == 0) {
			cusLine.init(tnd[ind].symbol).upto(20, editMoney(ind, tnd[ind].hc2fc(tra.bal)));
			if (cmd > 0)
				cusLine.show(0);
		} else
			cusLine.init(mon.tot_txt);
		cusLine.show(10);
		// TSC-MOD2014-AMZ#BEG
		if (GdTsc.isRoundingEnabled()) {
			tra.bal = tmp_bal;
		}
		// TSC-MOD2014-AMZ#END
	}

	/***************************************************************************
	 * verify customer id and salesperson enrolment
	 *
	 * @param mask
	 *            0x10=check for customer, 0x01=check for salesperson
	 * @return 0=ok, >0=error index
	 ***************************************************************************/
	public static int pre_valid(int mask) {
		int opt = options[O_SLMon] & mask;
		if (tra.stat == 0)
			if (opt > 15) {
				return 44;
			}
		if (tra.slm < 1)
			if ((opt & 15) > 0)
				return 36;
		return 0;
	}

	/***************************************************************************
	 * adapt dynakey state to customer/salesperson enforcement
	 *
	 * @param nbr
	 *            default dynakey state
	 * @return conditionally modified state
	 ***************************************************************************/
	public static int get_state(int nbr) {
		if (nbr == 3) {
			if (pre_valid(0x10) > 0)
				return 7; /* customer required */
			if (pre_valid(0x01) > 0)
				return 1; /* salesman required */
		}
		return nbr;
	}

	/***************************************************************************
	 * evaluation of ic/sc with potential authorization
	 *
	 * @param ic
	 *            itemcode (first 2 digits of access key to S_REG)
	 * @param sc
	 *            subcode (last 2 digits of access key to S_REG)
	 * @return 0=ok, >0=error index
	 ***************************************************************************/
	public static int sc_checks(int ic, int sc) {
		int rec = reg.find(ic, sc);
		if (rec == 0)
			return 7;
		lREG.read(rec, lREG.LOCAL);
		if ((lREG.tflg & 0x10) == 0)
			return 0;
		if (ctl.ckr_nbr > 799) {
			if ((input.lck & 0x04) > 0)
				return 0;
		}
		stsLine.init(lREG.text).show(2);
		return GdSigns.chk_autho(Mnemo.getInfo(38));
	}

	/***************************************************************************
	 * evaluation of bonus points
	 *
	 * @param items
	 *            number of points
	 * @return points < limit or zero
	 ***************************************************************************/
	public static int pts_valid(int items) {
		int lim = 100000;

		if (items < lim && items > -lim)
			return items;
		stsLine.init(Mnemo.getText(63)).upto(20, editInt(items)).show(2);
		GdPos.panel.clearLink(Mnemo.getInfo(34), 0x81);
		return 0;
	}

	/***************************************************************************
	 * evaluation of halo / lalo
	 *
	 * @param halo
	 *            halo in high-order byte, lalo in low-order byte
	 * @return true=out of range
	 ***************************************************************************/
	public static boolean chk_halos(int halo, int value) {
		int lalo = halo & 0xff;

		if (tra.res > 0)
			return false;
		halo >>= 8;
		if (halo == 0 || value <= limitBy(halo))
			if (lalo == 0 || value >= limitBy(lalo))
				return false;
		return GdPos.panel.clearLink(Mnemo.getInfo(46), 0x23) < 2;
	}

	/***************************************************************************
	 * check if drawer is owned after PoS restart
	 *
	 * @param ckr
	 *            cashier willing to open
	 * @return true=drawer belongs to someone else
	 ***************************************************************************/
	public static boolean chk_reboot(int ckr) {
		if (ckr < 1 || ckr >= 800)
			return false;
		if ((ctl.ckr = lCTL.find(ckr)) < 1)
			lCTL.error(new IOException("inconsistent with LAN"), true);
		return lCTL.sts == 1;
	}

	/***************************************************************************
	 * check if card/label was read when mandatory
	 *
	 * @param mask
	 *            card type (0x01=customer, 0x02=employee, 0x04=trans.id, 0x08=cashier, 0x10=creditcard, 0x20=bankcard,
	 *            0x40=charge account, 0x80=tender coupon)
	 * @return 0=ok, >0=error index
	 ***************************************************************************/
	public static int forceCard(int mask) {
		if (input.key > 0xff)
			return 0;
		if ((input.lck & 0x04) > 0)
			return 0;
		if ((options[O_xCaRd] & mask) == 0)
			return 0;
		return (input.optAuth & 2) > 0 ? 5 : 1;
	}
}
