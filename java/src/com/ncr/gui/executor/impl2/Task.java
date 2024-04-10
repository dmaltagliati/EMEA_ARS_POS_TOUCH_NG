package com.ncr.gui.executor.impl2;

class Task extends Thread {

	private Thread thread;

	private Object lock;

	public Task(Thread thread, Object lock) {
		this.thread = thread;
		this.lock = lock;
	}

	public Task(Runnable runnable, Object lock) {
		Thread thread = new Thread(runnable);
		this.thread = thread;
		this.lock = lock;
	}

	public void run() {
		synchronized (this.lock) {
			this.thread.run();
			this.lock.notifyAll();
		}
	}

	public Thread getThread() {
		return this.thread;
	}

}
