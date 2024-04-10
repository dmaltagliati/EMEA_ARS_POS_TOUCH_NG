package com.ncr.gui;

import com.ncr.ArsXmlParser;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BottomBar extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5432501332994934429L;
	private BufferedImage bottomBarImage = null;

	public BottomBar() {
		super();
		setLayout(null);

		HashMap map = (HashMap) ArsXmlParser.getInstance().getDialogElement("BottomBar", "Panel");

		setBackground((Color) ArsXmlParser.getInstance().getPanelElement(map, "BottomBar", "BackgroundColor"));
		setOpaque(((Boolean) ArsXmlParser.getInstance().getPanelElement(map, "BottomBar", "Opaque")).booleanValue());
		setBounds((Rectangle) ArsXmlParser.getInstance().getPanelElement(map, "BottomBar", "Bounds"));

		try {
			bottomBarImage = (BufferedImage) ArsXmlParser.getInstance().getPanelElement(map, "BottomBar", "Image");
		} catch (Exception exception) {
		}

		Map buttonsMap = (HashMap) ArsXmlParser.getInstance().getPanelElement(map, "BottomBar", "Button");
		Iterator iterator = buttonsMap.values().iterator();

		while (iterator.hasNext()) {
			KeyPadButton keyPadButton = (KeyPadButton) iterator.next();

			add(keyPadButton);
		}

	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(bottomBarImage, 0, 0, getWidth(), getHeight(), null);
	}

}
