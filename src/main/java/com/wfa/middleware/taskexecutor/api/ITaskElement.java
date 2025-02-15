package com.wfa.middleware.taskexecutor.api;

/**
 * This is the singular most atomic executable task in a possibly
 * vast structure of tasks
 */
public interface ITaskElement extends IExecutable{
	ITaskElement next(); // task to be executed in sequence to this task
	void setNext(ITaskElement childTask); // set next task to be executed
}
