package com.ncr.gui;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.ncr.Action;
import com.ncr.ArsXmlParser;
import com.ncr.ConIo;
import com.ncr.GdPos;
import com.ncr.UtilLog4j;

public class PluDlg extends Modal {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4637817175724622785L;
	int correctKey = ConIo.CLEAR;
	GdLabel key1;
	GdLabel key2;
	JPanel info = new JPanel();
	public JLabel l1 = new JLabel();
	public JLabel l2 = new JLabel();
	public JLabel l3 = new JLabel();
	public JLabel l4 = new JLabel();
	public JLabel l5 = new JLabel();
	public JLabel l6 = new JLabel();

	public PluDlg(String title) {
		super(title);
		getContentPane().setLayout(null);
		setBounds((Rectangle) ArsXmlParser.getInstance().getPanelElement("PluDlg", "Bounds"));
		Map labelsMap = (HashMap) ArsXmlParser.getInstance().getPanelElement("PluDlg", "Label");

		UtilLog4j.logInformation(this.getClass(), "Bounds = " + getBounds().x + ", " + getBounds().y + ", "
				+ getBounds().width + ", " + getBounds().height);

		l1 = (GdLabel) labelsMap.get("Line1");
		l2 = (GdLabel) labelsMap.get("Line2");
		l3 = (GdLabel) labelsMap.get("Line3");
		l4 = (GdLabel) labelsMap.get("Line4");
		l5 = (GdLabel) labelsMap.get("Line5");
		l6 = (GdLabel) labelsMap.get("Line6");

		key1 = (GdLabel) labelsMap.get("FirstKey");
		key2 = (GdLabel) labelsMap.get("SecondKey");

		key1.setText(Action.key_txt[1]);
		key2.setText(Action.key_txt[3]);

		info.setLayout(null);
		info.setBackground((Color) ArsXmlParser.getInstance().getPanelElement("PluDlg", "BackgroundColor"));
		info.setBounds(0, 0, getWidth(), getHeight());
		info.add(l1);
		info.add(l2);
		info.add(l3);
		info.add(l4);
		info.add(l5);
		info.add(l6);
		getContentPane().add(info);

		info.add(key1);
		key1.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				if (key1.isEnabled()) {
					GdPos.playSound("touch/click.wav");
				}
				Action.input.key = correctKey;
				// modalMain(0);
				press(correctKey);
			}
		});
		key2.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				if (key2.isEnabled()) {
					GdPos.playSound("touch/click.wav");
				}
				Action.input.key = ConIo.ENTER;
				// modalMain(0);
				press(ConIo.ENTER);

			}
		});
	}

	public void modalMain(int sts) {
		if (Action.input.key == 0x2f2f) {
			Action.input.reset("");
			return;
		}
		if (sts > 0) {
			return;
		}
		if (Action.input.key == 0) {
			Action.input.key = correctKey;
		}
		if (Action.input.key == correctKey) {
			super.modalMain(0);
		}
		if (Action.input.key == ConIo.ENTER) {
			super.modalMain(0);
		}
	}

}
