package com.ncr.gui;

import javax.swing.*;

public class DialogThread extends Thread {

	private JDialog dialog = new JDialog();

	public DialogThread() {
		super();
		setName("DialogThread");
	}

	public void setDialog(JDialog dialog) {
		this.dialog = dialog;
	}

	public void run() {
		dialog.setVisible(true);
	}

}
