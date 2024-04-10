package com.ncr.gui;

import com.ncr.ArsXmlParser;
import com.ncr.UtilLog4j;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class SecondPanelRight extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7831862059514242307L;
	private BufferedImage image = null;

	public SecondPanelRight() {
		setLayout(null);

		setBounds((Rectangle) ArsXmlParser.getInstance().getPanelElement("SecondPanelRight", "Bounds"));
		setOpaque(((Boolean) ArsXmlParser.getInstance().getPanelElement("SecondPanelRight", "Opaque")).booleanValue());
		try {
			image = (BufferedImage) ArsXmlParser.getInstance().getPanelElement("SecondPanelRight", "Image");
		} catch (Exception exception) {
		}

		UtilLog4j.logInformation(this.getClass(), "SecondPanelRight " + ((image != null) ? "OK" : "KO"));
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
	}

}
