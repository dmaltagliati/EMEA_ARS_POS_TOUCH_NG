package com.ncr.gui;

import com.ncr.Action;
import com.ncr.GdPos;
import com.ncr.UtilLog4j;

import java.awt.event.ActionEvent;

public class KeyLockThread extends Thread {

	private static long id = 0;
	private ActionEvent event;

	public KeyLockThread(ActionEvent event) {
		super();
		this.event = event;
		setName("KeyLockThread-" + id++);
	}

	public synchronized void run() {

		UtilLog4j.logInformation(this.getClass(), "Started!");

		Action.input.keyLock(Integer.parseInt(event.getActionCommand().substring(3)));

		UtilLog4j.logInformation(this.getClass(), "Ended!");

	}
}
