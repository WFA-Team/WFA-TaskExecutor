package com.wfa.middleware.taskexecutor.api;

import com.wfa.middleware.utils.AsyncPromise;

public abstract class ATaskElement<R> implements ITaskElement<R> {
	private ITaskElement<R> childTask = null;
	private final ITaskExecutorEngine taskEngine;
	protected boolean succeed = false;
	private int priority = 0;
	private R result = null;
	
	public ATaskElement(ITaskExecutorEngine taskEngine) {
		this.taskEngine = taskEngine;
	}
	
	@Override
	public void postexecute(AsyncPromise<R> promise) {		
		if (!succeed) {
			promise.fail(result);
		} else if (next() != null) {
			this.taskEngine.schedule(next(), promise);
		} else {
			promise.succeed(result);
		}
	}

	@Override
	public void setPriorityWeight(int priority) {
		this.priority = priority;	
	}

	@Override
	public int getPriorityWeight() {
		return this.priority;
	}

	@Override
	public int compareTo(IExecutable<?> o) {
		return this.getPriorityWeight() - o.getPriorityWeight();
	}

	@Override
	public ITaskElement<R> next() {
		return childTask;
	}

	@Override
	public void setNext(ITaskElement<R> childTask) {
		this.childTask = childTask;	
	}
	
	protected R getResult() {
		return result;
	}
	
	protected void setResult(R result) {
		this.result = result;
	}
}
