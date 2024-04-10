package com.ncr.gui;

import com.ncr.Action;
import com.ncr.GdPos;
import com.ncr.UtilLog4j;

import java.awt.event.ActionEvent;

public class ActionThread extends Thread {

	private static long id = 0;
	private volatile ActionEvent actionEvent = null;

	public ActionThread(ActionEvent actionEvent) {
		super();
		this.actionEvent = actionEvent;
		setName("ActionThread-" + id++);
	}

	public synchronized void run() {
		UtilLog4j.logInformation(this.getClass(), "Started!");
		GdPos.panel.startWaitTread();
		int code = Integer.parseInt(actionEvent.getActionCommand().substring(4), 16);
		UtilLog4j.logInformation(this.getClass(), "Accepting " + code);
		if (code > 0) {
			code = Action.input.accept(code);
			if (code >= 0) {
				code = GdPos.panel.eventMain(code);
				if (code >= 0) {
					GdPos.panel.eventStop(code);
				}
			}
		}

		GdPos.panel.innerList.remove(actionEvent);
		GdPos.panel.interruptWaitTread();
		UtilLog4j.logInformation(this.getClass(), "Ended!");
	}

	public ActionEvent getActionEvent() {
		return actionEvent;
	}

}
