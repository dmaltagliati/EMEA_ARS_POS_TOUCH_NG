package com.ncr.gui;

import java.awt.*;

// TODO MMS-R10 Valutare se possibile usare questa classe

public class BarCode extends Canvas {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1140265979893572848L;
	private int bars[];
	private String nmbr;

	private String ean13_mask[] = { "AAAAAA", "AABABB", "AABBAB", "AABBBA", "ABAABB", /* 1st digit = 0 - 4 */
			"ABBAAB", "ABBBAA", "ABABAB", "ABABBA", "ABBABA", /* 1st digit = 5 - 9 */
	};
	private String upc_E_mask[] = { "BBBAAA", "BBABAA", "BBAABA", "BBAAAB", "BABBAA", /* chk digit = 0 - 4 */
			"BAABBA", "BAAABB", "BABABA", "BABAAB", "BAABAB", /* chk digit = 5 - 9 */
	};
	private byte bar_code[][] = {

			/* A */ { 0x0D, 0x19, 0x13, 0x3D, 0x23, 0x31, 0x2F, 0x3B, 0x37, 0x0B },

			/* B */ { 0x27, 0x33, 0x1B, 0x21, 0x1D, 0x39, 0x05, 0x11, 0x09, 0x17 },

			/* C */ { 0x72, 0x66, 0x6C, 0x42, 0x5C, 0x4E, 0x50, 0x44, 0x48, 0x74 } };
	private int bar_ctrl[][] = {

			/* ean13 */
			{ 8 + 42 + 5 + 42 + 8, 7 }, /* upc A */{ 8 + 42 + 5 + 42 + 8, 1, 6, 11 }, /* 11-09 */{ 0 }, { 0 }, { 0 },
			/* ean 8 */{ 8 + 28 + 5 + 28 + 8, 0, 4 }, /* upc E */{ 8 + 42 + 3 + 0 + 9, 7 }, };

	private Dimension getCharSize() {
		Font f = getFont();
		FontMetrics fm = getFontMetrics(f);

		return new Dimension(fm.charWidth(' ') >> 2, f.getSize() + 2 >> 2);
	}

	public Dimension getPreferredSize() {
		Dimension d = getCharSize();

		d.width *= bars.length;
		d.height *= 18;
		return d;
	}

	public void paint(Graphics g) {
		Dimension chr = getCharSize(), d = getSize();
		int x = d.width - chr.width * bars.length >> 1;
		int y = d.height - chr.height * 18 >> 1;

		g.setColor(Color.white);
		g.fillRoundRect(x, y, chr.width * bars.length, chr.height * 18, chr.width << 2, chr.height << 1);
		g.setColor(getForeground());
		for (int ind = 0; ind < bars.length; ind++) {
			g.fillRect(x + ind * chr.width, y + chr.height, chr.width, chr.height * bars[ind] >> 2);
		}
		x += chr.width;
		y += chr.height * 17;
		for (int ind = 0; ind < nmbr.length(); ind++) {
			g.drawString(nmbr.substring(ind, ind + 1), x + chr.width * ind * 7, y);
		}
	}

	public void setBars(String ean) {
		int size = ean.length();

		if (size == 8) {
			if (ean.charAt(0) == '0') {
				size--;
			}
		} else {
			if (size < 12 || size > 13) {
				bars = new int[size * 7];
				nmbr = ean;
				return;
			}
		}
		char mask = 'A';
		StringBuffer sb = new StringBuffer(ean);
		int black = 56, ctrl[] = bar_ctrl[13 - size], dig, ind = 5;

		for (dig = ctrl.length; --dig > 0; sb.insert(ctrl[dig], ' ')) {
		}
		nmbr = sb.toString();
		bars = new int[ctrl[dig]];
		bars[ind] = bars[ind + 2] = black;
		ind += 3;
		for (dig = size & 1; dig < size; dig++) {
			if (dig < 7) {
				if (size == 13) {
					mask = ean13_mask[ean.charAt(0) - '0'].charAt(dig - 1);
				}
			}
			if (size == 7) {
				mask = upc_E_mask[ean.charAt(7) - '0'].charAt(dig - 1);
			}
			int code = bar_code[mask - 'A'][ean.charAt(dig) - '0'];
			int high = size == 12 && (dig == 0 || dig == 11) ? 0 : 4;

			for (int bit = 0x80; (bit >>= 1) > 0; ind++) {
				if ((code & bit) > 0) {
					bars[ind] = black - high;
				}
			}
			if (dig == (size - 1) >> 1) {
				mask = 'C';
				if (size > 7) {
					bars[ind + 1] = bars[ind + 3] = black;
					ind += 5;
				}
			}
		}
		if (size == 7) {
			bars[ind + 1] = black;
			ind += 3;
		}
		bars[ind] = bars[ind + 2] = black;
	}
}
