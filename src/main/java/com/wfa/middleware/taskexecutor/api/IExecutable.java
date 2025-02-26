package com.wfa.middleware.taskexecutor.api;

import com.wfa.middleware.utils.AsyncPromise;
import com.wfa.middleware.utils.JoinVoid;

// TODO-> See if there is a requirement to make result type of a task templatized
public interface IExecutable extends Comparable<IExecutable>{
	void preexecute();
	void execute();
	void postexecute(AsyncPromise<JoinVoid> promise); // To mark and notify the task as complete(success or failure)
	void setPriorityWeight(int priority);
	int getPriorityWeight();
}
