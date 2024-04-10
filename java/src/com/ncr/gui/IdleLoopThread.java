package com.ncr.gui;

import com.ncr.IdleThread;
import com.ncr.UtilLog4j;

public class IdleLoopThread extends Thread {

	private static long id = 0;
	private Object lock = null;

	public IdleLoopThread(Object lock) {
		this.lock = lock;
	}

	public void run() {

		setName(this.getClass().getName() + "-" + id++);
		UtilLog4j.logDebug(this.getClass(), "Started!");

		while (true) {

			if (Thread.currentThread().isInterrupted()) {
				UtilLog4j.logDebug(this.getClass(), "Interrupted!");
				break;
			}

			synchronized (lock) {
				IdleThread idle = new IdleThread(lock);
				idle.start();
				UtilLog4j.logDebug(this.getClass(), "Action.Idle is been started");
				try {
					lock.wait();
					UtilLog4j.logDebug(this.getClass(), "Action.Idle is been executed");
				} catch (InterruptedException e) {
					UtilLog4j.logDebug(this.getClass(), "Interrupted!");
					break;
				}

				try {
					Thread.sleep(250);
				} catch (InterruptedException e) {
					UtilLog4j.logDebug(this.getClass(), "Interrupted!");
					break;
				}

			}

		}
	}
}
