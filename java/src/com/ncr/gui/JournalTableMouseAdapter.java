package com.ncr.gui;

import com.ncr.DevIo;
import com.ncr.GdPos;
import com.ncr.UtilLog4j;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class JournalTableMouseAdapter extends MouseAdapter {
	public void mouseReleased(MouseEvent e) {
		if (GdPos.panel.journalTable.getActiveModel().equals(JournalTable.MODEL_NEW)) {
			int selectedRow = ((JournalTable) e.getSource()).getSelectedRow();
			int column = ((JournalTable) e.getSource()).getColumnModel().getColumnIndexAtX(e.getX());
			if (column == 0) {
				JournalTableLabel journalTableLabel = (JournalTableLabel) ((JournalTable) e.getSource())
						.getValueAt(selectedRow, 0);
				journalTableLabel.getComponent(0).dispatchEvent(e);
			}
			UtilLog4j.logInformation(this.getClass(),
					"Select mouse row (" + selectedRow + ") @" + GdPos.panel.journalTable.getActiveModel());
			DevIo.postInput("SELECTANDSAVE" + selectedRow, null);
			e.consume();
		}
	}
}
