package com.ncr.gui;

import com.ncr.*;

import java.awt.event.ActionEvent;

public class ScannerThread extends Thread {
	private static long id = 0;
	private volatile ActionEvent event;

	public ScannerThread(ActionEvent event) {
		super();
		this.event = event;
		setName("ScannerThread-" + id++);
	}

	public synchronized void run() {

		UtilLog4j.logInformation(this.getClass(), "Started!");
		GdPos.panel.startWaitTread();
		Struc.tra.testStarted = true;

		String barcode = event.getActionCommand();
		
		//MMS-MANTIS-22355#A BEGIN
		// Workaround per un problema legato allo scanner che mandava letture senza barcode
		if (barcode.trim().length() == 0) {
			UtilLog4j.logError(this.getClass(), "Barcode vuoto...");
			GdPos.panel.interruptWaitTread();
			UtilLog4j.logInformation(this.getClass(), "Ended!");
		}
		//MMS-MANTIS-22355#A END



		int code = Action.input.label(barcode);

		UtilLog4j.logInformation(this.getClass(), "[" + barcode + "]");

		if (GdPos.panel.modal == null) {
			GdPos.panel.eventMain(code);
		} else {
			GdPos.panel.modal.modalMain(code);
		}

		GdPos.panel.interruptWaitTread();
		UtilLog4j.logInformation(this.getClass(), "Ended!");

	}

}
