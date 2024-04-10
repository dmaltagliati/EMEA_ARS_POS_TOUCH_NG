/**
 * 
 */
package com.ncr.gui.executor.impl2;

import com.ncr.UtilLog4j;

import java.util.List;

/**
 * @author Matteo
 *
 */
class Executor implements Runnable {

	private List queue = new SynchonizedList();

	private Object lock = new Object();

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		synchronized (lock) {
			while (true) {
				checkTasksInQueue();
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	private void checkTasksInQueue() {
		if (!queue.isEmpty()) {
			// System.out.println("queue is not empty. Size: " + queue.size());
			Task task = (Task) queue.get(0);
			String threadName = task.getName();
			UtilLog4j.logDebug(this.getClass(), threadName + " removed");
			UtilLog4j.logDebug(this.getClass(), "the queue is: " + queue);
			task.start();
			UtilLog4j.logDebug(this.getClass(), threadName + " is running");
			try {
				lock.wait();
				UtilLog4j.logDebug(this.getClass(), "End wait");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			queue.remove(task);

		}
	}

	public void submit(Runnable runnable) {
		Task task = new Task(runnable, lock);
		queue.add(task);
		UtilLog4j.logDebug(this.getClass(), "the queue is: " + queue);
	}

	public synchronized void submit(Thread thread) {
		Task task = new Task(thread, lock);
		task.setName(thread.getName());
		queue.add(task);
		UtilLog4j.logDebug(this.getClass(), "the queue is: " + queue);
	}

	public synchronized Thread poll() {
		if (queue.isEmpty())
			return null;
		else {
			Task task = (Task) queue.get(0);
			Thread thread = task.getThread();
			return thread;
		}
	}

}
