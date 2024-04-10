package com.ncr.gui;

import com.ncr.ArsXmlParser;
import com.ncr.Itemdata;
import com.ncr.Struc;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.Map;

public class JournalTableItemdataCellRenderer implements TableCellRenderer {
	public static Color COLOR_DISCOUNT = Color.red;
	public static Color COLOR_COUPON = Color.blue;
	public static Color COLOR_CUSTOMER = Color.blue;
	public static Color COLOR_POINT = Color.orange;
	public static Color COLOR_FISCALCODE = Color.green; // FRG-DMO-CF#A
	public static Color COLOR_LOTTERY = Color.blue; // MMS-LOTTERY#A

	public JournalTableItemdataCellRenderer() {
		Map rowsMap = ArsXmlParser.getInstance().getRowColorsMap();

		if (rowsMap != null) {
			COLOR_DISCOUNT = (Color) rowsMap.get("Discount");
			COLOR_COUPON = (Color) rowsMap.get("Coupon");
			COLOR_CUSTOMER = (Color) rowsMap.get("Customer");
			COLOR_POINT = (Color) rowsMap.get("Point");
			COLOR_FISCALCODE = (Color) rowsMap.get("FiscalCode");
			COLOR_LOTTERY = (Color) rowsMap.get("Lottery"); // MMS-LOTTERY#A

		}
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		JournalTableLabel journalNewStyleLabel = (JournalTableLabel) value;

		if (journalNewStyleLabel == null) {
			return null;
		}

		journalNewStyleLabel.setOpaque(true);
		journalNewStyleLabel.setFont(table.getFont());

		if (journalNewStyleLabel.getType() == Itemdata.DISCOUNT) {
			journalNewStyleLabel.setForeground(COLOR_DISCOUNT);
		}
		// MMS-ECOUPONING#A BEGIN
		else if (journalNewStyleLabel.getType() == Itemdata.COUPON) {
			journalNewStyleLabel.setForeground(COLOR_COUPON);
		}
		// MMS-ECOUPONING#A END
		else if (journalNewStyleLabel.getType() == Itemdata.CUSTOMER) {
			journalNewStyleLabel.setForeground(COLOR_CUSTOMER);
		} else if (journalNewStyleLabel.getType() == Itemdata.POINT) {
			journalNewStyleLabel.setForeground(COLOR_POINT);
		}
		// FRG-DMO-CF#A BEGIN
		else if (journalNewStyleLabel.getType() == Itemdata.FISCALCODE) {
			journalNewStyleLabel.setForeground(COLOR_FISCALCODE);
		}
		// FRG-DMO-CF#A END


		if (journalNewStyleLabel.isVoid()) {
			Font font = journalNewStyleLabel.getFont();
			Map attributes = font.getAttributes();

			attributes.put(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);
			journalNewStyleLabel.setFont(new Font(attributes));
		}

		Color colors[] = new Color[2];


		colors = Struc.asr_ibkc.getBackGroundColor(Struc.ctl.lan);

		if ((colors[0].equals(Color.black)) && colors[1].equals(Color.black)) {
			colors[0] = Color.white;
			colors[1] = Color.gray;
		}

		Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		Border matteBorder = BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(100, 100, 100, 100));
		Border border = BorderFactory.createCompoundBorder(matteBorder, emptyBorder);

		journalNewStyleLabel.setBorder(border);

		if (isSelected) {
			journalNewStyleLabel.setBackground(table.getSelectionBackground());
		} else {
			int r = row % 2;
			journalNewStyleLabel
					.setBackground(new Color(colors[r].getRed(), colors[r].getGreen(), colors[r].getBlue(), 255));
		}

		return journalNewStyleLabel;
	}

}
