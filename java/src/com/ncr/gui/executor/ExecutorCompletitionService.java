/**
 * 
 */
package com.ncr.gui.executor;

/**
 * Interface to execute threads sequentially.
 * When a thread is submitted, it waits the termination of each thread that has been previously
 * submitted and not terminated yet. 
 * 
 * 
 * @author Matteo
 *
 */
public interface ExecutorCompletitionService {

	public void submit(Runnable runnable);

	public void submit(Thread thread);

	public Object poll();

}
