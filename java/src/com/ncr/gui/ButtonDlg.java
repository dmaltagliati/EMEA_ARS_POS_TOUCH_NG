package com.ncr.gui;

import com.ncr.Action;
import com.ncr.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

public class ButtonDlg extends Modal {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6961151483718781786L;
	public JScrollPane jScrollPane;

	private int counter = 0; // numero di elementi
	private int rows = 5; // numero di righe della griglia
	private int cols = 4; // numero di colonne della griglia
	private int gap = 1; // gap tra le celle
	private int elementsOnPage = rows * cols; // numero di elementi per pagina
	private int maxPages = 1; // numero di pagine
	private int currentPage = 0; // pagina corrente
	private LinkedList pageList = new LinkedList(); // ogni elemento della lista
													// contiene una pagina
	GdLabel chooser = new GdLabel(""); // parametri della griglia

	public ButtonDlg(String title) {
		super(title);

		getContentPane().setLayout(null);
		if (GdPos.arsGraphicInterface != null) {
			GdPos.arsGraphicInterface.setUndecorated(this,
					((Boolean) ArsXmlParser.getInstance().getPanelElement("ButtonDlg", "Undecorated")).booleanValue());
		}

		setBounds((Rectangle) ArsXmlParser.getInstance().getPanelElement("ButtonDlg", "Bounds"));
		getRootPane().setOpaque(
				((Boolean) ArsXmlParser.getInstance().getPanelElement("ButtonDlg", "Opaque")).booleanValue());
		getContentPane()
				.setBackground((Color) ArsXmlParser.getInstance().getPanelElement("ButtonDlg", "BackgroundColor"));

		Map labelsMap = (HashMap) ArsXmlParser.getInstance().getPanelElement("Chooser", "Label");
		chooser = (GdLabel) labelsMap.get("Chooser");

		try {
			rows = ((Integer) ArsXmlParser.getInstance().getPanelElement("Chooser", "Rows")).intValue();
		} catch (Exception exception) {
		}

		try {
			cols = ((Integer) ArsXmlParser.getInstance().getPanelElement("Chooser", "Cols")).intValue();
		} catch (Exception exception) {
		}

		try {
			gap = ((Integer) ArsXmlParser.getInstance().getPanelElement("Chooser", "VerticalGap")).intValue();
		} catch (Exception exception) {
		}

		setFont(chooser.getFont());

		elementsOnPage = rows * cols;
	}

	private GdLabel createLabel(final String index, String text) {
		GdLabel label = new GdLabel("");

		GdLabel labelTop = createLabelTop(index, text);
		GdLabel labelBottom = createLabelBottom(index, text);

		label.setLayout(new BorderLayout());
		label.setName("INPUT" + index);
		label.add(labelTop, BorderLayout.CENTER);
		label.add(labelBottom, BorderLayout.SOUTH);

		label.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == 0) {
					return;
				}
				GdPos.playBeep();
			}
		});
		return label;
	}

	private GdLabel createLabelTop(final String index, String text) {
		GdLabel label = new GdLabel("");
		StringTokenizer stringTokenizer = new StringTokenizer(text, " ");
		String s = "<html><p align='center'>";
		while (stringTokenizer.hasMoreTokens()) {
			s += stringTokenizer.nextToken();
			if (stringTokenizer.hasMoreTokens()) {
				s += "<br>";
			}
		}
		s += "</p></html>";
		label.setPreferredSize(new Dimension(getWidth() / cols, getHeight() / rows));
		label.setSize(label.getPreferredSize());
		label.setBackground(chooser.getBackground());
		label.setForeground(chooser.getForeground());
		label.setOpaque(true);
		label.setText(s);
		label.setHorizontalTextPosition(SwingConstants.CENTER);
		label.setBorder(BorderFactory.createLineBorder(chooser.getForeground()));
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setVerticalAlignment(SwingConstants.CENTER);
		label.setFont(chooser.getFont());
		return label;
	}

	private GdLabel createLabelBottom(final String index, String text) {
		GdLabel label = new GdLabel(index);
		label.setHorizontalAlignment(SwingConstants.CENTER);
		label.setVerticalAlignment(SwingConstants.CENTER);
		label.setOpaque(true);
		label.setBackground(chooser.getForeground());
		label.setForeground(chooser.getBackground());
		label.setFont(chooser.getFont());
		return label;
	}

	public void add(int foo, String key, String text) {
		maxPages = (counter / (rows * cols));
		if (maxPages >= pageList.size()) {
			GdLabel page = new GdLabel("");
			page.setBounds(0, 0, getWidth(), getHeight());
			page.setLayout(new GridLayout(rows, cols, gap, gap));
			page.setVisible(false);
			pageList.add(page);
			getContentPane().add(page);
		}
		GdLabel page = (GdLabel) (pageList.get(maxPages));
		page.add(createLabel(key, text));
		counter++;
	}

	public void show(String sin, boolean hidden, boolean enableScanner) {
		loadPage(0);
		super.show(sin, hidden, enableScanner);
	}

	public void modalMain(int sts) {
		if (Action.input.key == ConIo.NORTH) {
			if (currentPage > 0) {
				loadPage(--currentPage);
			}
			return;
		}
		if (Action.input.key == ConIo.SOUTH) {
			if (currentPage < maxPages) {
				loadPage(++currentPage);
			}
			return;
		}
		super.modalMain(sts);

	}

	private void loadPage(int page) {
		// ADP-MANTIS-16063 #A BEGIN
		if (pageList.size() == 0) {
			UtilLog4j.logDebug(this.getClass(), "pageList � vuota, esco da metodo loadPage");
			return;
		}
		// ADP-MANTIS-16063 #A END
		Iterator iterator = pageList.iterator();
		while (iterator.hasNext()) {
			GdLabel gdLabel = (GdLabel) iterator.next();
			gdLabel.setVisible(false);
		}
		GdLabel gdLabel = ((GdLabel) (pageList.get(page)));
		while (gdLabel.getComponentCount() < elementsOnPage) {
			gdLabel.add(new GdLabel(""));
		}
		gdLabel.setVisible(true);
	}

}
