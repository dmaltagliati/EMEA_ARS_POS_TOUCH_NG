package com.ncr.gui;

import com.ncr.Action;
import com.ncr.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.Iterator;
import java.util.LinkedList;

public class JournalTableItemdataModel extends DefaultTableModel {
	private static final long serialVersionUID = 2334832372128843277L;

	private LinkedList itemList = new LinkedList();

	public JournalTableItemdataModel() {
	}

	public int getRowCount() {
		if (itemList == null) {
			return 0;
		}
		return itemList.size();
	}

	public Object getValueAt(int row, int column) {

		JournalTableLabel journalNewStyleLabel = new JournalTableLabel("");
		try {
			Itemdata itemdata = (Itemdata) itemList.get(row);
			journalNewStyleLabel.setType(itemdata.getItemType());

			// Settaggio testo e allineamento in base al tipo di riga
			switch (column) {
			case 0:
				if (itemdata.isItem()) {
					journalNewStyleLabel.setText("" + itemdata.qty);
				}
				journalNewStyleLabel.setHorizontalAlignment(SwingConstants.RIGHT);
				break;

			case 1:
				if (itemdata.isTender()) {
					journalNewStyleLabel.setHorizontalAlignment(SwingConstants.RIGHT);
				} else if (itemdata.isSubtotal()) {
					journalNewStyleLabel.setHorizontalAlignment(SwingConstants.RIGHT);
				}
				journalNewStyleLabel.setText(itemdata.text.trim());
				break;

			case 2:
				if (!itemdata.isCustomer() && !itemdata.isPoint() && !itemdata.isFiscalCode() ) {
					journalNewStyleLabel.setText(Action.editMoney(0, itemdata.amt));
					journalNewStyleLabel.setHorizontalAlignment(SwingConstants.RIGHT);
				}
				break;

			default:
				if (itemdata.isDiscount() || itemdata.isPoint()) {
					for (int i = row - 1; i >= 0; i--) {
						itemdata = (Itemdata) itemList.get(i);
						if (itemdata.isSubtotal()) {
							return itemdata;
						}
						if (itemdata.isItem()) {
							Itemdata itemdataToDiscount = itemdata.copy();

							itemdataToDiscount.setItemType(Itemdata.DISCOUNT);
							if (itemdata.isVoided()) {
								itemdataToDiscount.setVoided();
							}
							return itemdataToDiscount;
						}
					}
				}
				return itemdata;
			}
		} catch (Exception e) {
			// TODO MMS Problema dovuto al fatto che in itemList non sono inseriti gli sconti che
			// invece appaiono nella tabella...
			// Bisogna unificare la itemList con il contenuto della promotionListGui
			System.err.println("Error on row (" + row + "):" + e.getMessage());
			UtilLog4j.logError(this.getClass(), "Error on row (" + row + ")");
		}
		return journalNewStyleLabel;

	}

	public void addRow(Object[] objects) {
		for (int i = 0; i < objects.length; i++) {
			Itemdata itemdata = (Itemdata) objects[i];
			if ((itemdata.spf1 & (Struc.M_ERRCOR | Struc.M_VOID)) != 0) { // MMS-LOTTERY-VAR1#A
				removeRow(itemdata);
			} else {
				addRow(itemdata);
			}
		}
	}

	public void setRowCount(int rowCount) {
		if (rowCount == 0) {
			itemList.clear();
		}
	}

	public boolean isCellEditable(int row, int column) {
		return false;
	}

