package com.ncr.gui;

/*
 * HeaderView.java
 *
 * Created on 13. Oktober 2005, 14:20
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

import com.ncr.Struc;

import java.awt.*;

/**
 * @author bs230003
 */
public class TransHeaderView extends Canvas {

	private static final long serialVersionUID = 6728245464333400437L;;

	// 21 32 41 52 56
	// 21 11 9 11 4
	// 123456789012345678901 12345678901 123456789 12345678901 1234

	// 123456789012345678901 12345678901 123456789 12345678901 1234
	private String labelText[] = { "Item Description    ", " Quantity  ", " Price   ", " Amount    ", " Tag" };
	private String noLabelText[] = { "                    ", "           ", "         ", "           ", "    " };
	private int labelLen[] = { 20, 11, 9, 11, 4 };
	private Color bgColor = null;
	private boolean isScrollbarEnabled = true;
	private boolean isCidMode = false;

	protected TransHeaderView() {
		bgColor = TransView.colorInactiveCaption;
	}

	protected TransHeaderView(boolean isScrollbar) {
		this();
		this.isScrollbarEnabled = isScrollbar;
		prepareHeaderLabel();
	}

	protected TransHeaderView(boolean isScrollbar, boolean isCidMode) {
		this();
		this.isScrollbarEnabled = isScrollbar;
		this.isCidMode = isCidMode;
		prepareHeaderLabel();
	}

	public Dimension getPreferredSize() {
		Dimension d = new Dimension(getParent().getSize().width, (getParent().getSize().height - 6) / 12);

		return d;
	}

	/**
	 * Reads the Headerlabel enties from P_REGPAR.DAT; entry name VIEW0, VIEW1
	 */
	public void prepareHeaderLabel() {
		int k = 0;
		String txt = null;

		labelText = noLabelText;
		for (int i = 0; i < 2; i++) {
			txt = Struc.view_txt[i];
			if (txt == null) {
				break;
			}
			if (i == 0) {
				if (txt.length() >= 20) {
					labelText[k++] = txt.substring(0, 20);
				}
				if (txt.length() >= 31) {
					labelText[k++] = " " + txt.substring(20, 31);
				}
				if (txt.length() >= 40) {
					labelText[k++] = " " + txt.substring(31, 40);
				}
			} else {
				if (txt.length() >= 11) {
					labelText[k++] = " " + txt.substring(0, 11);
				}
				if (txt.length() >= 15) { // UtilLog4j.logInformation(this.getClass(), isCidMode);
					if (isCidMode) {
						labelText[k++] = "     ";
					} else {
						int option = Struc.options[Struc.O_ItmPr];

						if ((option & 3) >= 3) {
							labelText[k++] = "     ";
						} else {
							labelText[k++] = " " + txt.substring(11, 15);
						}
					}
				}
			}
		}
		// for (k=0; k<labelText.length; k++)UtilLog4j.logInformation(this.getClass(), labelText[k]);
	}

	/**
	 * Paints the Graphical Headers of the Selectable Journal View
	 */
	public void paint(Graphics g) {
		// Override the font to plain
		Font font = new Font(getFont().getName(), Font.PLAIN, getFont().getSize());
		int x = 0;
		int width = 0;
		int y = 1;
		FontMetrics fm = getFontMetrics(font);
		Dimension dim = getSize();
		int le = 0;

		if (!isScrollbarEnabled) {
			le = (dim.width - (TransView.DATA_DISPLAY_COLS - 2) * fm.charWidth(' ')) / 2;
		} // leading edge
		else {
			le = (dim.width - TransView.DATA_DISPLAY_COLS * fm.charWidth(' ')) / 2;
		} // leading edge
		g.setColor(Color.gray);
		int k = 0;
		int height = dim.height - 4;

		g.setColor(Color.white);
		g.fillRect(0, 0, dim.width, dim.height);
		int fw = fm.charWidth(' ');
		int cw = 0;
		int ty = dim.height - (dim.height / 3);
		int tx = le; // UtilLog4j.logInformation(this.getClass(), "le=" + le);

		for (k = 0; k < labelText.length; k++) {
			g.setColor(bgColor);
			String txt = labelText[k];

			width = fw * labelLen[k];
			if (k == 0) {
				width += le;
			}
			if (k == 4) {
				width = dim.width - cw - 1;
			}
			cw += width;
			g.drawRect(x + 1, y, width - 2, height);
			g.fillRect(x + 1, y, width - 2, height);

			g.setFont(font);
			g.setColor(Color.white);
			g.drawString(txt, tx, ty);
			tx = x = x + width;
		}
	}

}
