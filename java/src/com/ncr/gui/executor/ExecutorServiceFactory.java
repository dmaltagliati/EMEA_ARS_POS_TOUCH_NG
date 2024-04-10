package com.ncr.gui.executor;

import com.ncr.gui.executor.impl2.ExecutorCompletitionServiceImpl2;

public class ExecutorServiceFactory {

	public static ExecutorCompletitionService getExecutorService() {
		return ExecutorCompletitionServiceImpl2.getInstance();
	}

}
