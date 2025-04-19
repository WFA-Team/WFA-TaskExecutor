package com.wfa.middleware.taskexecutor.api;

import com.wfa.middleware.utils.api.IJoinable;

public interface IGroupedTaskElement<R extends IJoinable<R>> extends ITaskElement<R> {
	// This is the task that is safe to be executed in parallel with other tasks
	void addParallelTask(ITaskElement<R> subTask); 
}
