/**
 * 
 */
package com.ncr.gui.executor.impl2;

import com.ncr.gui.executor.ExecutorCompletitionService;

/**
 * 
 * 
 * @author Matteo
 *
 */
public class ExecutorCompletitionServiceImpl2 implements ExecutorCompletitionService {

	private static ExecutorCompletitionService instance;

	private Executor executor;

	private ExecutorCompletitionServiceImpl2() {
		super();
		this.executor = new Executor();
		Thread thread = new Thread(this.executor);
		thread.setName("Executor");
		thread.start();
	}

	public static ExecutorCompletitionService getInstance() {
		if (instance == null) {
			instance = new ExecutorCompletitionServiceImpl2();
		}
		return instance;
	}

	/* (non-Javadoc)
	 * @see prot.ExecutorCompletitionService#submit(java.lang.Runnable)
	 */
	public void submit(Runnable runnable) {
		executor.submit(runnable);
	}

	public Object poll() {
		return executor.poll();
	}

	public void submit(Thread thread) {
		executor.submit(thread);
	}
}
