package com.ncr;

import com.ncr.zatca.ZatcaManager;

import com.ncr.ecommerce.ECommerceManager;

import java.text.*;
import java.util.ArrayList;
import java.util.List;

/*******************************************************************
 * local/remote file definitions and memory table access
 *******************************************************************/
public abstract class Table extends Struc {
	public static LocalACT lACT = new LocalACT("ACT", 1);
	public static LocalDPT lDPT = new LocalDPT("DPT", S_MOD);
	public static LocalREG lREG = new LocalREG("REG", 1);
	public static LocalSLM lSLM = new LocalSLM("SLM", S_MOD);

	public static LocalCTL lCTL = new LocalCTL("CTL");
	public static LocalLAN lLAN = new LocalLAN("LAN", S_CKR);
	public static LocalPOT lPOT = new LocalPOT("POT", S_CKR, lREG.block[0]);
	public static LocalPOS lPOS = new LocalPOS("POS");
	public static RmoteBIL rBIL = new RmoteBIL("BIL");
	public static RmoteCLS rCLS = new RmoteCLS("CLS");
	public static RmoteHCA rHCA = new RmoteHCA("HCA");
	public static RmoteNEW rNEW = new RmoteNEW("NEW");
	public static RmoteSAR rSAR = new RmoteSAR("SAR");
	public static RmoteVNU rVNU = new RmoteVNU("VNU");

	public static HshIo lPLU = new HshIo("PLU", 16, 80);
	public static HshIo lCLU = new HshIo("CLU", 16, 80);
	public static HshIo lGLU = new HshIo("GLU", 16, 80);
	public static LinIo rMNT = new LinIo("MNT", 0, 88);

	public static BinIo lALU = new BinIo("ACC", 4, 50);
	public static BinIo lDLU = new BinIo("DPT", 4, 54);
	public static BinIo lSLU = new BinIo("SLM", 4, 40);
	public static BinIo lRLU = new BinIo("RBT", 16, 80);
	//ECOMMERCE-SSAM#C BEG
	public static BinIo lRCD = new BinIo("RCD", 4, 30, !ECommerceManager.getInstance().isEnabled());
	//ECOMMERCE-SSAM#C BEG
	public static BinIo lREF = new BinIo("REF", 9, 54);
	public static BinIo lCIN = new BinIo("CIN", 16, 80);
	public static BinIo lCGR = new BinIo("CGR", 16, 80);
	public static BinIo lPWD = new BinIo("PWD", 4, 6);
	public static BinIo lQLU = new BinIo("QUA", 16, 80);
	public static BinIo lSIN = new BinIo("SIN", 7, 90);
	public static BinIo lSET = new BinIo("SET", 9, 54);
	public static BinIo lDBL = new BinIo("DBL", 7, 50);

	public static DatIo lBOX = new DatIo("BOX", 4, 38);
	public static DatIo lCRD = new DatIo("CRD", 19, 44);
	public static DatIo lDDQ = new DatIo("DDQ", 7, 40);

	public static DatIo lBOF = new DatIo("BOF", 0, 42);
	public static DatIo lTRA = new DatIo("TRA", 0, 80);
	public static SeqIo lIDC = new SeqIo("IDC", 0, 80);
	public static SeqIo lJRN = new SeqIo("JRN", 0, 44);
	public static SeqIo lGPO = new SeqIo("GPO", 0, 44);
	public static SeqIo lDTL = new SeqIo("DTL", 0, 34);

	/*
	 * Author: Soukaina
	 * GAZT Integration
	 * Start */
	//static SeqIo lTRALA = new SeqIo("TRALA", 0, 80);
	static int oldS = 0;
	static int newS = 0;
	static List<String> jrnLA = new ArrayList<String>();
	/* END */

	static DatIo lFile[] = { lIDC, lJRN, lDTL, lGPO, lSLM, lDPT, lACT, lPOT, lREG };

