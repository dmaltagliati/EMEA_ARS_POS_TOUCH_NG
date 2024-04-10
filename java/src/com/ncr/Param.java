package com.ncr;

import com.ncr.ecommerce.ECommerce;
import com.ncr.eft.EftPlugin;
import com.ncr.gpe.PosGPE;
import com.ncr.ssco.communication.manager.SscoPosManager;
import org.apache.log4j.Logger;

abstract class Param extends Table {
	private static int msrNewTrack = 0; // TAMI-ENH-NEWTRACK-CUST-CGA#A
	private static final Logger logger = Logger.getLogger(Param.class);

	static void readTXT() {
		int ind, rec = 0;

		bank_txt = new String[bank_txt.length];
		head_txt = new String[head_txt.length];
		mess_txt = new String[mess_txt.length];
		spec_txt = new String[spec_txt.length];
		xtax_txt = new String[xtax_txt.length];
		euro_txt = new String[euro_txt.length];
		note_txt = new String[note_txt.length];
		offl_txt = new String[offl_txt.length];
		save_txt = new String[save_txt.length];
		mdac_txt = new String[mdac_txt.length];
		zmsg_txt = new String[zmsg_txt.length];
		zqrc_txt = new String[zqrc_txt.length];
		//NCRMEA-2022-002
		gcer_txt = new String[gcer_txt.length];

		DatIo lPAR = new DatIo("TXT", 6, 48);
		lPAR.open(null, "P_REG" + lPAR.id + ".DAT", 0);
		while (lPAR.read(++rec) > 0)
			try {
				String key = lPAR.scan(4);
				if (lPAR.pb.charAt(lPAR.index) == '.')
					continue;
				ind = lPAR.scanHex(1);
				String txt = lPAR.pb.substring(lPAR.scan(':').index);

				if (key.equals("BANK"))
					bank_txt[ind] = txt;
				if (key.equals("CHDR"))
					head_txt[ind] = txt;
				if (key.equals("CMSG"))
					mess_txt[ind] = txt;
				if (key.equals("CSPC"))
					spec_txt[ind] = txt;
				if (key.equals("CTAX"))
					xtax_txt[ind] = txt;
				if (key.equals("EURO"))
					euro_txt[ind] = txt;
				if (key.equals("NOTE"))
					note_txt[ind] = txt;
				if (key.equals("NEOF"))
					offl_txt[ind] = txt;
				if (key.equals("SAVE"))
					save_txt[ind] = txt;
				if (key.equals("TEXT"))
					mdac_txt[ind] = txt;
				if (key.equals("ZMSG"))
					zmsg_txt[ind] = txt;
				if (key.equals(("ZQRC")))
					zqrc_txt[ind] = txt;
				if (key.startsWith("HDR") && Character.isDigit(key.charAt(3))) {
					int hdr = key.charAt(3) - '0';
					specialHeader[hdr][ind] = txt;
				}
				//NCRMEA-2022-002
				if (key.equals(("GCER")))
					gcer_txt[ind] = txt;
			} catch (Exception e) {
				lPAR.error(e, false);
			}
		lPAR.close();
	}

	public static int getNewMSR_Track() {
		return msrNewTrack;
	}

