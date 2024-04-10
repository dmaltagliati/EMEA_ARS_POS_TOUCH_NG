package com.ncr.gui;

import com.ncr.ArsXmlParser;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BottomBarExtender extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5432501332994934429L;
	private BufferedImage bottomBarExtenderImage = null;

	public BottomBarExtender() {
		super();
		setLayout(null);

		HashMap map = (HashMap) ArsXmlParser.getInstance().getDialogElement("BottomBarExtender", "Panel");

		setBackground((Color) ArsXmlParser.getInstance().getPanelElement(map, "BottomBarExtender", "BackgroundColor"));
		setOpaque(((Boolean) ArsXmlParser.getInstance().getPanelElement(map, "BottomBarExtender", "Opaque"))
				.booleanValue());
		setBounds((Rectangle) ArsXmlParser.getInstance().getPanelElement(map, "BottomBarExtender", "Bounds"));
		setPreferredSize(getSize());

		try {
			bottomBarExtenderImage = (BufferedImage) ArsXmlParser.getInstance().getPanelElement(map,
					"BottomBarExtender", "Image");

		} catch (Exception exception) {
			exception.printStackTrace();
		}

		Map buttonsMap = (HashMap) ArsXmlParser.getInstance().getPanelElement(map, "BottomBarExtender", "Button");
		Iterator iterator = buttonsMap.values().iterator();

		while (iterator.hasNext()) {
			KeyPadButton keyPadButton = (KeyPadButton) iterator.next();

			add(keyPadButton);
		}

	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(bottomBarExtenderImage, 0, 0, getWidth(), getHeight(), null);
	}

}
