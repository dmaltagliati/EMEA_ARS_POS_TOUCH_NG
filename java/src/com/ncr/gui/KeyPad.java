package com.ncr.gui;

import com.ncr.ArsXmlParser;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class KeyPad extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5432501332994934429L;
	private BufferedImage keyPadImage = null;

	public KeyPad() {
		super();
		setLayout(null);

		HashMap map = (HashMap) ArsXmlParser.getInstance().getDialogElement("KeyPad", "Panel");

		setBackground((Color) ArsXmlParser.getInstance().getPanelElement(map, "KeyPad", "BackgroundColor"));
		setOpaque(((Boolean) ArsXmlParser.getInstance().getPanelElement(map, "KeyPad", "Opaque")).booleanValue());
		setBounds((Rectangle) ArsXmlParser.getInstance().getPanelElement(map, "KeyPad", "Bounds"));

		try {
			BufferedImage backgroundImage = (BufferedImage) ArsXmlParser.getInstance().getPanelElement(map, "KeyPad",
					"Image");

			keyPadImage = backgroundImage.getSubimage(getX(), getY(), getWidth(), getHeight());
		} catch (Exception exception) {
		}

		Map buttonsMap = (HashMap) ArsXmlParser.getInstance().getPanelElement(map, "KeyPad", "Button");
		Iterator iterator = buttonsMap.values().iterator();

		while (iterator.hasNext()) {
			KeyPadButton keyPadButton = (KeyPadButton) iterator.next();

			add(keyPadButton);
		}

	}

	protected void paintComponent(Graphics g) {
		g.drawImage(keyPadImage, 0, 0, getWidth(), getHeight(), null);
		super.paintComponent(g);
	}

}
