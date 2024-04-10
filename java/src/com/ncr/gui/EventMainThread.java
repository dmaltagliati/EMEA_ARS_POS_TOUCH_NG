package com.ncr.gui;

import com.ncr.*;
import org.apache.commons.lang.ArrayUtils;

import java.awt.event.KeyEvent;

public class EventMainThread extends Thread {
	private static long id = 0;
	private volatile KeyEvent e;

	public EventMainThread(KeyEvent e) {
		super();
		this.e = e;
		setName("EventMainThread-" + id++);
	}

	public synchronized void run() {

		UtilLog4j.logInformation(this.getClass(), "Started!");

		GdPos.panel.startWaitTread();



		int code = Action.input.keyBoard(e);


		UtilLog4j.logInformation(this.getClass(),
				"[" + KeyEvent.getKeyText(e.getKeyCode()) + "]" + "[0x" + Integer.toHexString(Action.input.key) + "]");
		UtilLog4j.logInformation(this.getClass(), "input.pb = " + Action.input.pb);


		if (code >= 0) {
			code = GdPos.panel.eventMain(code);
			if (code >= 0) {
				GdPos.panel.eventStop(code);
			}
		}



		UtilLog4j.logInformation(this.getClass(), "Ended!");

		GdPos.panel.interruptWaitTread();
	}

}
