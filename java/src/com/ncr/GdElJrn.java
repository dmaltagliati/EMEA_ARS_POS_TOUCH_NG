package com.ncr;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Scrollbar;
import java.awt.SystemColor;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.ImageIcon;

public class GdElJrn extends Canvas {

	/**
	 *
	 */
	private static final long serialVersionUID = -3984656372031096228L;
	public int cols, rows;
	public boolean trxWise = false;

	SeqIo file = null;
	BufferedImage image = null;
	BufferedImage image2Screen = null;
	Font dble = null, sgle;
	public String list[];
	Point pad = new Point(4, 0);
	public Scrollbar bar = new Scrollbar();

	// BAS-FIX-2130427-MMS#A BEGIN
	private int minimumOperator = 0;
	// BAS-FIX-2130427-MMS#A END

	static Color colorScrollbar = Color.getColor("COLOR_SCROLLBAR", SystemColor.scrollbar);

	public GdElJrn(int x, int y) {
		this.cols = x;
		this.rows = y;
		this.list = new String[rows];
	}

	public boolean scroll(int vkey) {
		int val = bar.getValue(), prv = val;

		if (!bar.isEnabled()) {
			return false;
		}
		switch (vkey) {
			case KeyEvent.VK_UP:
				bar.setMinimum(getMinimum()); // BAS-FIX-2130427-MMS#A
				val--;
				break;

			case KeyEvent.VK_DOWN:
				val++;
				break;

			case KeyEvent.VK_PAGE_UP:
				val -= bar.getBlockIncrement();
				break;

			case KeyEvent.VK_PAGE_DOWN:
				val += bar.getBlockIncrement();
				break;

			case KeyEvent.VK_HOME:
				val = bar.getMinimum();
				break;

			case KeyEvent.VK_END:
				val = bar.getMaximum() - rows;
				break;

			default:
				return false;
		}
		bar.setValue(val);
		// BAS-FIX-2130427-MMS#A BEGIN
		if (((Action.input.lck & 0x10) == 0) && (vkey == KeyEvent.VK_UP) // &&
				// (prv
				// ==
				// bar.getValue()))
				// {
				// Aggiunto test (bar.getValue() != 0) perch� dopo un'azzeramento dati
				// veniva
				// dato il messaggio di CHIAVE SUPERVISORE dopo aver pagato...
				&& (prv == bar.getValue()) && (bar.getValue() != 0)) {
			GdPos.panel.clearLink(1, 1);
		}
		// BAS-FIX-2130427-MMS#A END
		return prv != bar.getValue();
	}

	private Dimension getCharSize() {
		Font f = getFont();
		FontMetrics fm = getFontMetrics(f);

		return new Dimension(fm.charWidth(' '), f.getSize());
	}

	public Dimension getPreferredSize() {
		Dimension d = getCharSize();

		d.height += 2;
		d.width *= cols;
		d.height *= list.length;
		d.width += pad.x << 1;
		d.height += pad.y << 1;
		d.width += 14;
		return d;
	}

	public String getText(int ind) {
		return ind < list.length ? list[ind] : null;
	}




	int getMinimum() {

		if (((Action.input.lck & 0x10) > 0) ) {
			return 0;
		}
		return minimumOperator;
	}

	// BAS-FIX-2130427-MMS#A END

	void setPicture(String name) {

		if (name != null) {
			File f = FmtIo.localFile("gif", name + ".GIF");

			if (f.exists()) {
				ImageIcon icon = new ImageIcon(f.getAbsolutePath());

				image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
				Graphics g = image.createGraphics();

				icon.paintIcon(null, g, 0, 0);
				g.dispose();
			}
		} else {
			image = null;
		}
		// GdPos.panel.jList.setVisible(image == null);
		if (GdPos.panel.journalTable != null) {
			GdPos.panel.journalTable.setVisible(image == null);
			GdPos.panel.journalPicture.setImage(image);
		}

		repaint();
	}

