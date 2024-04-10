package com.ncr.gui;

import java.awt.GridLayout;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.ncr.ArsXmlParser;
import com.ncr.FmtIo;

public class Dynakey extends JPanel {

	private static final long serialVersionUID = 6403479035926101526L;
	private int index;

	private GdLabel dynakeyButton = new GdLabel("");
	private GdLabel dynakeyButtonImage = new GdLabel("");
	private GdLabel dynakeyButtonSubImage = new GdLabel("");
	private GdLabel lineTop = new GdLabel("");
	private GdLabel lineBottom = new GdLabel("");
	private DynakeyGroup dyna;

	Dynakey(DynakeyGroup d) {
		super(null);
		setOpaque(false);

		Map labelsMap = (HashMap) ArsXmlParser.getInstance().getPanelElement("Dynakey", "Label");

		GdLabel defaultButton = (GdLabel) labelsMap.get("DynakeyButton");
		dynakeyButton.setLayout(new GridLayout(2, 1));
		dynakeyButton.setBounds(defaultButton.getBounds());
		dynakeyButton.setFont(defaultButton.getFont());
		dynakeyButton.setOpaque(defaultButton.isOpaque());

		GdLabel defaultButtonImage = (GdLabel) labelsMap.get("DynakeyButton");
		dynakeyButtonImage.setLayout(new GridLayout(2, 1));
		dynakeyButtonImage.setBounds(defaultButtonImage.getBounds());
		dynakeyButtonImage.setFont(defaultButtonImage.getFont());
		dynakeyButtonImage.setOpaque(defaultButtonImage.isOpaque());
		dynakeyButtonImage.setImage(defaultButtonImage.getBufferedImage());

		GdLabel defaultButtonSubImage = (GdLabel) labelsMap.get("DynakeyButtonSubImage");
		dynakeyButtonSubImage.setBounds(defaultButtonSubImage.getBounds());
		dynakeyButtonSubImage.setOpaque(defaultButtonSubImage.isOpaque());

		this.dyna = d;
		this.setFont(dynakeyButton.getFont());
		index = d.getComponentCount();
		setName("dynaKey" + index);

		lineTop.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		lineBottom.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
		lineTop.setHorizontalAlignment(defaultButton.getHorizontalAlignment());
		lineBottom.setHorizontalAlignment(defaultButton.getHorizontalAlignment());
		lineTop.setFont(defaultButton.getFont());
		lineBottom.setFont(defaultButton.getFont());
		lineTop.setForeground(defaultButton.getForeground());
		lineBottom.setForeground(defaultButton.getForeground());

		dynakeyButton.add(lineTop);
		dynakeyButton.add(lineBottom);
		add(dynakeyButton);
		add(dynakeyButtonSubImage);
		add(dynakeyButtonImage);

		addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				if ((e.getModifiers() & InputEvent.BUTTON1_MASK) > 0) {
					dyna.dkyTouch(index);
				}
			}
		});
	}

	public int getIndex() {
		return index;
	}

	public void setLineTop(String text) {
		this.lineTop.setText(text == null ? "" : text.trim());
	}

	public GdLabel getLineTop() {
		return lineTop;
	}

	public void setLineBottom(String text) {
		this.lineBottom.setText(text == null ? "" : text.trim());
	}

	public GdLabel getLineBottom() {
		return lineBottom;
	}

	public void setEnabled(boolean state) {
		super.setEnabled(state);
		setVisible(state);
		setImage();
	}

	public void reset(int align) {
		setLineTop("");
		setLineBottom("");
		setEnabled(false);
	}

	public void setImage() {
		boolean visible = dyna.getImage() == null;
		dynakeyButtonImage.setVisible(visible);
		dynakeyButtonSubImage.setVisible(visible);
		if (visible) {
			String subImageName = (dyna.getSubstate() > 0 ? "POS_DS" : "POS_DK") + FmtIo.editNum(dyna.getState(), 2)
					+ "_" + (index + 1) + ".GIF";
			dynakeyButtonSubImage.setImage("b24", subImageName);
		}
	}

}
