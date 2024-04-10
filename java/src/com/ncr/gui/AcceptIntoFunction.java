package com.ncr.gui;

import com.ncr.Action;
import com.ncr.ArsXmlParser;
import com.ncr.ConIo;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class AcceptIntoFunction extends Modal {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3268337957611479517L;
	private GdLabel info;

	public AcceptIntoFunction(String msg) {
		super("");
		setBounds((Rectangle) ArsXmlParser.getInstance().getPanelElement("AcceptInto", "Bounds"));
		Map labelsMap = (HashMap) ArsXmlParser.getInstance().getPanelElement("AcceptInto", "Label");

		info = (GdLabel) labelsMap.get("Info");
		info.setText(msg);
		getContentPane().add(info, BorderLayout.CENTER);
	}

	public void modalMain(int sts) {
		if (Action.input.key == ConIo.ENTER) {
			super.modalMain(1);
			return;
		} else {
			if (Action.input.key == ConIo.CLEAR) {
				if (Action.input.num == 0) {
					super.modalMain(0);
				} else {
					super.modalMain(1);
				}
				return;
			}
		}
	}
}
