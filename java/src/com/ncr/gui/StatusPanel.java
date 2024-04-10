package com.ncr.gui;

import com.ncr.ArsXmlParser;
import com.ncr.GdPos;
import com.ncr.UtilLog4j;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class StatusPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7831862059514242307L;
	Map labelsMap = null;

	public StatusPanel() {

		// MMS-R10
		if (GdPos.CURSOR != null) {
			setCursor(GdPos.CURSOR);
		}
		// MMS-R10

		setLayout(null);
		setVisible(true);
		setOpaque(((Boolean) ArsXmlParser.getInstance().getPanelElement("Status", "Opaque")).booleanValue());
		setBounds((Rectangle) ArsXmlParser.getInstance().getPanelElement("Status", "Bounds"));

		labelsMap = (HashMap) ArsXmlParser.getInstance().getPanelElement("Status", "Label");

		Iterator iterator = labelsMap.values().iterator();

		while (iterator.hasNext()) {
			GdLabel gdLabel = (GdLabel) iterator.next();

			gdLabel.setVisible(false);
			add(gdLabel);
		}

	}

	public void setVisible(String id, boolean visible) {
		GdLabel gdLabel = (GdLabel) labelsMap.get(id);

		if (gdLabel != null) {
			gdLabel.setVisible(visible);
		} else {
			UtilLog4j.logError(this.getClass(),
					"Impossibile " + (visible ? "visualizzare" : "nascondere") + " l'immagine " + id);
		}
	}

}