	// EMEA-UPB-DMA#A BEG
	static BinIo lUPB = new BinIo("UPB", 16, 80);
	// EMEA-UPB-DMA#A END
	public static TableSls act = new TableSls(lACT);
	public static TableDpt dpt = new TableDpt(lDPT);
	public static TableReg reg = new TableReg(lREG);
	public static TableSls slm = new TableSls(lSLM);
	public static TableRbt[] rbt = new TableRbt[S_RBT];
	public static TableMmt[] mmt = new TableMmt[S_MMT];

	/***************************************************************************
	 * initialize parameter and sales tables in memory
	 ***************************************************************************/
	public static void tblInit() {
		int rec;

		for (rec = tnd.length; rec > 0; tnd[--rec] = new TndMedia())
			;
		for (rec = mat.length; rec > 0; mat[--rec] = new MsgLines())
			;
		for (rec = slp.length; rec > 0; slp[--rec] = new SlpLines())
			;
		for (rec = eod.length; rec > 0; eod[--rec] = new EodTypes())
			;
		for (rec = rbt.length; rec > 0; rbt[--rec] = new TableRbt())
			;
		for (rec = vat.length; rec > 0; vat[--rec] = new TaxRates())
			;

		act.init();
		dpt.init();
		slm.init();
		tnd[0].dec = NumberFormat.getCurrencyInstance().getMaximumFractionDigits();
		while (lREG.read(++rec, lREG.LOCAL) > 0) {
			reg.key[rec - 1] = lREG.key;
			int ic = lREG.key / 100, sc = lREG.key % 100;
			if (ic == 1 && sc == 1) {
				version[sc] = lREG.rate;
				ctl.gross = lREG.block[0].total;
			}
			if (rec > lPOT.getSize()) {
				if (ic < 10 || sc < 8)
					lREG.block[0].reset();
				lPOT.key = lREG.key;
				lPOT.write();
			}
			if (ic > 9 && sc > 0)
				tnd[ic - 10].init(sc, lREG);
			if (ic != 7)
				continue;
			if (sc > 0 && sc < 9) {
				vat[sc - 1].rate = lREG.rate;
				vat[sc - 1].text = lREG.text;
				if (lREG.text.charAt(5) == '*')
					vat[sc - 1].flat = 1;
			}
			if (sc > 10 && sc < 19) {
				if (vat[sc - 11].flat > 0)
					vat[sc - 11].text = lREG.text;
			}
		}
		for (rec = 0; lPOS.read(++rec) > 0; posWrite(rec, 0, 0L))
			;
		if (lLAN.getSize() < 1) {
			lLAN.write();
			lLAN.key = ctl.reg_nbr;
			lLAN.org = version[1];
			lLAN.dat = ctl.zero;
			lLAN.rewrite(1);
			lLAN.sync();
			lPOT.sync();
		}
        ExtResume.consolidateTra(); // AMZ-2017#ADD
		lTRA.open("data", "S_TRA" + REG + ".DAT", 2);
		lDDQ.open(null, "P_REG" + lDDQ.id + ".DAT", 0);
		UpSet.init();
	}

	/***************************************************************************
	 * search for active checker in POT using local LAN
	 *
	 * @param nbr
	 *            checker number (000-799)
	 * @return index of checker or first free slot, -1=full
	 ***************************************************************************/
	public static int ckrBlock(int nbr) {
		lLAN.read(1, lLAN.LOCAL);
		for (int ind = 0; ind < lLAN.tbl.length; ind++) {
			if (lLAN.tbl[ind] == nbr)
				return ind;
			if (lLAN.tbl[ind] < 1)
				return ind;
		}
		return lLAN.ERROR;
	}

