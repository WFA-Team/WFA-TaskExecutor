package com.wfa.middleware.taskexecutor.api;

public interface IGroupedTaskElement extends ITaskElement {
	// This is the task that is safe to be executed in parallel with other tasks
	void addParallelTask(ITaskElement subTask); 
}
