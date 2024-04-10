package com.ncr.gui;

import com.ncr.GdPos;
import com.ncr.UtilLog4j;

// MMS-MANTIS-14668-KEY-THREAD#A BEGIN
public class SplashThread extends Thread {

	private static long id = 0;

	public SplashThread() {
		super();
		setName("SplashThread-" + id++);
	}

	public void run() {

		if (GdPos.panel.splashPanel == null) {
			UtilLog4j.logInformation(this.getClass(), "Not Enabled!");
			return;
		}

		UtilLog4j.logInformation(this.getClass(), "Start!");

		boolean first = true;
		while (true) {

			try {
				Thread.sleep(1);
			} catch (InterruptedException interruptedException) {
				UtilLog4j.logInformation(this.getClass(), "Interrupted!");
				break;
			}

			if (Thread.currentThread().isInterrupted()) {
				UtilLog4j.logInformation(this.getClass(), "CurrentThread is Interrupted!");
				break;
			}

			if (first) {
				first = false;
				UtilLog4j.logInformation(this.getClass(), "Activate SplashPanel!");
				GdPos.panel.splash(true);
				continue;
			}

		}

		UtilLog4j.logInformation(this.getClass(), "Deactivate SplashPanel!");
		GdPos.panel.splash(false);
	}

}
// MMS-MANTIS-14668-KEY-THREAD#A END
