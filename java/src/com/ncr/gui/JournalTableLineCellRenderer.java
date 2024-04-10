package com.ncr.gui;

import com.ncr.Struc;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class JournalTableLineCellRenderer implements TableCellRenderer {
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		JLabel jLabel = (JLabel) value;

		if (jLabel == null || jLabel.getText() == null) {
			return null;
		}
		jLabel.setOpaque(true);
		jLabel.setFont(table.getFont());

		String text = jLabel.getText().trim();

		if (text != null && text.trim().startsWith(">")) {
			Font font = jLabel.getFont();

			jLabel.setText(" " + text.substring(text.indexOf(">") + ">".length()));
			jLabel.setFont(new Font(font.getName(), Font.BOLD, (font.getSize() * 2)));
		} else if (text != null && text.trim().startsWith("&1e&")) {
			Font font = jLabel.getFont();

			jLabel.setText(" " + text.substring(text.indexOf("&1e&") + "&1e&".length()));
			jLabel.setFont(new Font(font.getName(), Font.BOLD, (font.getSize() * 2)));
		}

		Color colors[] = new Color[2];


		colors = Struc.asr_ibkc.getBackGroundColor(Struc.ctl.lan);

		if ((colors[0].equals(Color.black)) && colors[1].equals(Color.black)) {
			colors[0] = Color.white;
			colors[1] = Color.gray;
		}

		Border border = BorderFactory.createLineBorder(new Color(150, 150, 150, 50), 1);

		jLabel.setBorder(border);

		if (isSelected) {
			jLabel.setBackground(table.getSelectionBackground());
		} else {
			if (row % 2 == 0) {
				jLabel.setBackground(new Color(colors[0].getRed(), colors[0].getGreen(), colors[0].getBlue(), 255));
			} else {
				jLabel.setBackground(new Color(colors[1].getRed(), colors[1].getGreen(), colors[1].getBlue(), 255));
			}
		}

		return jLabel;
	}

}
