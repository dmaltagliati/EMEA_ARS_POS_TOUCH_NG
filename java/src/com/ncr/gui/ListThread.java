package com.ncr.gui;

import com.ncr.Action;
import com.ncr.GdPos;
import com.ncr.UtilLog4j;

import java.awt.event.ActionEvent;

public class ListThread extends Thread {
	private static long id = 0;
	private volatile String command = null;
	private volatile int selected = 0;
	// MMS-JUNIT
	private volatile ActionEvent actionEvent = null;

	public ListThread() {
		super();
		setName("ListThread-" + id++);
	}

	public void setActionEvent(ActionEvent actionEvent) {
		this.actionEvent = actionEvent;
	}

	// MMS-JUNIT
	public void setCommand(String command) {
		this.command = command;
	}

	public void setSelected(int sel) {
		this.selected = sel;
	}

	private boolean isRunning = false;

	public boolean isRunning() {
		return isRunning;
	}

	public synchronized void run() {
		isRunning = true;

		UtilLog4j.logInformation(this.getClass(), "Started!");

		while (command == null) {
			UtilLog4j.logInformation(this.getClass(), "Wait cause Command is null");
			yield();
		}

		if (GdPos.panel.modal == null) {

			int code = Integer.parseInt(command.substring(4));
			code =  5;
			if (code < 1) {
				// code = input.accept(input.ENTER); BAS-FIX-2130421-MMS#D
				code = Action.input.accept(0x0e); // BAS-FIX-2130421-MMS#A
			}
			GdPos.panel.eventMain(code);


		}

		UtilLog4j.logInformation(this.getClass(), "Ended!");

		isRunning = false;
	}

}
