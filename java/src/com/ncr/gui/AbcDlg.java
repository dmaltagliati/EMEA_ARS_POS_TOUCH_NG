package com.ncr.gui;

import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JPanel;

import com.ncr.Action;
import com.ncr.ArsXmlParser;
import com.ncr.ConIo;
import com.ncr.GdPos;
import com.ncr.UtilLog4j;

public class AbcDlg extends Modal {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4252226765923442615L;
	private int cols = 10;
	private int rows = 4;
	public JPanel area = new JPanel();

	public void touch(MouseEvent e, int type) {
		if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == 0) {
			return;
		}
		char c = ((GdLabel) e.getComponent()).getText().charAt(0);
		KeyEvent k = new KeyEvent(getKbrd(), type, e.getWhen(), e.getModifiers(), KeyEvent.VK_UNDEFINED, c);

		GdPos.panel.queue.postEvent(k);
		e.consume(); /* SUN: key events can be mouse food */
		GdPos.playSound("touch/click.wav");

	}

	public AbcDlg(String title) {
		super(title);
		setBounds((Rectangle) ArsXmlParser.getInstance().getPanelElement("AbcDlg", "Bounds"));
		setFont((Font) ArsXmlParser.getInstance().getPanelElement("AbcDlg", "Font"));
		getContentPane().add(area);
		area.setLayout(new GridLayout(rows, cols, 2, 2));
		for (int y = 0; y < rows; y++) {
			for (int x = 0; x < cols; x++) {
				char c = Action.input.alpha.charAt(y * cols + x);
				GdLabel key = new GdLabel(String.valueOf(c));

				key.setFont(getFont());
				area.add(key);
				if (c > ' ') {
					key.setImage("gif", "ALPHA.GIF");
				} else {
					key.setVisible(false);
				}
				key.addMouseListener(new MouseAdapter() {
					public void mousePressed(MouseEvent e) {
						touch(e, KeyEvent.KEY_PRESSED);
					}

					public void mouseReleased(MouseEvent e) {
						touch(e, KeyEvent.KEY_RELEASED);
					}
				});
			}
		}
		addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {
				for (int ind = area.getComponentCount(); ind-- > 0;) {
					((GdLabel) area.getComponent(ind)).setImage(null, null);
				}
			}
		});
	}

	public void modalMain(int sts) {

		UtilLog4j.logInformation(this.getClass(),
				"Called with " + sts + " and Looking for 0x" + Integer.toHexString(Action.input.key));

		if (Action.input.key == ConIo.CLEAR) {
			if (Action.input.max > 0 && sts > 0) {
				Action.input.reset("");
				Action.input.key = 0;
			} else {
				super.modalMain(sts = 0);
			}
			return;
		}

		if (sts > 0) {
			super.modalMain(sts);
			return;
		}


		// FRG-TRANSAZ.RESO#A END
		UtilLog4j.logInformation(this.getClass(), "SCANNED <" + Action.input.pb + ">");

		super.modalMain(sts);

	}
}
