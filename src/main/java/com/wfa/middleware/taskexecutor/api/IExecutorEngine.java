package com.wfa.middleware.taskexecutor.api;

/**
 * Executor engine, will handle scheduling of independent executions, 
 * avoiding deadlocks.
 * 
 * author -> tortoiseDev
 */
public interface IExecutorEngine <T extends IExecutable> {
	void setMaxParallelism(int parallelism) throws IllegalStateException; // call only when engine is stopped	
	void schedule(T executable);
	void startEngine() throws IllegalStateException;
	void stopEngine() throws IllegalStateException;
	void pauseEngine() throws IllegalStateException;
}
