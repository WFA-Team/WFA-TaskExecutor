package com.wfa.middleware.taskexecutor.api;

import com.wfa.middleware.utils.AsyncPromise;

public interface IExecutable<R> extends Comparable<IExecutable<?>> {
	void preexecute();
	void execute();
	void postexecute(AsyncPromise<R> promise); // To mark and notify the task as complete(success or failure)
	void setPriorityWeight(int priority);
	int getPriorityWeight();
}
