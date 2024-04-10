package com.ncr.gui;

import com.ncr.ArsXmlParser;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class PwdDlg extends Modal {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6944813877620290160L;
	JPanel info = new JPanel();
	public GdLabel nmbr;
	public GdLabel pers;
	public GdLabel labelName;

	public PwdDlg(String name) {
		super("PoS Enter Data");
		getContentPane().setLayout(null);
		setBounds((Rectangle) ArsXmlParser.getInstance().getPanelElement("PwdDlg", "Bounds"));
		Map labelsMap = (HashMap) ArsXmlParser.getInstance().getPanelElement("PwdDlg", "Label");

		nmbr = (GdLabel) labelsMap.get("Code");
		pers = (GdLabel) labelsMap.get("Number");
		labelName = (GdLabel) labelsMap.get("Name");
		labelName.setText(name);

		info.setLayout(null);
		info.setBackground((Color) ArsXmlParser.getInstance().getPanelElement("PwdDlg", "BackgroundColor"));
		info.setBounds(0, 0, getWidth(), getHeight());
		info.add(nmbr);
		info.add(pers);
		info.add(labelName);

		getContentPane().add(info);
	}
}
