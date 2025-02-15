package com.wfa.middleware.taskexecutor.api;

public interface IPrioritizedRunnable extends Runnable, Comparable<IPrioritizedRunnable> {
	void setPriorityWeight(int weight);
	int getPriorityWeight();
}