	/***************************************************************************
	 * access combinations of REG header and POT data
	 *
	 * @param rec
	 *            relative record number (1 <= rec <= records in file)
	 * @param sel
	 *            selection (>0=terminal/group, 0=all terminals, -1=local)
	 * @return record size - 2 (0 = end of file)
	 ***************************************************************************/
	public static int ckrRead(int rec, int sel) {
		String id = lREG.id;
		lREG.id = lPOT.id;
		int sts = lREG.read(rec, sel);
		if (sel == lREG.LOCAL)
			if (sts > 0)
				lPOT.readSls(rec, lPOT.blk);
		lREG.id = id;
		return sts;
	}

	/***************************************************************************
	 * update financial report data in REG and POT
	 *
	 * @param rec
	 *            relative record number (1 <= rec <= records in file)
	 * @param sls
	 *            single sales total structure
	 ***************************************************************************/
	public static void ckrWrite(int rec, Sales sls) {
		sls.write(rec, 0, lREG);
		if (ctl.ckr_nbr < 800) {
			sls.write(rec, lPOT.blk, lPOT);
		}
	}

	/***************************************************************************
	 * update financial report data in CMOS and REG ic00
	 *
	 * @param sc
	 *            subcode 00 - 08
	 * @param trans
	 *            transaction count
	 * @param total
	 *            monitary amount
	 ***************************************************************************/
	public static void posWrite(int sc, int trans, long total) {
		lPOS.read(sc);
		lPOS.trans = (lPOS.trans + trans) % 10000;
		lPOS.total = (lPOS.total + total) % 10000000000L;
		if (sc == 1) {
			if (lPOS.trans < 1)
				lPOS.trans++;
			ctl.tran = lPOS.trans;
			ctl.uniqueId = "20" + ctl.date + editNum(ctl.sto_nbr, 3) + editNum(ctl.reg_nbr, 3) + editNum(ctl.tran, 4);
		}
		if (sc == 2)
			ctl.zero = lPOS.trans;
		lPOS.rewrite(sc);
		int blk = 0, rec = reg.find(0, sc);
		if (rec > 0) {
			lREG.readSls(rec, blk);
			lREG.block[blk].trans = lPOS.trans;
			lREG.block[blk].items = 0;
			lREG.block[blk].total = lPOS.total;
			lREG.writeSls(rec, blk);
		}
	}

	/***************************************************************************
	 * read control data of department not in DLU from DPT into department lookup structure (dlu), if dpt
	 ***************************************************************************/
	public static void getMemdpt(int rec) {
		lDPT.read(rec, lDPT.LOCAL);
		dlu.flag = lDPT.flag;
		dlu.sit = lDPT.sit;
		dlu.vat = lDPT.vat;
		dlu.cat = lDPT.cat;
		dlu.halo = lDPT.halo;
		dlu.flg2 = lDPT.flg2;
		dlu.ages = lDPT.ages;
		dlu.type = lDPT.type;
		dlu.text = lDPT.text;
		dlu.xtra = lDPT.xtra;
	}

	/***************************************************************************
	 * search for slip control data
	 *
	 * @param code
	 *            action code
	 * @return index into form definition table (slp), -1=not found
	 ***************************************************************************/
	public static int slpFind(int code) {
		int ind = slp.length;
		while (ind-- > 0)
			if (slp[ind].top > 0)
				if (slp[ind].code == code)
					break;
		return ind;
	}

