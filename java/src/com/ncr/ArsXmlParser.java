package com.ncr;

import com.ncr.gui.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

public class ArsXmlParser {

	private static ArsXmlParser parser = new ArsXmlParser();

	public static ArsXmlParser getInstance() {
		return parser;
	}

	private ArsXmlParser() {
	}

	private Document dom;
	private Map map = new HashMap();

	public void load() {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

		try {

			// Using factory get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			// parse using builder to get DOM representation of the XML file
			dom = db.parse("conf/ars.xml");
			parseDocument();

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (SAXException se) {
			se.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private void parseDocument() {
		// get the root element
		Element docEle = dom.getDocumentElement();

		// get a nodelist of elements
		NodeList nl = docEle.getElementsByTagName("Fonts");

		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {

				// get the panel element
				Element el = (Element) nl.item(i);

				// get the Element map
				Map elementMap = getFont(el);

				// add it to the map
				Map font = (HashMap) map.get("Fonts");

				if (font != null) {
					elementMap.putAll(font);
				}
				map.put("Fonts", elementMap);
			}
		}

		// Row Colors
		nl = docEle.getElementsByTagName("Rows");

		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {

				// get the panel element
				Element el = (Element) nl.item(i);

				// get the Element map
				Map elementMap = getRowColor(el);

				// add it to the map
				Map row = (HashMap) map.get("Rows");

				if (row != null) {
					elementMap.putAll(row);
				}
				map.put("Rows", elementMap);
			}
		}

		// get a nodelist of elements
		nl = docEle.getElementsByTagName("Panel");
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {

				// get the panel element
				Element el = (Element) nl.item(i);

				// get the Element map
				Map elementMap = getPanel(el);

				Map panelMap = (HashMap) map.get("Panel");

				if (panelMap != null) {
					elementMap.putAll(panelMap);
				}
				map.put("Panel", elementMap);

			}
		}

		// get a nodelist of elements
		nl = docEle.getElementsByTagName("Dialog");
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {

				// get the panel element
				Element el = (Element) nl.item(i);

				// get the Element map
				Map elementMap = getDialog(el);

				// add it to the map
				Map dialog = (HashMap) map.get("Dialog");

				if (dialog != null) {
					elementMap.putAll(dialog);
				}
				map.put("Dialog", elementMap);
			}
		}

	}

	/**
	 * I take an employee element and read the values in, create an Employee object and return it
	 */
	private Map getPanel(Element element) {

		HashMap panelMap = new HashMap();
		Map elementMap = new HashMap();

		String id = element.getAttribute("id");
		Integer horizontalAlignment = getAlignment(element, "HorizontalAlignment");
		if (horizontalAlignment == null) {
			panelMap.put("HorizontalAlignment", new Integer(SwingConstants.CENTER));
		}
		panelMap.put("HorizontalAlignment", horizontalAlignment);

		// for each <employee> element get text or int values of
		// name ,id, age and name
		Boolean undecorated = getBooleanValue(element, "Undecorated");
		BufferedImage bufferedImage = getBufferedImageValue(element, "Image");
		Rectangle bounds = getRectangleValue(element, "Bounds");
		Rectangle idleBounds = getRectangleValue(element, "IdleBounds");
		Map labelsMap = getLabelsMap(element, "Label");
		Map buttonsMap = getButtonsMap(element, "Button");
		Map listMap = getListsMap(element, "List");
		Map tableMap = getTableMap(element, "Table");
		Color foregroundColor = getColorValue(element, "ForegroundColor");
		Color backgroundColor = getColorValue(element, "BackgroundColor");
		Boolean opaque = getBooleanValue(element, "Opaque");

		Integer verticalGap = getIntValue(element, "VerticalGap");

		if (verticalGap != null) {
			panelMap.put("VerticalGap", verticalGap);
		}

		Integer rows = getIntValue(element, "Rows");

		if (rows != null) {
			panelMap.put("Rows", rows);
		}

		Integer cols = getIntValue(element, "Cols");

		if (cols != null) {
			panelMap.put("Cols", cols);
		}

		String fontId = getTextValue(element, "Font");
		Font font = getFont(fontId);

		if (font != null) {
			panelMap.put("Font", font);
		}

		panelMap.put("Image", bufferedImage);
		panelMap.put("Bounds", bounds);
		panelMap.put("IdleBounds", idleBounds);
		panelMap.put("BackgroundColor", backgroundColor);
		panelMap.put("ForegroundColor", foregroundColor);
		panelMap.put("Opaque", opaque);
		panelMap.put("Undecorated", undecorated);
		panelMap.put("Label", labelsMap);
		panelMap.put("List", listMap);
		panelMap.put("Table", tableMap);
		panelMap.put("Button", buttonsMap);
		elementMap.put(id, panelMap);

		return elementMap;
	}

	private Map getListsMap(Element element, String tagName) {
		Map listsMap = new HashMap();

		// get a nodelist of elements
		NodeList nl = element.getElementsByTagName("ListScrollPane");

		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {

				// get the panel element
				Element el = (Element) nl.item(i);

				// get the Element map
				listsMap.putAll(getScrollPane(el));
			}
			return listsMap;
		}
		return new HashMap();
	}

	private Map getTableMap(Element element, String tagName) {
		Map tablesMap = new HashMap();

		// get a nodelist of elements
		NodeList nl = element.getElementsByTagName("MapScrollPane");

		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {

				// get the panel element
				Element el = (Element) nl.item(i);

				// get the Element map
				tablesMap.putAll(getScrollPane(el));
			}
			return tablesMap;
		}
		return new HashMap();
	}

	private Map getScrollPane(Element element) {
		Map elementMap = new HashMap();

		String id = element.getAttribute("id");
		Boolean opaque = getBooleanValue(element, "Opaque");
		Rectangle bounds = getRectangleValue(element, "Bounds");
		JList jList = new JList();
		JournalTable journalTable = new JournalTable();
		JScrollPane jScrollPane = null;

		// get a nodelist of elements
		NodeList nl = element.getElementsByTagName("List");

		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength();) {
				// get the panel element
				Element el = (Element) nl.item(i);

				// get the Element map
				jList = getList(el);
				jScrollPane = new JScrollPane(jList);
				// E' prevista una sola JList all'interno del JScrollPane
				break;
			}
		}

		// get a nodelist of elements
		nl = element.getElementsByTagName("Table");
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength();) {
				// get the panel element
				Element el = (Element) nl.item(i);

				// get the Element map
				journalTable = getTable(el);
				journalTable.addMouseListener(new JournalTableMouseAdapter());
				jScrollPane = new JScrollPane(journalTable);
				// E' prevista una sola JTable all'interno del JScrollPane
				break;
			}
		}

		jScrollPane.setBounds(bounds);
		jScrollPane.getViewport().setOpaque(opaque.booleanValue());
		jScrollPane.setOpaque(opaque.booleanValue());
		Integer borderSize = getIntValue(element, "BorderSize");

		if (borderSize != null) {
			Border border = BorderFactory.createLineBorder(Color.black, borderSize.intValue());

			jScrollPane.setBorder(border);
		} else {
			jList.setBorder(null);
			journalTable.setBorder(null);
			jScrollPane.setBorder(null);
		}

		elementMap.put(id, jScrollPane);

		return elementMap;
	}

	private JList getList(Element element) {
		JList jList = new JList();

		String id = element.getAttribute("id");
		Boolean opaque = getBooleanValue(element, "Opaque");
		Rectangle bounds = getRectangleValue(element, "Bounds");
		Integer fixedCellHeight = getIntValue(element, "FixedCellHeight");

		if (fixedCellHeight != null) {
			jList.setFixedCellHeight(fixedCellHeight.intValue());
		}

		String fontId = getTextValue(element, "Font");
		Font font = getFont(fontId);

		if (font != null) {
			jList.setFont(font);
		}

		jList.setName(id);
		jList.setOpaque(opaque.booleanValue());
		jList.setBounds(bounds);
		((DefaultListCellRenderer) jList.getCellRenderer()).setOpaque(opaque.booleanValue());

		return jList;

	}

	private JournalTable getTable(Element element) {
		JournalTable journalTable = new JournalTable();

		String id = element.getAttribute("id");
		Boolean opaque = getBooleanValue(element, "Opaque");
		Rectangle bounds = getRectangleValue(element, "Bounds");
		Integer rowHeight = getIntValue(element, "RowHeight");

		String fontId = getTextValue(element, "Font");
		Font font = getFont(fontId);

		if (font != null) {
			journalTable.setFont(font);
		}

		journalTable.setName(id);
		journalTable.setOpaque(opaque.booleanValue());
		journalTable.setIntercellSpacing(new Dimension(0, 0));
		journalTable.setShowGrid(false);
		journalTable.setModelsMap((HashMap) getModelsMap(element, "Model"));

		journalTable.setBounds(bounds);
		if (rowHeight != null) {
			journalTable.setRowHeight(rowHeight.intValue());
		}

		return journalTable;

	}

	private Map getModel(Element element) {

		HashMap modelMap = new HashMap();
		HashMap modelParameterMap = new HashMap();
		String id = element.getAttribute("id");
		String fontId = getTextValue(element, "Font");

		DefaultTableModel journalTableModel = null;

		if (id.equalsIgnoreCase(JournalTable.MODEL_OLD)) {
			journalTableModel = new JournalTableLineModel();
			journalTableModel.setColumnCount(1);
			journalTableModel.setColumnIdentifiers(new Object[] { "" });
			modelParameterMap.put("CellRender", new JournalTableLineCellRenderer());
		} else if (id.equalsIgnoreCase(JournalTable.MODEL_LASER)) {
			journalTableModel = new JournalTableLineModel();
			journalTableModel.setColumnCount(1);
			journalTableModel.setColumnIdentifiers(new Object[] { "" });
			modelParameterMap.put("CellRender", new JournalTableLineCellRenderer());
		} else if (id.equalsIgnoreCase(JournalTable.MODEL_NEW)) {
			journalTableModel = new JournalTableItemdataModel();
			journalTableModel.setColumnCount(3);
			journalTableModel.setColumnIdentifiers(new Object[] { "", "", "" });
			modelParameterMap.put("CellRender", new JournalTableItemdataCellRenderer());
		}
		modelParameterMap.put("TableModel", journalTableModel);
		modelParameterMap.put("Font", getFont(fontId));
		modelMap.put(id, modelParameterMap);
		return modelMap;

	}

	private Map getFont(Element element) {
		Map elementMap = new HashMap();

		NodeList nl = element.getElementsByTagName("Font");

		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {

				// get the panel element
				Element el = (Element) nl.item(i);

				String id = el.getAttribute("id");

				String name = getTextValue(el, "Name");
				String styleName = getTextValue(el, "Style");
				Integer size = getIntValue(el, "Size");

				int style = Font.PLAIN;

				if (styleName.equalsIgnoreCase("BOLD")) {
					style = Font.BOLD;
				} else if (styleName.equalsIgnoreCase("ITALIC")) {
					style = Font.ITALIC;
				}

				Font font = new Font(name, style, size.intValue());

				elementMap.put(id, font);
			}
		}

		return elementMap;
	}

	private Map getRowColor(Element element) {
		Map elementMap = new HashMap();

		NodeList nl = element.getElementsByTagName("Row");

		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {

				// get the panel element
				Element el = (Element) nl.item(i);

				String id = el.getAttribute("id");

				Color foregroundColor = getColorValue(el, "ForegroundColor");

				elementMap.put(id, foregroundColor);
			}
		}

		return elementMap;
	}

	/**
	 * I take an employee element and read the values in, create an Employee object and return it
	 */
	private Map getDialog(Element element) {

		Map elementMap = new HashMap();

		String id = element.getAttribute("id");

		// for each <employee> element get text or int values of
		// name ,id, age and name
		Boolean undecorated = getBooleanValue(element, "Undecorated");
		Boolean opaque = getBooleanValue(element, "Opaque");
		Rectangle bounds = getRectangleValue(element, "Bounds");
		Map panelsMap = getPanelsMap(element, "Panel");
		Color backgroundColor = getColorValue(element, "BackgroundColor");

		HashMap dialogMap = new HashMap();

		dialogMap.put("Undecorated", undecorated);
		dialogMap.put("Opaque", opaque);
		dialogMap.put("Bounds", bounds);
		dialogMap.put("Panel", panelsMap);
		if (backgroundColor != null) {
			dialogMap.put("BackgroundColor", backgroundColor);
		}
		elementMap.put(id, dialogMap);

		return elementMap;
	}

	private Map getLabel(Element element) {
		Map elementMap = new HashMap();

		String id = element.getAttribute("id");

		Boolean opaque = getBooleanValue(element, "Opaque");
		Boolean slide = getBooleanValue(element, "Slide");
		Rectangle bounds = getRectangleValue(element, "Bounds");
		Integer horizontalAlignment = getAlignment(element, "HorizontalAlignment");
		Integer verticalAlignment = getAlignment(element, "VerticalAlignment");
		String text = getTextValue(element, "Text");

		GdLabel gdLabel = new GdLabel("");

		gdLabel.setName(id);
		gdLabel.setSlide(slide.booleanValue());

		gdLabel.setBounds(bounds);

		Integer borderSize = getIntValue(element, "BorderSize");

		if (borderSize != null) {
			Border border = BorderFactory.createLineBorder(Color.black, borderSize.intValue());

			gdLabel.setBorder(border);
		}

		String fontId = getTextValue(element, "Font");
		Font font = getFont(fontId);

		if (font != null) {
			gdLabel.setFont(font);
		}

		BufferedImage image = getBufferedImageValue(element, "Image");
		String imageStr = getTextValue(element, "Image"); // BGE_GOTOPOS#A

		// gdLabel.setImage(image); //BGE_GOTOPOS#D
		gdLabel.setImage(null, imageStr); // BGE_GOTOPOS#A

		Color backgroundColor = getColorValue(element, "BackgroundColor");
		if (backgroundColor != null) {
			gdLabel.setBackground(backgroundColor);
		}
		Color foregroundColor = getColorValue(element, "ForegroundColor");
		if (foregroundColor != null) {
			gdLabel.setForeground(foregroundColor);
		}
		String prefix = getTextValue(element, "Prefix");

		gdLabel.setPrefix(prefix);
		String suffix = getTextValue(element, "Suffix");

		gdLabel.setSuffix(suffix);

		gdLabel.setText(text);
		gdLabel.setOpaque(opaque.booleanValue());

		if (horizontalAlignment != null) {
			gdLabel.setHorizontalAlignment(horizontalAlignment.intValue());
			gdLabel.setHorizontalTextPosition(horizontalAlignment.intValue());
		}
		if (verticalAlignment != null) {
			gdLabel.setVerticalAlignment(verticalAlignment.intValue());

			// TODO MMS-R10 sistemare questa cosa...
			gdLabel.setVerticalTextPosition(verticalAlignment.intValue());
			if (verticalAlignment.intValue() == GdLabel.BOTTOM) {
				gdLabel.setIconTextGap(-gdLabel.getHeight() / 3);
			} else if (verticalAlignment.intValue() == GdLabel.TOP) {
				gdLabel.setIconTextGap(-gdLabel.getHeight());
			}
		}

		HashMap stateMap = new HashMap();
		Map stateParametersMap = new HashMap();
		stateParametersMap.put("BackgroundColor", (backgroundColor == null ? Color.white : backgroundColor));
		stateParametersMap.put("ForegroundColor", (foregroundColor == null ? Color.black : foregroundColor));
		stateParametersMap.put("Font", font);
		stateParametersMap.put("Image", image);
		stateMap.put("Normal", stateParametersMap);
		stateParametersMap = new HashMap();
		stateParametersMap.put("BackgroundColor", new Color(170, 170, 170));
		stateMap.put("Disable", stateParametersMap);

		// get a nodelist of elements
		NodeList nl = element.getElementsByTagName("State");
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {
				stateParametersMap = new HashMap();
				// get the panel element
				Element el = (Element) nl.item(i);
				String stateId = el.getAttribute("id");
				stateParametersMap.put("BackgroundColor", getColorValue(el, "BackgroundColor"));
				stateParametersMap.put("ForegroundColor", getColorValue(el, "ForegroundColor"));
				stateParametersMap.put("Font", getFont(getTextValue(el, "Font")));
				stateParametersMap.put("Image", getBufferedImageValue(el, "Image"));
				stateMap.put(stateId, stateParametersMap);
			}
		}

		gdLabel.setStateMap(stateMap);

		elementMap.put(id, gdLabel);

		return elementMap;
	}

	private Font getFont(String fontId) {
		if (fontId != null) {
			Map fontMap = (Map) map.get("Fonts");

			if (fontMap != null) {
				return (Font) fontMap.get(fontId);
			}
		}
		return null;
	}

	private Map getButton(Element element) {
		Map elementMap = new HashMap();
		KeyPadButton keyPadButton = new KeyPadButton("");

		String id = element.getAttribute("id");

		keyPadButton.setName(id);

		Integer horizontalAlignment = getAlignment(element, "HorizontalAlignment");

		if (horizontalAlignment != null) {
			keyPadButton.setHorizontalAlignment(horizontalAlignment.intValue());
		}

		Integer verticalAlignment = getAlignment(element, "VerticalAlignment");
		if (verticalAlignment != null) {
			keyPadButton.setVerticalAlignment(verticalAlignment.intValue());
			keyPadButton.setVerticalTextPosition(verticalAlignment.intValue());
		}

		String text = getTextValue(element, "Text");

		if (text != null) {
			int indexOfLineBreak = text.indexOf("\\n");

			if (indexOfLineBreak >= 0) {
				String alignment = getTextValue(element, "HorizontalAlignment");

				if (alignment == null) {
					alignment = "left";
				}
				text = "<html><p align=\"" + alignment.toLowerCase() + "\">" + text.substring(0, indexOfLineBreak)
						+ "<br>" + text.substring(indexOfLineBreak + "\\n".length()) + "</p></html>";
			}
		}
		keyPadButton.setText(text);

		keyPadButton.setHorizontalTextPosition(SwingConstants.CENTER);

		Font font = getFont(getTextValue(element, "Font"));
		if (font != null) {
			keyPadButton.setFont(font);
		}

		BufferedImage image = getBufferedImageValue(element, "Image");

		keyPadButton.setImage(image);

		Boolean opaque = getBooleanValue(element, "Opaque");

		keyPadButton.setOpaque(opaque.booleanValue());

		Rectangle bounds = getRectangleValue(element, "Bounds");

		keyPadButton.setBounds(bounds);

		Color backgroundColor = getColorValue(element, "BackgroundColor");
		if (backgroundColor != null) {
			keyPadButton.setBackground(backgroundColor);
		}

		Color foregroundColor = getColorValue(element, "ForegroundColor");
		if (foregroundColor != null) {
			keyPadButton.setForeground(foregroundColor);
		}

		Color pressedColor = getColorValue(element, "PressedColor");

		if (pressedColor != null) {
			keyPadButton.setPressedColor(pressedColor);
		}

		int keyCode = getHexValue(element, "KeyCode");

		keyPadButton.setKeyCode(keyCode);

		// MMS-LOTTERY-VAR1#A BEGIN
		try {
			char keyChar = (char) getHexValue(element, "KeyChar");
			keyPadButton.setKeyChar(keyChar);
		} catch (Exception e) {
		}
		// MMS-LOTTERY-VAR1#A END

		HashMap stateMap = new HashMap();
		Map stateParametersMap = new HashMap();
		stateParametersMap.put("BackgroundColor", (backgroundColor == null ? Color.white : backgroundColor));
		stateParametersMap.put("ForegroundColor", (foregroundColor == null ? Color.black : foregroundColor));
		stateParametersMap.put("Font", font);
		stateParametersMap.put("Image", image);
		stateMap.put("Normal", stateParametersMap);
		stateParametersMap = new HashMap();
		stateParametersMap.put("BackgroundColor", new Color(170, 170, 170));
		stateMap.put("Disable", stateParametersMap);

		// get a nodelist of elements
		NodeList nl = element.getElementsByTagName("State");
		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {
				stateParametersMap = new HashMap();
				// get the panel element
				Element el = (Element) nl.item(i);
				String stateId = el.getAttribute("id");
				stateParametersMap.put("BackgroundColor", getColorValue(el, "BackgroundColor"));
				stateParametersMap.put("ForegroundColor", getColorValue(el, "ForegroundColor"));
				stateParametersMap.put("Font", getFont(getTextValue(el, "Font")));
				stateParametersMap.put("Image", getBufferedImageValue(el, "Image"));
				stateMap.put(stateId, stateParametersMap);
			}
		}

		keyPadButton.setStateMap(stateMap);

		elementMap.put(id, keyPadButton);
		return elementMap;
	}

	private int getHexValue(Element element, String tagName) {
		try {
			return Integer.parseInt(getTextValue(element, tagName).substring(2), 16);
		} catch (Exception exception) {
			return 0;
		}
	}

	private Color getColorValue(Element element, String tagName) {
		try {
			StringTokenizer stringTokenizer = new StringTokenizer(getTextValue(element, tagName), ",");
			int r, g, b;
			int a = 255;

			r = Integer.parseInt(((String) stringTokenizer.nextElement()).trim());
			g = Integer.parseInt(((String) stringTokenizer.nextElement()).trim());
			b = Integer.parseInt(((String) stringTokenizer.nextElement()).trim());
			try {
				a = Integer.parseInt(((String) stringTokenizer.nextElement()).trim());
			} catch (Exception exception) {
			}
			return new Color(r, g, b, a);
		} catch (Exception exception) {
			return null;
		}
	}

	private String getTextValue(Element ele, String tagName) {
		String textVal = null;

		try {
			NodeList nl = ele.getElementsByTagName(tagName);

			if (nl != null && nl.getLength() > 0) {
				Element el = (Element) nl.item(0);

				textVal = el.getFirstChild().getNodeValue();
			}
		} catch (Exception exception) {
		}

		return textVal;
	}

	private Integer getIntValue(Element ele, String tagName) {
		// in production application you would catch the exception
		try {
			return new Integer(getTextValue(ele, tagName));
		} catch (Exception exception) {
			return null;
		}
	}

	private Boolean getBooleanValue(Element element, String tagName) {
		return new Boolean(getTextValue(element, tagName));
	}

	private Integer getAlignment(Element element, String tagName) {
		String s = getTextValue(element, tagName);

		if (s != null) {
			if (s.equalsIgnoreCase("CENTER")) {
				return new Integer(JLabel.CENTER);
			} else if (s.equalsIgnoreCase("RIGHT")) {
				return new Integer(JLabel.RIGHT);
			} else if (s.equalsIgnoreCase("LEFT")) {
				return new Integer(JLabel.LEFT);
			} else if (s.equalsIgnoreCase("BOTTOM")) {
				return new Integer(JLabel.BOTTOM);
			} else if (s.equalsIgnoreCase("TOP")) {
				return new Integer(JLabel.TOP);
			}
		}
		return null;

	}

	private Rectangle getRectangleValue(Element ele, String tagName) {
		try {
			StringTokenizer stringTokenizer = new StringTokenizer(getTextValue(ele, tagName), ",");
			int x = Integer.parseInt(((String) stringTokenizer.nextElement()).trim());
			int y = Integer.parseInt(((String) stringTokenizer.nextElement()).trim());
			int width = Integer.parseInt(((String) stringTokenizer.nextElement()).trim());
			int height = Integer.parseInt(((String) stringTokenizer.nextElement()).trim());

			return new Rectangle(x, y, width, height);
		} catch (Exception exception) {
			return new Rectangle();
		}
	}

	private BufferedImage getBufferedImageValue(Element ele, String tagName) {
		try {
			File f = new File(getTextValue(ele, tagName));

			if (f.exists()) {
				ImageIcon icon = new ImageIcon(f.getAbsolutePath());
				BufferedImage gif = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
						BufferedImage.TYPE_INT_ARGB);
				Graphics g = gif.createGraphics();

				icon.paintIcon(null, g, 0, 0);
				g.dispose();
				return gif;
			}
			return null;
		} catch (Exception exception) {
			return null;
		}
	}

	private Map getPanelsMap(Element ele, String tagName) {

		Map labelsMap = new HashMap();

		// get a nodelist of elements
		NodeList nl = ele.getElementsByTagName("Panel");

		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {

				// get the panel element
				Element el = (Element) nl.item(i);

				// get the Element map
				labelsMap.putAll(getPanel(el));
			}
			return labelsMap;
		}
		return new HashMap();
	}

	private Map getLabelsMap(Element ele, String tagName) {

		Map labelsMap = new HashMap();

		// get a nodelist of elements
		NodeList nl = ele.getElementsByTagName("Label");

		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {

				// get the panel element
				Element el = (Element) nl.item(i);

				// get the Element map
				labelsMap.putAll(getLabel(el));
			}
			return labelsMap;
		}
		return new HashMap();
	}

	private Map getModelsMap(Element ele, String tagName) {

		Map modelsMap = new HashMap();

		// get a nodelist of elements
		NodeList nl = ele.getElementsByTagName("Model");

		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {

				// get the panel element
				Element el = (Element) nl.item(i);

				// get the Element map
				modelsMap.putAll(getModel(el));
			}
			return modelsMap;
		}
		return new HashMap();
	}

	private Map getButtonsMap(Element ele, String tagName) {

		Map buttonsMap = new HashMap();

		// get a nodelist of elements
		NodeList nl = ele.getElementsByTagName("Button");

		if (nl != null && nl.getLength() > 0) {
			for (int i = 0; i < nl.getLength(); i++) {

				// get the panel element
				Element el = (Element) nl.item(i);

				// get the Element map
				buttonsMap.putAll(getButton(el));
			}
			return buttonsMap;
		}
		return new HashMap();
	}

	public Map getRowColorsMap() {
		return (HashMap) map.get("Rows");
	}

	public final Object getDialogElement(String dialogId, String elementId) {
		Map dialogMap = (Map) map.get("Dialog");

		if (dialogMap == null) {
			return null;
		}
		Map elementMap = (Map) dialogMap.get(dialogId);

		if (elementMap == null) {
			return null;
		}
		return elementMap.get(elementId);
	}

	public final Object getPanelElement(String panelId, String elementId) {
		Map panelMap = (Map) map.get("Panel");

		if (panelMap == null) {
			return null;
		}
		return getPanelElement(panelMap, panelId, elementId);
	}

	public final Object getPanelElement(Map map, String panelId, String elementId) {
		Map elementMap = (Map) map.get(panelId);

		if (elementMap == null) {
			return null;
		}
		return elementMap.get(elementId);
	}

}
