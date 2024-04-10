package com.ncr.gui;

import com.ncr.ArsXmlParser;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Arrows extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5432501332994934429L;
	private BufferedImage arrowsImage = null;

	public Arrows() {
		super();
		setLayout(null);

		HashMap map = (HashMap) ArsXmlParser.getInstance().getDialogElement("Arrows", "Panel");

		setBackground((Color) ArsXmlParser.getInstance().getPanelElement(map, "Arrows", "BackgroundColor"));
		setOpaque(((Boolean) ArsXmlParser.getInstance().getPanelElement(map, "Arrows", "Opaque")).booleanValue());
		setBounds((Rectangle) ArsXmlParser.getInstance().getPanelElement(map, "Arrows", "Bounds"));

		try {
			BufferedImage backgroundImage = (BufferedImage) ArsXmlParser.getInstance().getPanelElement(map, "Arrows",
					"Image");

			arrowsImage = backgroundImage.getSubimage(getX(), getY(), getWidth(), getHeight());
		} catch (Exception exception) {
		}

		Map buttonsMap = (HashMap) ArsXmlParser.getInstance().getPanelElement(map, "Arrows", "Button");
		Iterator iterator = buttonsMap.values().iterator();

		while (iterator.hasNext()) {
			KeyPadButton keyPadButton = (KeyPadButton) iterator.next();

			add(keyPadButton);
		}

	}

	protected void paintComponent(Graphics g) {
		g.drawImage(arrowsImage, 0, 0, getWidth(), getHeight(), null);

		super.paintComponent(g);
	}

}
