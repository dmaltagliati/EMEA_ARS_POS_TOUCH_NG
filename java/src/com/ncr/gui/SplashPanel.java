package com.ncr.gui;

import com.ncr.ArsXmlParser;
import com.ncr.GdPos;
import com.ncr.UtilLog4j;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class SplashPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7831862059514242307L;
	private BufferedImage image = null;

	public SplashPanel() {

		// MMS-R10
		if (GdPos.CURSOR != null) {
			setCursor(GdPos.CURSOR);
		}
		// MMS-R10

		setLayout(null);
		setBounds((Rectangle) ArsXmlParser.getInstance().getPanelElement("Splash", "Bounds"));
		try {
			image = (BufferedImage) ArsXmlParser.getInstance().getPanelElement("Splash", "Image");
		} catch (Exception exception) {
		}

		UtilLog4j.logInformation(this.getClass(), "SplashPanel " + ((image != null) ? "OK" : "KO"));
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
	}

}
