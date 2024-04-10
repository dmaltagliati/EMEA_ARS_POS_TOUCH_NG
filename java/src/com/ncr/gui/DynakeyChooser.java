package com.ncr.gui;

import com.ncr.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class DynakeyChooser extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5713354525871706826L;

	private final int ORIGINAL_COLS = 5;
	private final int ORIGINAL_ROWS = 4;

	private BufferedImage chooserImage = null;

	private GdLabel chooser = new GdLabel("");
	private int rows = ORIGINAL_ROWS;
	private int cols = ORIGINAL_COLS;
	private int gap = 0;

	public DynakeyChooser() {

		setLayout(null);
		setName("chooser");
		setVisible(false);

		// Non carico nulla se non ho la entry nell'xml
		Object object = ArsXmlParser.getInstance().getPanelElement("Chooser", "Bounds");

		if (object == null) {
			return;
		}

		setOpaque(((Boolean) ArsXmlParser.getInstance().getPanelElement("Chooser", "Opaque")).booleanValue());
		setBounds((Rectangle) ArsXmlParser.getInstance().getPanelElement("Chooser", "Bounds"));
		Map labelsMap = (HashMap) ArsXmlParser.getInstance().getPanelElement("Chooser", "Label");

		Color backgroundColor = (Color) ArsXmlParser.getInstance().getPanelElement("Chooser", "BackgroundColor");

		if (backgroundColor != null) {
			setBackground(backgroundColor);
		}

		try {
			BufferedImage backgroundImage = (BufferedImage) ArsXmlParser.getInstance().getPanelElement("Chooser",
					"Image");

			chooserImage = backgroundImage.getSubimage(getX(), getY(), getWidth(), getHeight());
		} catch (Exception exception) {
		}

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

		chooser.setLayout(new GridLayout(rows, cols));
		add(chooser);

	}

	public void setImage(String path, String name) {
		UtilLog4j.logInformation(this.getClass(), "path=" + path + "; name=" + name);
		String directoryName = name.substring(0, name.indexOf(".GIF"));

		chooser.addMouseListener(new MouseAdapter() {

			public void mouseReleased(MouseEvent e) {
				e.consume();
			}

		});
		chooser.removeAll();
		chooser.setVisible(false);
		if (chooser.getIcon() == null) {
			// Server per far si che il Chooser sia visibile, in quanto se non
			// ha un'immagine di sfondo
			// non viene visualizzato.
			chooser.setImage(new BufferedImage(chooser.getWidth(), chooser.getHeight(), BufferedImage.TYPE_4BYTE_ABGR));
		}

		GdLabel gdLabel = new GdLabel("");

		gdLabel.setImage(path, name);
		if (gdLabel.getIcon() != null) {

			int cellWidth = (gdLabel.getIcon().getIconWidth() / ORIGINAL_COLS);
			int cellHeight = (gdLabel.getIcon().getIconHeight() / ORIGINAL_ROWS);

			int labelIndex = 0;

			for (int row = 0; row < ORIGINAL_ROWS; row++) {
				for (int col = 0; col < ORIGINAL_COLS; col++) {
					// GdLabel label = createCell(FmtIo.editNum(++labelIndex, 2));
					GdLabel label = new GdLabel("");
					label.setName("LIST" + FmtIo.editNum(++labelIndex, 2));
					label.setSize(chooser.getIcon().getIconWidth() / cols, chooser.getIcon().getIconHeight() / rows);
					label.setBorder(BorderFactory.createLineBorder(getBackground(), gap));

					label.setImage(gdLabel.getBufferedImage().getSubimage(col * cellWidth, row * cellHeight, cellWidth,
							cellHeight));
					label.setHorizontalTextPosition(JLabel.CENTER);
					label.setVerticalTextPosition(JLabel.CENTER);
					label.addMouseListener(new MouseAdapter() {
						public void mouseReleased(MouseEvent e) {
							if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == 0) {
								return;
							}
							DevIo.postInput(((GdLabel) e.getSource()).getName(), null);
							e.consume();
							GdPos.playBeep();
						}
					});

					chooser.add(label);
				}
			}
		} else {
			String key = directoryName.substring(directoryName.indexOf("_") + 1);
			int cellWidth = (chooser.getIcon().getIconWidth() / cols);
			int cellHeight = (chooser.getIcon().getIconHeight() / rows);

			int maxImages = cols * rows;

			int rec = 0;
			while (Table.lREF.read(++rec) > 0) {
				if (Table.lREF.scan(4).equals(key)) {
					JPanel labelPanel = new JPanel();

					labelPanel.setLayout(new GridBagLayout());
					labelPanel.setBorder(BorderFactory.createLineBorder(getBackground(), gap));
					labelPanel.setOpaque(true);
					labelPanel.setBackground(chooser.getBackground());
					GridBagConstraints gbc = new GridBagConstraints();

					gbc.fill = GridBagConstraints.NONE;
					gbc.anchor = GridBagConstraints.CENTER;
					gbc.gridx = 0;
					gbc.gridy = 0;
					gbc.weightx = 1;
					gbc.weighty = 0.75;
					// GdLabel label = createCell(LinIo.editNum(labelIndex++, 2));
					GdLabel label = new GdLabel("");
					int size = (int) Math.min(cellWidth * gbc.weightx, cellHeight * gbc.weighty);
					label.setPreferredSize(new Dimension(size, size));
					label.setSize(label.getPreferredSize());
					label.setBorder(null);
					label.setBackground(chooser.getBackground());
					labelPanel.add(label, gbc);
					gbc.fill = GridBagConstraints.HORIZONTAL;
					gbc.anchor = GridBagConstraints.SOUTH;
					gbc.gridy = 1;
					gbc.weightx = 1;
					gbc.weighty = 0.25;

					GdLabel labelInfo = new GdLabel("");

					labelInfo.setHorizontalAlignment(SwingConstants.CENTER);
					labelInfo.setOpaque(true);
					labelInfo.setBackground(chooser.getForeground());
					labelInfo.setForeground(chooser.getBackground());
					labelInfo.setFont(chooser.getFont());

					String id = Table.lREF.scan(':').scan(4).trim();
					// String labelId = "LIST" + id;
					String imageName = "ITM_" + Table.lREF.skip().scan(16).trim() + ".GIF";

					FontMetrics fontMetrics = chooser.getFontMetrics(chooser.getFont());
					labelInfo.setText(Table.lREF.skip().skip(4).skip().scan(20).trim());
					int width = fontMetrics.stringWidth(labelInfo.getText());
					labelInfo.setPreferredSize(new Dimension(width, fontMetrics.getHeight()));
					labelInfo.setSize(labelInfo.getPreferredSize());
					labelInfo.setSlide(chooser.isSlide());
					if (width > cellWidth) {
						labelInfo.slide(20);
					}
					// label.setName(labelId);
					label.setImage("gif", imageName);
					if (label.getIcon() == null) {
						label.setImage("touch", "imageMissing.png");
					}
					labelPanel.add(labelInfo, gbc);
					labelPanel.setName("LIST" + FmtIo.leftFill(id, 2, '0'));
					labelPanel.addMouseListener(new MouseAdapter() {
						public void mouseReleased(MouseEvent e) {
							if ((e.getModifiers() & InputEvent.BUTTON1_MASK) == 0) {
								return;
							}
							DevIo.postInput(((Component) e.getSource()).getName(), null);
							e.consume();
							GdPos.playBeep();
						}
					});

					chooser.add(labelPanel);
					maxImages--;
				}
			}
			for (int i = 0; i < maxImages; i++) {
				GdLabel label = new GdLabel("");

				label.setBorder(BorderFactory.createLineBorder(getBackground(), gap));
				chooser.add(label);
			}
		}
		Struc.tra.testStarted = true;
		chooser.setVisible(true);

	}

	public Icon getImage() {
		return chooser.getIcon();
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (chooserImage != null) {
			g.drawImage(chooserImage, 0, 0, getWidth(), getHeight(), null);
		}
	}

}