	static void init() {
		int dpt_no, rec = 0;

		eftPluginManager.readExtendedTender(); // AMZ-2017-002#ADD
		ECommerce.loadChargeFile();  //AMAZON-COMM-CGA#A

		tblInit();
		readTXT();
		lDBL.open(null, "P_REGDBL.DAT", 0);

		DatIo lPAR = new DatIo("PAR", 6, 48);
		lPAR.open(null, "P_REG" + lPAR.id + ".DAT", 1);
		while (lPAR.read(++rec) > 0)
			try {
				String key = lPAR.scan(4);
				if (lPAR.pb.charAt(lPAR.index) == '.')
					continue;
				int fld = 0, ind = lPAR.scanHex(1);
				String txt = lPAR.pb.substring(lPAR.scan(':').index);
				// TAMI-ENH-20140526-SBE#A BEG
				if (key.startsWith("EFT")) {
					eftPluginManager.loadPluginParameters(key, ind, txt);
				}
				// TAMI-ENH-20140526-SBE#A END

				if (key.equals("CMNT"))
					mnt_line = txt;
				if (key.equals("CCHK"))
					chk_line = txt;
				if (key.equals("CCPY"))
					cpy_line = txt;
				if (key.equals("CECU"))
					ecu_line = txt;
				if (key.equals("CIHT"))
					iht_line = txt;
				if (key.equals("CINQ"))
					inq_line = txt;
				if (key.equals("CTLR"))
					trl_line = txt;
				if (key.equals("FSOP"))
					fso_line = txt;
				if (key.equals("DABC"))
					kbd_alpha[ind] = txt;
				if (key.equals("VIEW"))
					view_txt[ind] = txt;

				if (key.equals("CAGE")) {
					while (fld < 10) {
						ckr_age[fld] = lPAR.scanNum(2);
						cus_age[fld++] = lPAR.scanNum(2);
					}
				}
				if (key.equals("COPT")) {
					while (fld < 20) {
						options[ind * 20 + fld++] = lPAR.scanHex(2);
					}
				}
				if (key.equals("CCSH")) {
					while (fld < 5) {
						csh_tbl[ind * 5 + fld++] = lPAR.scanNum(8);
					}
				}
				if (key.equals("CSID")) {
					TableRbt ptr = rbt[ind];
					ptr.text = lPAR.scan(20);
					ptr.rate_empl = lPAR.scan(':').scanNum(4);
					ptr.rate_cust = lPAR.scan(':').scanNum(4);
					ptr.rate_item = lPAR.scan(':').scanNum(4);
					ptr.rate_ival = lPAR.scan(':').scanNum(4);
				}
				if (key.equals("TARE")) {
					while (fld < 10) {
						tare_tbl[ind * 10 + fld++] = lPAR.scanNum(4);
					}
				}
				if (key.equals("TEXS")) {
					while (fld < 5) {
						lPAR.index = lPAR.fixSize + fld * 8;
						MsgLines ptr = mat[fld++];
						ptr.mode = lPAR.scanNum(1);
						ptr.code = lPAR.scanNum(2);
						ptr.logo = lPAR.scan();
						ptr.line = lPAR.scanHex(1);
						ptr.last = lPAR.scan('/').scanHex(1);
						ptr.flag = lPAR.scan();
						if ((ptr.last += ptr.line) > 16)
							ptr.last = 0;
					}
				}
				if (key.equals("CSLP")) {
					while (fld < 5) {
						SlpLines ptr = slp[ind * 5 + fld++];
						ptr.code = lPAR.scanNum(2);
						ptr.logo = lPAR.scan();
						ptr.top = lPAR.scanNum(2);
						ptr.end = lPAR.scanNum(2);
						ptr.flag = lPAR.scan();
					}
				}
				if (key.equals("DKEY")) {
					while (fld < 4)
						key_txt[fld++] = lPAR.scan(10);
				}
				if (key.equals("DNBR")) {
					while (fld < 5) {
						chk_nbr[ind * 5 + fld++] = lPAR.scan(8);
					}
				}
				if (key.startsWith("DT")) {
					int x = Integer.parseInt(key.substring(2));
					while (fld < 2) {
						CshDenom ptr = tnd[x].dnom[ind * 2 + fld++];
						ptr.value = lPAR.scanNum(8);
						ptr.text = lPAR.scan(12);
					}
				}
				if (key.equals("DVRS")) {
					while (fld < 2) {
						vrs_tbl[ind * 2 + fld++] = lPAR.scan(12);
						lPAR.skip(8);
					}
				}
				if (key.equals("CMNY")) {
					while (fld < 20) {
						tnd_tbl[fld++] = lPAR.scanNum(2);
					}
				}
				//WINEPTS-CGA#A BEG
				if (key.equals("GPE0")) {
					PosGPE.loadGPEParameters((lPAR.scan(lPAR.dataLen() - lPAR.fixSize)), ind);
				}

				//KEPT0:000 002 085 086 0000000000000000000000000000
				if (key.equals("KEPT")) {
					while (fld < 13) {
						try {
							PosGPE.preselect[(ind + 1) * fld++] = lPAR.scanNum(3);
						} catch (NumberFormatException e) {
						}
					}
				}
				//WINEPTS-CGA#A END
				if (key.equals("PRES"))
					if (ind-- > 0) {
						if (txt.startsWith("DYNA"))
							dir_tbl[ind] = -3;
						if (txt.startsWith("DESK"))
							dir_tbl[ind] = -2;
						if (txt.startsWith("LIST"))
							dir_tbl[ind] = -1;
						try {
							dir_tbl[ind] = lPAR.scanKey(4);
						} catch (NumberFormatException e) {
						}
						plu_tbl[ind] = lPAR.scan(':').scan(16);
						plu_txt[ind] = lPAR.scan(':').scan(18);
					}
				if (key.startsWith("PD"))
					if (ind < 8) {
						try {
							fld = Integer.parseInt(key.substring(2));
						} catch (NumberFormatException e) {
						}
						if (fld < 16)
							if (fld-- > 0) {
								sel_tbl[fld][ind] = lPAR.skip(5).scan(16);
								sel_txt[fld][ind] = lPAR.skip(1).scan(18);
							}
					}
				if (key.equals("EANX")) {
					while (fld < 2)
						ean_16spec[ind * 2 + fld++] = lPAR.scan(20);
				}
				if (key.equals("MSRX")) {
					while (fld < 2)
						msr_20spec[ind * 2 + fld++] = lPAR.scan(20);
				}

				// TAMI-ENH-NEWTRACK-CUST-CGA#A BEG
				if (key.equals("MSRN")) {
					msrNewTrack = lPAR.scanNum(2);
				}
				// TAMI-ENH-NEWTRACK-CUST-CGA#A END

				// SARAWAT-ENH-20150507-CGA#A BEG
				if (key.equals("PSAR")) {
					GdSarawat.getInstance().loadCapillaryParams(txt);
				}
				// SARAWAT-ENH-20150507-CGA#A END

				// CHKINPUT-CGA#A BEG
				if (key.equals("MEAP")) {
					ItemAuthManager.getInstance().setEnabled(Integer.parseInt(txt.substring(0, 2)));
					ReceiptPrintManager.getInstance().loadParams(txt.substring(2, 12));
				}
				// CHKINPUT-CGA#A END

				if (key.equals("SCPP")) {
					SurchargeManager.getInstance().loadSCPParams(txt);
				}
				if (key.equals("DONP")) {
					DonationManager.getInstance().loadDONParams(txt);
				}

				if (key.equals("CEOC") && ctl.srv_nbr != 0 || key.equals("CEOD") && ctl.srv_nbr == 0) {
					while (fld < 10) {
						EodTypes ptr = eod[fld++];
						ptr.sel = lPAR.scan();
						ptr.type = lPAR.scanNum(1);
						ptr.ac = lPAR.scanNum(2);
					}
				}

				if (key.equals("PSHP")) {
					GdPsh.getInstance().readPregpar(txt, ind);
				}
				if (key.equals("SAFP")) {
					GdSaf.readPregpar(txt, ind);
				}
				if (key.equals("NREG")) {
					while (fld < 10) {
						note_tbl[fld++] = lPAR.skip().scanKey(3);
					}
				}
				// SARAWAT-ENH-20150507-CGA#A BEG
				if (key.equals("DYKY")) { // load com.ncr.gui.Dynakey configuration
					for (fld = 0; fld < 8; fld++) {
						ConIo.dynas[ind][fld] = lPAR.scanHex(2);
					}
				}
				if (key.equals("DYTX")) { // load com.ncr.gui.Dynakey Description
					for (fld = 0; fld < 8; fld++) {
						//ConIo.rules[ind][fld] = lPAR.scan(3); //TNDMORE-CGA#D
						ConIo.rules[ind][fld] = lPAR.scan(4); //TNDMORE-CGA#A
					}
				}
				// SARAWAT-ENH-20150507-CGA#A END

                //VERIFONE-20160201-CGA#A BEG
                if (key.equals("VERI")) {
					eftPluginManager.loadPluginParameters("EFT" + EftPlugin.MARSHALL_TENDER_ID, ind, txt);
                }
                //VERIFONE-20160201-CGA#A END
				if (key.equals("SSCO")) {
					SscoPosManager.getInstance().loadSSCOParameters(
							(lPAR.scan(lPAR.dataLen() - lPAR.fixSize)), ind);
				}
				// TSC-MOD2014-AMZ#BEG
				if (key.equals("STND")) {
					GdTsc.readSTND(txt);
				}
				if (key.equals("BTND")) {
					BlackList.readBTND(txt);
				}
				if (key.equals("BTNP")) {
					BlackList.readBTNP(txt);
				}
				if (key.startsWith("BTM")) {
					int x = Integer.parseInt(key.substring(3));
					BlackList.readBTM(txt, ind, x);
				}
				if (key.equals("VOOP")) {
					while (fld < 20) {
						Voucher.readVOOP(fld++, lPAR.scanHex(2));
					}
				}
				if (key.equals("CNOP")) {
					while (fld < 20) {
						CreditNote.readCNOP(fld++, lPAR.scanHex(2));
					}
				}
				if (key.equals("CNTX")) {
					CreditNote.readCNTX(ind, txt);
				}
				if (key.equals("PTSC")) {
					GdTsc.readPTSC(txt);
				}
				// TSC-MOD2014-AMZ#END

			} catch (Exception e) {
				lPAR.error(e, true);
			}
		lPAR.close();

		lPAR = new DatIo("KEY", 6, 48);
		lPAR.open(null, "P_REG" + lPAR.id + ".DAT", rec = 0, true);
		while (lPAR.read(++rec) > 0)
			try {
				String key = lPAR.scan(4);
				if (!key.equals("FKEY"))
					continue;
				if (lPAR.pb.charAt(lPAR.index) == '.')
					continue;
				int fld = 0, ind = lPAR.scanHex(1) << 4;
				for (lPAR.scan(':'); fld < 16; fld++) {
					ConIo.table[ind + fld] = lPAR.scanHex(2);
				}
			} catch (Exception e) {
				lPAR.error(e, true);
			}
		lPAR.close();

		lPAR = new DatIo("TND", 2, 128);
		lPAR.open(null, "P_REG" + lPAR.id + ".DAT", rec = 0, true);
		while (lPAR.read(++rec) > 0)
			try {
				if (lPAR.pb.charAt(1) < '0')
					continue;
				TndMedia ptr = tnd[lPAR.scanNum(2)];
				ptr.setType(lPAR.scan(':').scan());
				logger.info("ptr.type: " + ptr.getType());

				ptr.tx20 = lPAR.scan(':').scan(20);
				ptr.flag = lPAR.scan(':').scanHex(2);
				ptr.till = lPAR.scan(':').scanNum(1);
				ptr.flom = lPAR.scan(':').scanHex(2);
				ptr.flg2 = lPAR.scan(':').scanHex(2);
				ptr.coin = lPAR.scan(':').scanNum(8);
				// TSC-ENH2014-3-AMZ#BEG
				if(ptr.coin > 90000000){
					ptr.coin -= 90000000;
					ptr.customerFavour = true;
				}else{
					ptr.customerFavour = false;
				}
				// TSC-ENH2014-3-AMZ#END
				for (int fld = 0; fld < ptr.limit.length; fld++)
					ptr.limit[fld] = lPAR.scan(':').scanNum(8);
			} catch (Exception e) {
				lPAR.error(e, true);
			}
		lPAR.close();

		lPAR = new DatIo("MOD", 0, 42);
		lPAR.open(null, "P_REG" + lPAR.id + ".DAT", rec = 0, true);
		while (lPAR.read(++rec) > 0)
			try {
				if (lPAR.pb.charAt(11) != ']' && lPAR.pb.charAt(12) != ']')
					continue;
				int hundreds = lPAR.pb.charAt(11) == ']' ? 0 : 1;
				String key = lPAR.skip(3).scan(5);
				int ind = lPAR.scan('[').scanNum(2 + hundreds);
				lPAR.skip(5 - hundreds).scan('"');
				if (key.equals("ERROR"))
					Mnemo.setInfo(ind, lPAR.scan(20));
				if (key.equals("MENUS"))
					Mnemo.setMenu(ind, lPAR.scan(20));
				if (key.equals("MNEMO"))
					Mnemo.setText(ind, lPAR.scan(12));
				if (key.equals("SUPER"))
					Mnemo.setHead(ind, lPAR.scan(12));
				if (key.equals("ORDER"))
					Mnemo.setHint(ind, lPAR.scan(20));
				if (key.equals("DIAGS"))
					Mnemo.setDiag(ind, lPAR.scan(20));
			} catch (Exception e) {
				lPAR.error(e, true);
			}
		lPAR.close();

		for (rec = 0; rec < dir_tbl.length; rec++) {
			if ((dpt_no = dir_tbl[rec]) > 0) {
				if (GdPrice.src_dpt(dpt_no) > 0)
					dlu.text = Mnemo.getText(6) + editKey(dpt_no, 4) + " ???";
				dir_txt[rec] = dlu.text.substring(0, 18);
			}
		}
	}
}
