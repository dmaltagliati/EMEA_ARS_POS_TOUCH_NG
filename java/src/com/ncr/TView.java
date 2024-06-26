package com.ncr;

import java.util.*;

public abstract class TView extends Basis {
	private static Vector trxData = new Vector();
	private static int indexEC = 0;
	private static LinIo line = new LinIo("LIN", 0, 56);

	public static TrxData getLineItem(int ind) {
		return ind < getTrxData().size() ? (TrxData) getTrxData().elementAt(ind) : new TrxData(0, 0, true, -1, null);
	}

	public static int syncIndex(int ind) {
		if (ctl.view > 0) {
			if (GdPos.panel.trxView.list.sel == 0)
				return -1;
			TrxData td = getLineItem(GdPos.panel.trxView.list.sel - 1);
			ind = td.state == 1 ? td.index : -1;
		}
		return indexEC = ind;
	}

	public static void modify(int index, int state) {
		if (index < 0)
			return;
		for (int ind = getTrxData().size(); ind-- > 0;) {
			TrxData td = (TrxData) getTrxData().elementAt(ind);
			if (td.index == index)
				td.state = state;
		}
	}

	public static void select() {
		return ;
		//
//		if (getTrxData().size() > 0)
//			return;
//		GdPos.panel.trxCard.setPicture("READY");
//		if (GdPos.panel.trxCard.image != null)
//			GdPos.panel.pnlRoll.toFront(2);
	}

	public static void update() {
		int ind, sel = getTrxData().size();

		if (ctl.view > 0) {
			GdPos.panel.pnlRoll.toFront(1);
			select();
		}
		GdPos.panel.trxView.updateView(sel);
		//GdPos.panel.cid.trxView.updateView(sel);
		if (sel > 0) {
			TrxData td = (TrxData) getTrxData().elementAt(ind = sel - 1);
			while (td.index < 0) {
				if (--ind < 0)
					break;
				td = (TrxData) getTrxData().elementAt(ind);
				if (td.under)
					break;
				if (td.index >= 0)
					GdPos.panel.trxView.updateView(ind + 1);
			}
		}
	}

	/***************************************************************************
	 * append line item to trx view
	 *
	 * @param type
	 *            character record type S=sales C=credit, M=money, T=tender ' '=text, '>'=doublewide, '$'=loan/pickup
	 * @param flag
	 *            integer combination of flags x80=home currency in tags, x40=tender currency in tags x01=stick to prev,
	 *            x02=stick to next
	 ***************************************************************************/
	public static void append(char type, int flag, String text, String sqty, String sprc, String samt, String tags) {
		int state = 0, color = 0, index = -1;

		if (type == 'S') {
			if (itm.eXline.length() > 0)
				append(' ', 0x02, itm.eXline, "", "", "", "");
			if (itm.serial.length() > 0)
				append(' ', 0x02, Mnemo.getText(76), itm.serial, "", "", "");
			if (itm.redemptionDsc.length() > 0) // PSH-ENH-001-AMZ#ADD
				append(' ', 0x02, itm.redemptionDsc, "", "", "", ""); 	// PSH-ENH-001-AMZ#ADD -- Append the extended
																		// description after the item line
			if (itm.gCardDsc.length() > 0) // PSH-ENH-001-AMZ#ADD
				append(' ', 0x02, itm.gCardDsc, "", "", "", ""); 	// PSH-ENH-001-AMZ#ADD -- Append the extended
																	// description after the item line

			if (itm.prm > 0) {
				if ((itm.flag & F_WEIGHT) > 0)
					append(' ', 0x02, "", "*/*", tnd[0].symbol + '/' + GdScale.getUnit(), "", "");
				sqty = editDec(itm.dec, 3) + " " + itm.ptyp;
			} else if (itm.unit != 10)
				sqty += " x" + editDec(itm.unit, 1);
			if ((itm.spf1 & M_ERRCOR) > 0)
				state = 2;
			else if ((itm.stat & 2) == 0)
				state = 1;
			index = itm.index;
		}
		if (type == 'S' || type == 'C') {
			for (int bit = M_RETURN; bit > 0; bit >>= 1)
				if ((itm.spf1 & bit) > 0)
					color ^= 1;
			color++;
			tags = itm.mark + vat[itm.vat].text.substring(5, 6) + Mnemo.getText(58).substring(0, 1) + itm.sit;
		}
		if (type == 'M') {
			if (!itm.number.isEmpty())
				append(' ', 0x02, Mnemo.getText(GiftCardPluginManager.getInstance().isGiftCard(itm) ? 115 : 30) + itm.number, "", "", "", "");
			if (!itm.eXline.isEmpty())
				append(' ', 0x02, itm.eXline, "", "", "", "");
			color = state = (itm.spf1 & M_ERRCOR) > 0 ? 2 : 1;
			tags = itm.mark + "   ";
			index = itm.index;
		}
		if (type == 'T') {
			index = itm.index;
			color = state = itm.mark > ' ' ? 2 : 1;
			tags = itm.mark + tnd[0].symbol;
			if (tnd[itm.tnd].unit > 0) {
				sqty = tnd[itm.tnd].editXrate(true);
				sprc = editMoney(itm.tnd, Math.abs(itm.pos));
			}
		}
		if (type == '>')
			text = type + text;
		if ((flag & 0x40) > 0)
			tags = itm.mark + tnd[itm.tnd].symbol;
		if ((flag & 0x80) > 0)
			tags = " " + tnd[0].symbol;
		getLine().init(text).upto(31, sqty).upto(41, sprc).upto(52, samt).push(tags);
		if ((flag & 0x01) > 0) {
			((TrxData) getTrxData().lastElement()).under = false;
		}
		getTrxData().addElement(new TrxData(state, color, (flag & 0x02) == 0, index, getLine().toString()));
		if (state == 2)
			modify(getIndexEC(), 3);
		update();
	}

	public static void scroll(int vkey) {
		int sel = GdPos.panel.trxView.scroll(vkey);
		if (sel > 0)
			GdPos.panel.trxView.updateView(sel);
	}

	public static void clear() {
		getTrxData().removeAllElements();
		update();
	}

	public static Vector getTrxData() {
		return trxData;
	}

	public static void setTrxData(Vector trxData) {
		TView.trxData = trxData;
	}

	public static int getIndexEC() {
		return indexEC;
	}

	public static void setIndexEC(int indexEC) {
		TView.indexEC = indexEC;
	}

	public static LinIo getLine() {
		return line;
	}

	public static void setLine(LinIo line) {
		TView.line = line;
	}
}
