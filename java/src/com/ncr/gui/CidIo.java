package com.ncr.gui;

import com.ncr.FmtIo;
import com.ncr.UtilLog4j;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Date;

// TODO MMS-R10 Valutare se funziona ancora questa classe

public class CidIo extends Window {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6519675686318360358L;

	int width = Integer.getInteger("CID", 0).intValue();

	GdLabel cusArea[] = new GdLabel[4];
	GdLabel proArea = new GdLabel("        ");

	JPanel pnlCus12 = new JPanel();
	JPanel pnlCus34 = new JPanel();

	Font font = new Font("Monospaced", Font.BOLD, 56);
	// Ground ground = null;

	public CidIo(Frame parent) {
		super(parent);
		UtilLog4j.logInformation(this.getClass(), parent.getName());

		// File f = FmtIo.localFile("gif", "CIDWIN.GIF");
		// if (f.exists()) {
		// ground = new Ground(this, f.getAbsolutePath());
		// }
		for (int ind = 0; ind < cusArea.length; ind++) {
			if (ind < 2) {
				pnlCus12.add(cusArea[ind] = new GdLabel(null));
			} else {
				pnlCus34.add(cusArea[ind] = new GdLabel(null));
			}
			// cusArea[ind].bim = ground;
			cusArea[ind].setEnabled(false);
		}
		// pnlCus12.bim = pnlCus34.bim = ground;
		add(pnlCus12, BorderLayout.NORTH);
		add(pnlCus34, BorderLayout.SOUTH);
		add(proArea, BorderLayout.EAST);
		Dimension d = getToolkit().getScreenSize();

		setBounds(d.width, 0, 800, 600);
	}

	public void init() {
		if (width == 0) {
			return;
		}
		setFont(font);
		setVisible(true);
		proArea.getSize();
		clear();
	}

	public void display(int line, String data) {
		cusArea[line].setText(data);
	}

	public void clear() {
		String name = "PROMO";
		File hot = FmtIo.localFile("hot", name + ".GIF");

		if (hot.exists()) {
			File f = FmtIo.localFile("gif", hot.getName());

			if (f.exists()) {
				proArea.setImage(null, null);
				f.delete();
			}
			if (!hot.renameTo(f)) {
				UtilLog4j.logInformation(this.getClass(),
						new Date() + " [" + System.currentTimeMillis() + "] " + hot.getPath());
			}
		}
		for (int ind = cusArea.length; ind-- > 0; display(ind, null)) {
			;
		}
		proArea.setImage("gif", name + ".GIF");
	}
}