	/***************************************************************************
	 * clear local totals at end of transaction
	 ***************************************************************************/
	public static void tblClear() {
		int nbr = 0, rec;
		act.reset();
		dpt.reset();
		reg.reset();
		slm.reset();
		for (rec = vat.length; rec > 0; vat[--rec].reset())
			;
		for (rec = rbt.length; rec > 0; rbt[--rec].reset())
			;
		for (rec = mmt.length; rec > 0; mmt[--rec] = new TableMmt())
			;

		for (ctl.alert = false; rec < tnd.length; rec++) {
			if (tnd[rec].limit[L_MaxDrw] > 0)
				ctl.alert |= tnd[rec].alert >= tnd[rec].limit[L_MaxDrw];
		}
		for (rec = 0; lTRA.read(++rec) > 0;) {
			char type = lTRA.pb.charAt(32);
			if (type == 'C') /* skip empl/cust % template */
				if (lTRA.pb.charAt(35) == '9')
					continue;
			if (type == 'u') {
				String ean = lTRA.pb.substring(43, 59);
				int i = WinUpb.getInstance().findUpbTra(ean, false);
				if (i >= 0 && tra.itemsVsUPB.get(i).isConfirmed()) {
					lTRA.pb = lTRA.pb.substring(0, lTRA.pb.length() - 1) + "0";
				}
			}
			lIDC.onto(0, lTRA.scan(28)).push(editNum(++nbr, 3));
			lIDC.push(lTRA.skip(3).scan(3)).push(editNum(tra.mode, 1));
			lIDC.push(lTRA.pb.substring(++lTRA.index));
			if (type == 'F')
				if (ctl.alert)
					lIDC.poke(38, '*');
			lIDC.write();
		}

		lLAN.read(1, lLAN.LOCAL);
		lLAN.ckr = ctl.ckr_nbr;
		lLAN.sts = tra.code;
		lLAN.idc = lIDC.getSize();
		lLAN.jrn = lJRN.getSize();
		if (cntLine.recno > 0)
			lLAN.mnt = cntLine.recno;
		lLAN.dtl = lDTL.getSize();
		lLAN.gpo = lGPO.getSize();
		lLAN.date = ctl.date;
		lLAN.time = ctl.time;
		lLAN.lan = ctl.lan;
		if (lLAN.ckr < 800)
			lLAN.tbl[lPOT.blk] = ctl.ckr_nbr;
		lLAN.rewrite(1);
		lLAN.sync();
		lCTL.sync();
		lIDC.sync();
		lGPO.sync();
		lJRN.sync();
		lTRA.close();
		lCIN.close();
		lCGR.close();
		lTRA.open("data", "S_TRA" + REG + ".DAT", 2);
		lDDQ.recno = 1;
		net.writeSls(0, 0, lLAN);
	}

	/***************************************************************************
	 * write local totals at end of transaction
	 ***************************************************************************/
	public static void tblWrite() {
		int rec = 0;
		Delta.control('H', ctl.time / 100 + ctl.date * 10000);
		while (rec < reg.key.length) {
			int ic = reg.key[rec] / 100, sc = reg.key[rec] % 100;
			Sales sls = reg.sales[rec++][0];
			if (ic == 1) {
				if (sc == 1) {
					ctl.gross += sls.total;
					posWrite(sc, 1, sls.total);
					ckrWrite(rec, sls);
					if (tra.mode > 1 && tra.mode < 9) {
						sls.set(-sls.items, -sls.total);
						ckrWrite(reg.find(ic, tra.mode), sls);
					}
					continue;
				}
			} else if (tra.mode > 1 && ic != 9)
				continue;
			if (sls.isZero())
				continue;
			ckrWrite(rec, sls);
			if (ic > 9) {
				if (sc > 3)
					continue;
				tnd[ic - 10].alert += sc > 2 ? -sls.total : sls.total;
			}
		}
		lREG.sync();
		lPOT.sync();

		if (tra.mode < 2 && ctl.ckr_nbr < 800) {
			for (rec = 0; rec < act.key.length; act.write(++rec))
				;
			for (rec = 0; rec < dpt.key.length; dpt.write(++rec))
				;
			for (rec = 0; rec < slm.key.length; slm.write(++rec))
				;
			lACT.sync();
			lDPT.sync();
			lSLM.sync();
		}
		Delta.control('F', lDTL.recno);
		tblClear();
	}