	void setPicture2Screen(String name) {

		if (name != null) {
			File f = FmtIo.localFile("gif", name + ".GIF");

			if (f.exists()) {
				ImageIcon icon = new ImageIcon(f.getAbsolutePath());

				image2Screen = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
						BufferedImage.TYPE_INT_ARGB);
				Graphics g = image2Screen.createGraphics();

				icon.paintIcon(null, g, 0, 0);
				g.dispose();
			}
		} else {
			image2Screen = null;
		}
		// GdPos.panel.jList.setVisible(image == null);

		// GdPos.panel.journalTable2Screen.clear(JournalTable.MODEL_NEW;
		if (GdPos.panel.journalTable2Screen != null) {
			GdPos.panel.journalTable2Screen.setVisible(image2Screen == null);
			GdPos.panel.journalPicture2Screen.setImage(image2Screen);
		}
		repaint();
	}

	void drawText(Graphics g, String s, int y, int wide, int high) {
		int x = pad.x, mid = cols >> 1;
		boolean quarter = rows < list.length;

		if (quarter) {
			y -= high >>= 1;
		}
		y -= high >> 2;
		if (s.charAt(1) == '>') {
			g.setFont(dble);
			if (quarter) {
				for (int col = 1; col++ < mid; x += wide << 2) {
					if (col - 2 == mid >> 1) {
						x = pad.x;
						y += high;
					}
					g.drawString(s.substring(col, col + 1), x, y + 2);
				}
			} else {
				for (int col = 1; col++ < mid; x += wide << 1) {
					g.drawString(s.substring(col, col + 1), wide + x, y + 1);
				}
			}
		} else {
			g.setFont(sgle);
			if (quarter) {
				if (s.substring(mid).trim().length() == 0) {
					s = s.substring(mid) + s.substring(1, mid + 1);
				}
				for (int col = 0; ++col < cols; x += wide << 1) {
					if (col == mid) {
						x = pad.x;
						y += high;
					}
					g.drawString(s.substring(col, col + 1), x, y + 1);
				}
			} else {
				g.drawString(s, x, y);
			}
		}
	}

	void init(SeqIo file, int option) {
		// Font f = getFont();

		this.file = file;
		if ((option & 0x01) > 0) {
			rows /= 3;
			sgle = dble;
		}
		trxWise = (option & 2) > 0;
		// dble = new Font(f.getName(), Font.BOLD, sgle.getSize() * 4 / 3);
		bar.setBlockIncrement(rows);
		// BAS-FIX-2130427-MMS#A BEGIN
		// bar.setValues(0, rows, 0, rows);
		bar.setValues(0, rows, getMinimum(), rows);
		// BAS-FIX-2130427-MMS#A END
		bar.setBackground(colorScrollbar);
		bar.setVisible(false);
	}

	void stop() {
		file = null;
	}

	void setScrollbar(boolean enabled) {
		bar.setEnabled(enabled);
	}

	void view(boolean append) {
		int ind = 0, rec;

		if (append) {
			rec = file.getSize();

			if (rec <= bar.getMaximum()) {
				scroll(KeyEvent.VK_END);
			} else {
				// BAS-FIX-2130427-MMS#A BEGIN
				// bar.setValues(rec - rows, rows, file.recno, rec);
				bar.setValues(rec - rows, rows, getMinimum(), rec);
				// BAS-FIX-2130427-MMS#A END
			}
			if (--rec > file.recno) {
				if (trxWise) {
					file.read(rec);
					if (file.pb.charAt(0) != ' ') {
						// BAS-FIX-2130427-MMS#A BEGIN
						// bar.setValues(rec, rows, file.recno, rec + rows);
						bar.setValues(rec, rows, getMinimum(), rec + rows);
						// BAS-FIX-2130427-MMS#A END
					}
				}
			} else {
				// BAS-FIX-2130427-MMS#A BEGIN
				// bar.setValues(rec, rows, file.recno, rec + rows);
				bar.setValues(rec, rows, getMinimum(), rec + rows);
				// BAS-FIX-2130427-MMS#A BEGIN
			}
		}
		rec = bar.getValue();
		if (file != null) {
			while (ind < rows) {
				list[ind] = file.read(++ind + rec) > 0 ? file.pb : null;
			}
		}
		setPicture(null);
		setPicture2Screen(null);
		repaint();
	}



	void clear() {
		for (int idx = 0; idx < rows; idx++) {
			list[idx] = "  ";
		}
	}
}
