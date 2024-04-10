package com.ncr.gui;

import com.ncr.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.util.HashMap;

public class JournalTable extends JTable {

	private static final long serialVersionUID = -1560628519830392615L;
	public static final String MODEL_OLD = "OldStyle";
	public static final String MODEL_NEW = "NewStyle";
	public static final String MODEL_LASER = "LaserStyle";
	private HashMap modelsMap = new HashMap();
	private String activeModel = "";
	private String lastActiveModel = "";

	public void setModelsMap(HashMap modelsMap) {
		this.modelsMap = modelsMap;
	}

	public boolean supportModel(String modelId) {
		return (modelsMap.get(modelId) != null);
	}

	public String getActiveModel() {
		return activeModel;
	}

	public void setActiveModel(String modelId) {
		UtilLog4j.logInformation(this.getClass(), "modelId=" + modelId);
		this.lastActiveModel = this.activeModel;
		this.activeModel = modelId;
		HashMap journalTableParams = (HashMap) modelsMap.get(modelId);

		if (journalTableParams == null) {
			// Default Model: JournalTable.MODEL_OLD
			journalTableParams = (HashMap) modelsMap.get(JournalTable.MODEL_OLD);
		} else {
			setModel((DefaultTableModel) journalTableParams.get("TableModel"));
			setFont((Font) journalTableParams.get("Font"));
			for (int i = 0; i < getColumnCount(); i++) {
				getColumnModel().getColumn(i).setCellRenderer((TableCellRenderer) journalTableParams.get("CellRender"));
			}
			if (activeModel.equals(JournalTable.MODEL_NEW)) {
				// TODO MMS Rivedere la formattazione della larghezza delle colonne nell'xml
				getColumnModel().getColumn(0).setPreferredWidth((int) (getWidth() * 0.10D));
				getColumnModel().getColumn(1).setPreferredWidth((int) (getWidth() * 0.70D));
				getColumnModel().getColumn(2).setPreferredWidth((int) (getWidth() * 0.20D));
			}
		}
	}

	public void restoreLastActiveModel() {
		setActiveModel(this.lastActiveModel);
	}

	public void addToModel(final String modelId, final String string) {
		UtilLog4j.logDebug(this.getClass(), "Add Object to the Model " + modelId);
		HashMap journalTableParams = (HashMap) modelsMap.get(modelId);
		if (journalTableParams != null) {
			DefaultTableModel journalTableModel = ((DefaultTableModel) journalTableParams.get("TableModel"));
			journalTableModel.addRow(new Object[] { string });
			journalTableModel.fireTableDataChanged();
			selectLast();
		}
	}

	public void addToModel(final String modelId, final Itemdata itemdata) {
		UtilLog4j.logDebug(this.getClass(), "Add Object to the Model " + modelId);
		HashMap journalTableParams = (HashMap) modelsMap.get(modelId);
		if (journalTableParams != null) {
			DefaultTableModel journalTableModel = ((DefaultTableModel) journalTableParams.get("TableModel"));
			journalTableModel.addRow(new Object[] { itemdata });
			journalTableModel.fireTableDataChanged();
			selectLast();
		}
	}

	public void selectLast() {
		int lastRow = getRowCount() - 1;
		if (lastRow < 0) {
			lastRow = 0;
		}
		try {
			UtilLog4j.logDebug(this.getClass(), "Select added row (" + lastRow + ") @" + this.activeModel);
			DevIo.postInput("SELECT" + lastRow, null);
		} catch (Exception exception) {
			exception.printStackTrace();
			UtilLog4j.logInformation(this.getClass(), "Memory Free: " + Runtime.getRuntime().freeMemory() / 1024000
					+ "Mb Total: " + Runtime.getRuntime().totalMemory() / 1024000 + "Mb");
			UtilLog4j.logInformation(this.getClass(), "Force the Garbage Collector Because of Exception");
			System.gc();
		}

	}

	public void selectVoided() {
		if (JournalAndDetail.getInstance().isVoidedRow()) {
			// Seleziono la riga stornata
			int voidedRow = JournalAndDetail.getInstance().getVoidedRow();
			UtilLog4j.logInformation(this.getClass(), "Select voided row (" + voidedRow + ") @" + this.activeModel);
			DevIo.postInput("SELECT" + JournalAndDetail.getInstance().getVoidedRow(), null);
			JournalAndDetail.getInstance().resetVoidedRow();
		}
	}

	public void clear(final String modelId) {
		UtilLog4j.logDebug(this.getClass(), "modelId=" + modelId);
		UtilLog4j.logDebug(this.getClass(), "Clear the table!");
		HashMap journalTableParams = (HashMap) modelsMap.get(modelId);
		if (journalTableParams != null) {
			DefaultTableModel journalTableModel = ((DefaultTableModel) journalTableParams.get("TableModel"));
			journalTableModel.setRowCount(0);
			journalTableModel.fireTableDataChanged();
		}

	}

	public void fireSelection(int row) {
		try {
			if (GdPos.panel.journalTable.getRowCount() > 0 && row < GdPos.panel.journalTable.getRowCount()) {
				UtilLog4j.logDebug(this.getClass(),
						"Select from actionPerformed row (" + row + ") @" + GdPos.panel.journalTable.getActiveModel());
				changeSelection(row, 0, false, false);
				Object selectedObject = GdPos.panel.journalTable.getValueAt(row, -1);
				if (selectedObject instanceof Itemdata) {
					Itemdata itemdata = (Itemdata) selectedObject;
					JournalAndDetail.getInstance().updateItemDetail(itemdata);
				}
			}
		} catch (Exception ex) {
			UtilLog4j.logError(this.getClass(), "Selected Element Nr." + row, ex);
			ex.printStackTrace();
		}
	}

}
