package com.ncr.gui;

import com.ncr.GdPos;
import com.ncr.JournalAndDetail;
import com.ncr.UtilLog4j;

import java.awt.event.ActionEvent;

public class VoidCurrentThread extends Thread {

	private static long id = 0;
	ActionEvent actionEvent;

	public VoidCurrentThread(ActionEvent actionEvent) {
		super();
		this.actionEvent = actionEvent;
		setName("VoidCurrentThread-" + id++);
	}

	public synchronized void run() {
		UtilLog4j.logInformation(this.getClass(), "Started!");
		GdPos.panel.startWaitTread();

		JournalAndDetail.getInstance().voidCurrent();

		GdPos.panel.interruptWaitTread();
		if (GdPos.panel.modal == null) {
			if (GdPos.panel.executorCompletionService.poll() != null) { // FLM-THREADS#A
				GdPos.panel.startWaitTread();
			}
		}
		UtilLog4j.logInformation(this.getClass(), "Ended!");
	}

}
