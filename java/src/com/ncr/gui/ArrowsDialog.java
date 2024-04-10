package com.ncr.gui;

import com.ncr.ArsXmlParser;
import com.ncr.GdPos;
import com.ncr.GoToPosManager;
import com.ncr.UtilLog4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;

public class ArrowsDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7106634328952443091L;
	private Arrows arrows = null;
	private DialogThread dialogThread = null;
	private Rectangle originalBounds;

	public ArrowsDialog() {
		super(GdPos.panel.frame);
		UtilLog4j.logInformation(this.getClass(), getName());

		// MMS-R10
		if (GdPos.CURSOR != null) {
			setCursor(GdPos.CURSOR);
		}
		// MMS-R10

		if (GdPos.arsGraphicInterface != null) {
			GdPos.arsGraphicInterface.setUndecorated(this,
					((Boolean) ArsXmlParser.getInstance().getDialogElement("Arrows", "Undecorated")).booleanValue());
			GdPos.arsGraphicInterface.setModalExclusionType(this, "APPLICATION_EXCLUDE");
			GdPos.arsGraphicInterface.setAlwaysOnTop(this, true);
		}

		originalBounds = (Rectangle) ArsXmlParser.getInstance().getDialogElement("Arrows", "Bounds");
		setBounds(originalBounds);
		getRootPane()
				.setOpaque(((Boolean) ArsXmlParser.getInstance().getDialogElement("Arrows", "Opaque")).booleanValue());

		arrows = new Arrows();
		add(arrows);

		dialogThread = new DialogThread();
		dialogThread.setDialog(this);
		dialogThread.start();

		hideMyself();

	}

	public void hideMyself() {
		UtilLog4j.logDebug(this.getClass(), "");
		// setLocation(0 - originalBounds.width, 0 - originalBounds.height);
		// setBounds(new Rectangle(1, 1, 1, 1));
		setBounds(new Rectangle(originalBounds.x, originalBounds.y, 1, 1));
	}

	public void showMyself() {
		UtilLog4j.logDebug(this.getClass(), "");
		if (GoToPosManager.getInstance().isFLAndTouch()) {
			hideMyself();
			return;
		}
		setBounds(originalBounds);
	}

	public synchronized void addKeyListener(KeyListener l) {
		KeyListener[] keyListeners = (KeyListener[]) getListeners(KeyListener.class);

		for (int i = 0; i < keyListeners.length; i++) {
			super.removeKeyListener(keyListeners[i]);
		}
		super.addKeyListener(l);
	}

}
