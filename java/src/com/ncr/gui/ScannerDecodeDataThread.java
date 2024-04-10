package com.ncr.gui;

import com.ncr.*;
import jpos.ScannerConst;

import java.awt.event.ActionEvent;
import java.util.StringTokenizer;

public class ScannerDecodeDataThread extends Thread {
	private static long id = 0;

	private volatile ActionEvent event;

	public ScannerDecodeDataThread(ActionEvent event) {
		super();
		this.event = event;
		setName("ScannerDecodeDataThread-" + id++);
	}

	public synchronized void run() {

		UtilLog4j.logInformation(this.getClass(), "Started!");
		GdPos.panel.startWaitTread();

		StringTokenizer tokenizer = new StringTokenizer(event.getActionCommand(), ":");
		String tokenCmd = tokenizer.nextToken();
		int tokenScanType = Integer.parseInt(tokenizer.nextToken());
		String tokenScanLabel = tokenizer.nextToken();

		UtilLog4j.logInformation(this.getClass(), "         tokenCmd: " + tokenCmd);
		UtilLog4j.logInformation(this.getClass(), "    tokenScanType: " + tokenScanType);
		UtilLog4j.logInformation(this.getClass(), "   tokenScanLabel: " + tokenScanLabel);

		UtilLog4j.logInformation(this.getClass(),
				"TokenCmd: " + tokenCmd + "; tokenScanType: " + tokenScanType + "; tokenScanLabel: " + tokenScanLabel);

		UtilLog4j.logInformation(this.getClass(),
				"tokenScanType equals " + (tokenScanType == ScannerConst.SCAN_SDT_Code39));
		UtilLog4j.logInformation(this.getClass(), "tokenScanLabel equals " + tokenScanLabel.trim().startsWith("A"));


		Action.input.setScanType(tokenScanType);
		Action.input.setScanLabel(tokenScanLabel);

		UtilLog4j.logInformation(this.getClass(), "[" + tokenScanType + "-" + tokenScanLabel + "]");

		if (GdPos.panel.modal == null) {
			GdPos.panel.eventMain(Action.input.labelFromScanner(tokenScanLabel));
		} else if (GdPos.panel.modal instanceof ScanDlg || GdPos.panel.modal instanceof AbcDlg
				|| GdPos.panel.modal instanceof AbcInputDlg) { // MMS-LOTTERY#A
			GdPos.panel.modal.modalMain(Action.input.labelFromScanner(tokenScanLabel));
		}

		GdPos.panel.interruptWaitTread();
		UtilLog4j.logInformation(this.getClass(), "Ended!");

	}

}
