package com.ncr.gui;

import com.ncr.Action;
import com.ncr.ArsXmlParser;
import com.ncr.ConIo;
import com.ncr.UtilLog4j;

import java.awt.*;

// Permette di avere una finestra modale che accetta un'input da scanner
public class ScanDlg extends Modal {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4845877325395755347L;

	public ScanDlg(String title) {
		super(title);
		getContentPane().setLayout(null);
		setBounds((Rectangle) ArsXmlParser.getInstance().getPanelElement("ScanDlg", "Bounds"));

	}

	public void modalMain(int sts) {

		if (Action.input.key == ConIo.CLEAR) {
			sts = 0;
			// MMS-RT#A BEGIN
			if (Action.input.num > 0) {
				Action.input.reset("");
			} else {
				super.modalMain(sts = 0);
			}
			return;
			// MMS-RT#A END
		}

		// MMS-RT#A BEGIN
		// Se ci sono errori (es. troppi caratteri digitati) ignoro l'errore e lascio la modale aperta
		if (sts > 0) {
			return;
		}

//		if (strategy != null) {
//			int ret = strategy.exec();
//			if (ret > 0) {
//				return;
//			}
//		}
		// MMS-RT#A END

		UtilLog4j.logInformation(this.getClass(), "SCANNED <" + Action.input.pb + ">");

		super.modalMain(sts);

	}
}
