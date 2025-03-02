package com.wfa.middleware.taskexecutor.api;

/**
 * This is the singular most atomic executable task in a possibly
 * vast structure of tasks
 */
public interface ITaskElement<R> extends IExecutable<R> {
	ITaskElement<R> next(); // task to be executed in sequence to this task
	void setNext(ITaskElement<R> childTask); // set next task to be executed
}
