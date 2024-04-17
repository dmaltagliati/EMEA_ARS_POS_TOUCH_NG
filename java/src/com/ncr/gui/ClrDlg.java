package com.ncr.gui;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.ncr.Action;
import com.ncr.ArsXmlParser;
import com.ncr.ConIo;
import com.ncr.DevIo;
import com.ncr.GdPos;
import com.ncr.Struc;
import com.ncr.TouchMenuParameters;
import com.ncr.UtilLog4j;

public class ClrDlg extends Modal {

	/**
	 * 
	 */
	private static final long serialVersionUID = 951715332715928737L;

	private GdLabel key1;
	private GdLabel key2;
	private GdLabel info;

	public ClrDlg(String title, int pnt) {
		super(title);
		//MMS-DIGITALRECEIPT#A BEGIN
		// Le ClrDlr vengono create con un temporizzatore nel caso in cui pnt sia < 0
		// I secondi di attesa prima della chiusura automatica sono parametrizzati nella riga
		// FNPR1 alla 14ma coppia (modalTimeoutMills).
		forceTimer = pnt < 0;
		pnt = Math.abs(pnt);
		//MMS-DIGITALRECEIPT#A END

		int style = (pnt & 2) > 0 ? GdLabel.STYLE_WINDOW : GdLabel.STYLE_STATUS;

		getContentPane().setLayout(null);
		setBounds((Rectangle) ArsXmlParser.getInstance().getPanelElement("ClrDlg", "Bounds"));
		getRootPane()
				.setOpaque(((Boolean) ArsXmlParser.getInstance().getPanelElement("ClrDlg", "Opaque")).booleanValue());
		getContentPane().setBackground((Color) ArsXmlParser.getInstance().getPanelElement("ClrDlg", "BackgroundColor"));

		Map labelsMap = (HashMap) ArsXmlParser.getInstance().getPanelElement("ClrDlg", "Label");

		key1 = (GdLabel) labelsMap.get("FirstKey");
		key2 = (GdLabel) labelsMap.get("SecondKey");

		key1.setText(Action.key_txt[1]);
		key2.setText(Action.key_txt[0]);

		if ((pnt & 4) > 0) {
			setCode(ConIo.ABORT);
			key2.setText(Action.key_txt[2]);
			if ((pnt & 1) < 1) {
				style = GdLabel.STYLE_RAISED;
			}
		}

		info = (GdLabel) labelsMap.get("Info");
		info.setPreferredSize(info.getSize());

		info.setText(title);
		if (GdPos.panel.touchMenu != null && TouchMenuParameters.getInstance().isSelfService()) {
		} else {
			info.setStyle(style);
			info.setAlerted((pnt & 5) > 0);
		}

		// MMS-JUNIT
		setErrorLabel(info);
		// MMS-JUNIT

		if (Struc.tra.code == 0) {
			if (info.getText().trim().equals(GdPos.panel.mnemo.getInfo(10).trim())) {
				GdLabel resto = (GdLabel) labelsMap.get("Resto");
				if (resto != null) {
					if (Struc.tra != null && Struc.tra.bal != 0) {
						resto.setText(Action.editMoney(0, Struc.tra.bal));
						add(resto);
					}
				}
			}
		}

		// if (GdPos.panel.touchMenu != null && TouchMenuParameters.getInstance().isSelfService()) { //MMS-DIGITALRECEIPT#D
		automaticClosingProgressBar.setBackground(Color.white);
		automaticClosingProgressBar.setForeground(Color.red);
		automaticClosingProgressBar.setPreferredSize(getSize());
		automaticClosingProgressBar.setSize(getWidth(), 10);
		automaticClosingProgressBar.setLocation(0, info.getHeight() - automaticClosingProgressBar.getHeight());
		automaticClosingProgressBar.setVisible(false);
		//MMS-DIGITALRECEIPT#D BEGIN
		//	info.add(automaticClosingProgressBar);
		//}
		//MMS-DIGITALRECEIPT#D END

		getContentPane().add(key1);
		getContentPane().add(key2);
		getContentPane().add(info);
		getContentPane().add(automaticClosingProgressBar); //MMS-DIGITALRECEIPT#A

		key1.setEnabled((pnt & 1) > 0);
		key2.setEnabled((pnt & 6) > 0);

		key1.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				if (key1.isEnabled()) {
					GdPos.playSound("touch/click.wav");
					press(ConIo.CLEAR);
				}
			}
		});
		key2.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				if (key2.isEnabled()) {
					GdPos.playSound("touch/click.wav");
					press(getCode());
				}
			}
		});

	}

	public void modalMain(int sts) {
		UtilLog4j.logInformation(this.getClass(),
				"Called with " + sts + " and Looking for 0x" + Integer.toHexString(Action.input.key));
		if (Action.input.key == 0x2f2f) {
			Action.input.reset("");

			return;
		}
		if (sts > 0) {

			return;
		}
		UtilLog4j.logInformation(this.getClass(), "key1.isEnabled()=" + key1.isEnabled());
		UtilLog4j.logInformation(this.getClass(), "key2.isEnabled()=" + key2.isEnabled());
		if (sts == 0) {
			if (Action.input.key > 0) {
				sts = 5;
			}
			if (key1.isEnabled()) {
				if (Action.input.key == 0) {
					Action.input.key = ConIo.CLEAR;
				} else {
					if (Action.input.key == ConIo.CLEAR) {
						sts = 1;
					} else {
						DevIo.alert();
						DevIo.alert();
					}
				}
			}
			if (key2.isEnabled()) {
				if (Action.input.key == 0) {
					Action.input.key = getCode();
				} else {
					if (Action.input.key == getCode()) {
						sts = 2;
					}
				}
			}
		}
		if (sts < 3) {
			// Action.input.key = 0; // MMS-R10
			super.modalMain(sts);
		}

	}
}
