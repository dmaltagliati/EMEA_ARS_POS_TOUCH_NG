package com.ncr.gui;

import com.ncr.ArsXmlParser;
import com.ncr.GdPos;
import com.ncr.GoToPosManager;
import com.ncr.UtilLog4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;

public class BottomBarDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4832498188037136127L;
	private BottomBar bottomBar = null;
	private DialogThread dialogThread = null;
	private BottomBarExtenderDialog bottomBarExtenderDialog = null;
	private Rectangle originalBounds;

	public BottomBarDialog() {
		super(GdPos.panel.frame);
		UtilLog4j.logInformation(this.getClass(), getName());

		// MMS-R10
		if (GdPos.CURSOR != null) {
			setCursor(GdPos.CURSOR);
		}
		// MMS-R10

		if (GdPos.arsGraphicInterface != null) {
			GdPos.arsGraphicInterface.setUndecorated(this,
					((Boolean) ArsXmlParser.getInstance().getDialogElement("BottomBar", "Undecorated")).booleanValue());
			GdPos.arsGraphicInterface.setModalExclusionType(this, "APPLICATION_EXCLUDE");
			GdPos.arsGraphicInterface.setAlwaysOnTop(this, true);
		}

		originalBounds = (Rectangle) ArsXmlParser.getInstance().getDialogElement("BottomBar", "Bounds");
		setBounds(originalBounds);
		getRootPane().setOpaque(
				((Boolean) ArsXmlParser.getInstance().getDialogElement("BottomBar", "Opaque")).booleanValue());
		setBackground((Color) ArsXmlParser.getInstance().getDialogElement("BottomBar", "BackgroundColor"));

		bottomBar = new BottomBar();
		add(bottomBar);

		dialogThread = new DialogThread();
		dialogThread.setDialog(this);
		dialogThread.start();

		bottomBarExtenderDialog = new BottomBarExtenderDialog();
		bottomBarExtenderDialog.hideMyself();
		dialogThread = new DialogThread();
		dialogThread.setDialog(bottomBarExtenderDialog);
		dialogThread.start();

		hideMyself();
	}

	public void hideMyself() {
		UtilLog4j.logDebug(this.getClass(), "");
		// setLocation(0 - originalBounds.width, 0 - originalBounds.height);
		setBounds(new Rectangle(originalBounds.x, originalBounds.y, 1, 1));
		bottomBarExtenderDialog.hideMyself();
	}

	public void showMyself() {
		UtilLog4j.logDebug(this.getClass(), "");
		if (GoToPosManager.getInstance().isFLAndTouch()) {
			hideMyself();
			return;
		}
		setBounds(originalBounds);
	}

	public void closeExtener() {
		if (isOpened()) {
			bottomBarExtenderDialog.hideMyself();
			GdPos.panel.refreshModals();
		}
	}

	public boolean isOpened() {
		return bottomBarExtenderDialog.isOnScreen();
	}

	public void toggle() {
		if (isOpened()) {
			bottomBarExtenderDialog.hideMyself();
		} else {
			bottomBarExtenderDialog.showMyself();
		}
		bottomBarExtenderDialog.setVisible(false);
		bottomBarExtenderDialog.setVisible(true);
		GdPos.panel.refreshModals();

	}

	public synchronized void addKeyListener(KeyListener l) {
		KeyListener[] keyListeners = (KeyListener[]) getListeners(KeyListener.class);

		for (int i = 0; i < keyListeners.length; i++) {
			super.removeKeyListener(keyListeners[i]);
		}
		super.addKeyListener(l);
		bottomBarExtenderDialog.addKeyListener(l);
	}

}