	public void addRow(Itemdata itemdata) {
		UtilLog4j.logInformation(this.getClass(), itemdata.dump());

		UtilLog4j.logInformation(this.getClass(), "DUMP BEFORE______________________________________");
		for (int i = 0; i < itemList.size(); i++) {
			Itemdata itemdataToModify = (Itemdata) itemList.get(i);

			if (itemdataToModify.isItem()) {
				UtilLog4j.logInformation(this.getClass(), "Item: " + itemdataToModify.dump());

				Iterator iterator = itemdataToModify.promotionListGui.iterator();

				while (iterator.hasNext()) {
					Itemdata promotionItemdata = (Itemdata) iterator.next();
					UtilLog4j.logInformation(this.getClass(), "   Promotion: " + promotionItemdata.dump());

				}
			}
		}
		UtilLog4j.logInformation(this.getClass(), "_________________________________________________");

		if (itemdata.isDiscount() || itemdata.isPoint()) {
			int firstIndexOfEntryIdZero = -1;

			for (int i = 0; i < itemList.size(); i++) {
				Itemdata itemdataToModify = (Itemdata) itemList.get(i);

				if (itemdataToModify.entryId == itemdata.entryId) {

					// Aggiungo la promo alla lista di promozioni
					if (itemdataToModify.isItem()) {
						if (!itemdataToModify.promotionListGui.contains(itemdata)) {

							UtilLog4j.logInformation(this.getClass(), "Adding Promotion! " + itemdata.dump());
							itemdataToModify.promotionListGui.add(itemdata);
						}
					}

					if (firstIndexOfEntryIdZero == -1) {
						firstIndexOfEntryIdZero = i;
					}
					if (itemdataToModify.isDiscount() || itemdataToModify.isPoint()) {
						if (itemdataToModify.number.equals(itemdata.number)) {
							if (itemdata.amt != 0) {
								itemList.add(i + 1, itemdata);
							}
							itemList.remove(i);
							firstIndexOfEntryIdZero = -1;
							break;
						}
					}
				}
			}
			if (firstIndexOfEntryIdZero > -1) {
				// Inserisco sconti o punti solo se sono valorizzati.
				// Solitamente sono a zero
				// quando si tratta di un reversal della promozione.
				if (itemdata.amt != 0) {
					itemList.add(firstIndexOfEntryIdZero + 1, itemdata);
				}
			}

		}  else if (itemdata.isMessage()) {
			for (int i = 0; i < itemList.size(); i++) {
				Itemdata itemdataToModify = (Itemdata) itemList.get(i);
				if (itemdataToModify.entryId == itemdata.entryId) {
					// Aggiungo il messaggio alla lista di promozioni
					if (itemdataToModify.isItem()) {
						itemdataToModify.promotionListGui.add(itemdata);
						break;
					}
				}
			}

		}  else {
			itemList.add(itemdata);
		}

		UtilLog4j.logInformation(this.getClass(), "DUMP AFTER ______________________________________");
		for (int i = 0; i < itemList.size(); i++) {
			Itemdata itemdataToModify = (Itemdata) itemList.get(i);

			if (itemdataToModify.isItem()) {
				UtilLog4j.logInformation(this.getClass(), "Item: " + itemdataToModify.dump());

				Iterator iterator = itemdataToModify.promotionListGui.iterator();

				while (iterator.hasNext()) {
					Itemdata promotionItemdata = (Itemdata) iterator.next();
					UtilLog4j.logInformation(this.getClass(), "   Promotion: " + promotionItemdata.dump());

				}
			}
		}
		UtilLog4j.logInformation(this.getClass(), "_________________________________________________");

	}

	public void removeRow(Itemdata itemdata) {

		UtilLog4j.logInformation(this.getClass(), itemdata.dump());
		Iterator iterator = itemList.iterator();

		int currentRow = 0;
		while (iterator.hasNext()) {
			Itemdata itemdataToRemove = (Itemdata) iterator.next();

		 if (itemdataToRemove.entryId == itemdata.originalEntryId) {
				if (itemdata.entryId == 0) {
					// Rimuovo tutti gli elementi che hanno entryId a zero
					// (es. SUBTOTALE, SCONTI TRANSAZIONALI)
					UtilLog4j.logInformation(this.getClass(), "Removing " + itemdataToRemove.dump());
					iterator.remove();
				} else {
					// Evidenzio gli articoli come stornati
					// ed elimino tutte le informazioni sottostanti (es. sconti)
					if (itemdataToRemove.isItem()) {
						UtilLog4j.logInformation(this.getClass(), "Setting as Voided " + itemdataToRemove.dump());
						itemdataToRemove.setVoided();
						// itemdataToRemove.promotionListGui.clear();
					} else {
						UtilLog4j.logInformation(this.getClass(), "Removing " + itemdataToRemove.dump());
						iterator.remove();
					}
					// Memorizzo l'indice della riga stornata
					if (!JournalAndDetail.getInstance().isVoidedRow()) {
						JournalAndDetail.getInstance().setVoidedRow(currentRow);
					}
				}
			}
			currentRow++;
		}

	}

}
