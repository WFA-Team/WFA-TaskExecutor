package com.wfa.middleware.taskexecutor.api;

public interface IExecutable extends Comparable<IExecutable>{
	void preexecution();
	void execute();
	void postexecution();
	void setPriorityWeight(int priority);
	int getPriorityWeight();
}