	/***************************************************************************
	 * add to hourly activity totals
	 *
	 * @param key
	 *            integer time (hhmm)
	 * @param items
	 *            item count
	 * @param total
	 *            monitary amount
	 ***************************************************************************/
	public static void accumAct(int key, int items, long total) {
		int ind = 0, rec = 0;
		key = keyValue(String.valueOf(key));
		while (ind < act.key.length) {
			if (act.key[ind++] < 0x2400) {
				if (key < act.key[ind - 1])
					break;
				rec = ind;
			}
		}
		act.addSales(rec, 0, items, total);
	}

	/***************************************************************************
	 * add to checker / register totals
	 *
	 * @param ic
	 *            item code
	 * @param sc
	 *            subcode
	 * @param items
	 *            item count
	 * @param total
	 *            monitary amount
	 ***************************************************************************/
	public static void accumReg(int ic, int sc, int items, long total) {
		int rec = reg.find(ic, sc);
		reg.addSales(rec, 0, items, total);
	}

	/***************************************************************************
	 * add to deparmtment totals using itm.dpt and itm.cat
	 *
	 * @param blk
	 *            total block index (0 - 2)
	 * @param items
	 *            item count
	 * @param total
	 *            monitary amount
	 ***************************************************************************/
	public static void accumDpt(int blk, int items, long total) {
		dpt.addSales(itm.dpt, blk, items, total);
		if (itm.cat > 50)
			accumReg(8, itm.cat, blk > 0 ? 0 : items, total);
	}

	/***************************************************************************
	 * add to salesmen totals using tra.slm
	 *
	 * @param blk
	 *            total block index (0 - 2)
	 * @param items
	 *            item count
	 * @param total
	 *            monitary amount
	 ***************************************************************************/
	public static void accumSlm(int blk, int items, long total) {
		slm.addSales(itm.slm, blk, items, total);
	}

	/***************************************************************************
	 * add to tax info block
	 *
	 * @param sc
	 *            basic subcode (00=tax, 10=sales, 20=deposit, 30=refund)
	 * @param ind
	 *            index into vat table (0-7)
	 * @param items
	 *            item count
	 * @param total
	 *            monitary amount
	 ***************************************************************************/
	public static void accumTax(int sc, int ind, int items, long total) {
		accumReg(7, sc + ind + 1, items, total);
	}

	/***************************************************************************
	 * accumulate to individual (itm.tnd) and total tender
	 *
	 * @param sc
	 *            subcode (1=sales, 2=loan, 3=pickup, 4=onhand, 5=float etc)
	 * @param items
	 *            item count
	 * @param total
	 *            monitary amount
	 ***************************************************************************/
	public static void accumTnd(int sc, int items, long total) {
		accumReg(10 + itm.tnd, sc, items, total);
		accumReg(10, sc, items, tnd[itm.tnd].fc2hc(total));
	}

	/***************************************************************************
	 * accumulate bonus points
	 *
	 * @param sc
	 *            subcode (1-8) of ic30
	 * @param items
	 *            item count
	 * @param total
	 *            monitary amount
	 ***************************************************************************/
	public static void accumPts(int sc, int items, long total) {
		accumReg(8, 30 + sc, items, total);
	}

	/***************************************************************************
	 * determination of subcodes
	 *
	 * @param mask
	 *            bit mask of preselections (1 byte)
	 * @return number of highest bit set in mask (8, 7, ... 1)
	 ***************************************************************************/
	public static int sc_value(int mask) {
		for (int ind = 1; mask > 0; ind++)
			if ((mask <<= 1) > 255)
				return ind;
		return 0;
	}

	/***************************************************************************
	 * determination of subcode for storage of bonuspoints
	 *
	 * @param ind
	 *            type of points (0=on item, 1=on total, 2=free, 3=redeemed)
	 * @return subcode for customers (1-4) or anonymous (5-8)
	 ***************************************************************************/
	public static int sc_points(int ind) {
		return (tra.spf2 > 0 ? 1 : 5) + ind;
	}
}
