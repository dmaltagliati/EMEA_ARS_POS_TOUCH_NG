package com.ncr.gui;

import javax.swing.table.DefaultTableModel;

public class JournalTableLineModel extends DefaultTableModel {
	private static final long serialVersionUID = 2334832372128843277L;

	public Object getValueAt(int row, int column) {
		JournalTableLabel journalNewStyleLabel = new JournalTableLabel("");

		if (column < 0 || column >= getColumnCount()) {
			return journalNewStyleLabel;
		}
		if (row < 0 || row >= getRowCount()) {
			return journalNewStyleLabel;
		}

		journalNewStyleLabel.setText((String) super.getValueAt(row, column));
		return journalNewStyleLabel;
	}

	public boolean isCellEditable(int row, int column) {
		return false;
	}

}
