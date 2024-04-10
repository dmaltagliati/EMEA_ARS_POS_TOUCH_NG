package com.ncr.gui;

import com.ncr.ArsXmlParser;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class CheckerDlg extends Modal {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6961151483718781786L;
	public GdLabel badgeLabel;
	JPanel info = new JPanel();

	public CheckerDlg(String title) {
		super(title);
		Init();
	}

	private void Init() {

		getContentPane().setLayout(null);

		setBounds((Rectangle) ArsXmlParser.getInstance().getPanelElement("CheckerDlg", "Bounds"));
		getRootPane().setOpaque(
				((Boolean) ArsXmlParser.getInstance().getPanelElement("CheckerDlg", "Opaque")).booleanValue());
		getContentPane()
				.setBackground((Color) ArsXmlParser.getInstance().getPanelElement("CheckerDlg", "BackgroundColor"));

		Map labelsMap = (HashMap) ArsXmlParser.getInstance().getPanelElement("CheckerDlg", "Label");
		badgeLabel = (GdLabel) labelsMap.get("Badge");

		info.setBounds(((Rectangle) ArsXmlParser.getInstance().getPanelElement("CheckerDlg", "Bounds")));
		info.setLayout(null);
		info.setBackground((Color) ArsXmlParser.getInstance().getPanelElement("CheckerDlg", "BackgroundColor"));
		info.add(badgeLabel);

		getContentPane().add(info);

	}

	public static boolean checkerDlgExists() {
		try {
			Map labelsMap = (HashMap) ArsXmlParser.getInstance().getPanelElement("CheckerDlg", "Label");
			if (!labelsMap.isEmpty()) {
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		return true;
	}
}
