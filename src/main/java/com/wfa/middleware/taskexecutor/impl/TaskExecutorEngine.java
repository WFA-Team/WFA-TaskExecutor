package com.wfa.middleware.taskexecutor.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wfa.middleware.taskexecutor.api.IPrioritizedRunnable;
import com.wfa.middleware.taskexecutor.api.ITaskElement;
import com.wfa.middleware.taskexecutor.api.ITaskExecutorEngine;
import com.wfa.middleware.utils.beans.api.IThreadPoolFactory;

@Component
public class TaskExecutorEngine extends ExecutorEngine<ITaskElement<?>> implements ITaskExecutorEngine {

	@Autowired
	public TaskExecutorEngine(IThreadPoolFactory<IPrioritizedRunnable> threadPoolFactory) {
		super(threadPoolFactory);
	}
}
