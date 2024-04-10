package com.ncr.gui;

import com.ncr.GdPos;
import com.ncr.UtilLog4j;

public class WaitThread extends Thread {
	public final int MSEC_BEFORE_WAIT = 1000;
	private static long id = 0;

	public WaitThread() {
		super();
		setName("WaitThread-" + id++);
	}

	public void run() {

		if (GdPos.panel.waitPanel == null) {
			UtilLog4j.logDebug(this.getClass(), "Not Enabled!");
			return;
		}

		UtilLog4j.logInformation(this.getClass(), "Start!");

		long timeBeforeWait = System.currentTimeMillis() + MSEC_BEFORE_WAIT;

		boolean first = true;
		while (true) {

			try {
				Thread.sleep(1);
			} catch (InterruptedException interruptedException) {
				UtilLog4j.logDebug(this.getClass(), "Interrupted!");
				break;
			}

			if (Thread.currentThread().isInterrupted()) {
				UtilLog4j.logDebug(this.getClass(), "CurrentThread is Interrupted!");
				break;
			}

			if (first && System.currentTimeMillis() > timeBeforeWait) {
				first = false;
				UtilLog4j.logInformation(this.getClass(), "Activate WaitPanel!");
				GdPos.panel.wait(true);
				continue;
			}

		}

		UtilLog4j.logInformation(this.getClass(), "Deactivate WaitPanel!");
		GdPos.panel.wait(false);
	}

}

