package com.ncr.gui;

import com.ncr.ArsXmlParser;
import com.ncr.GdPos;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class WaitPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7831862059514242307L;
	private BufferedImage image = null;

	public WaitPanel() {

		// MMS-R10
		if (GdPos.CURSOR != null) {
			setCursor(GdPos.CURSOR);
		}
		// MMS-R10

		setLayout(null);
		setVisible(false);
		setOpaque(((Boolean) ArsXmlParser.getInstance().getPanelElement("Wait", "Opaque")).booleanValue());
		setBounds((Rectangle) ArsXmlParser.getInstance().getPanelElement("Wait", "Bounds"));

		try {
			image = (BufferedImage) ArsXmlParser.getInstance().getPanelElement("Wait", "Image");
		} catch (Exception exception) {
		}

		Map labelsMap = (HashMap) ArsXmlParser.getInstance().getPanelElement("Wait", "Label");
		Iterator iterator = labelsMap.values().iterator();

		while (iterator.hasNext()) {
			GdLabel gdLabel = (GdLabel) iterator.next();

			add(gdLabel);
		}

	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
	}

}
