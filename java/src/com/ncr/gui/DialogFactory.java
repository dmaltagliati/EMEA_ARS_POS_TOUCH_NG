package com.ncr.gui;

import com.ncr.Action;
import com.ncr.ArsXmlParser;
import com.ncr.UtilLog4j;

import java.awt.*;

public class DialogFactory {
	public static final int SELECTION_DIALOG = 1;
	public static final int VALASSIS_DIALOG = 2; // MMS-ECOUPONING#A
	public static final int CHECKER_DIALOG = 3;
	public static final int GENERIC_DIALOG = 4;
	public static final int ACCEPT_DIALOG = 5;
	public static final int ERROR_DIALOG = 6;

	public static Modal create(int type, String title) {
		return create(type, title, Action.input.pnt);
	}

	public static Modal create(int type, String title, int modalType) {
		switch (type) {
		case SELECTION_DIALOG: {
			boolean dialogEnabled = ((Rectangle) ArsXmlParser.getInstance().getPanelElement("ButtonDlg",
					"Bounds") != null);
			if (dialogEnabled) {
				return new ButtonDlg(title);
			}
			return new SelDlg(title);
		}
		case CHECKER_DIALOG: {
			boolean dialogEnabled = ((Rectangle) ArsXmlParser.getInstance().getPanelElement("CheckerDlg",
					"Bounds") != null);

			if (dialogEnabled) {
				return new CheckerDlg(title);
			} else {
				dialogEnabled = ((Rectangle) ArsXmlParser.getInstance().getPanelElement("ButtonDlg", "Bounds") != null);
				if (dialogEnabled) {
					return new ButtonDlg(title);
				}
			}
			return new SelDlg(title);
		}
		case GENERIC_DIALOG: {
			return new Modal(title);
		}
		// case ACCEPT_DIALOG: {
		// return new AcceptIntoFunction(title);
		// }
		case ERROR_DIALOG: {
			return new ClrDlg(title, modalType);
		}

		default:
			UtilLog4j.logError(DialogFactory.class, "No Dialog created!");
			return null;
		}

	}

}
