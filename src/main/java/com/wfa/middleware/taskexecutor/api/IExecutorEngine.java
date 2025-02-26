package com.wfa.middleware.taskexecutor.api;

import com.wfa.middleware.utils.AsyncPromise;
import com.wfa.middleware.utils.JoinVoid;
import com.wfa.middleware.utils.api.IJoinable;

/**
 * Executor engine, will handle scheduling of independent executions, 
 * avoiding deadlocks.
 * 
 * author -> tortoiseDev
 */
public interface IExecutorEngine <T extends IExecutable> {
	void setMaxParallelism(int parallelism) throws IllegalStateException; // call only when engine is stopped	
	IJoinable<AsyncPromise<JoinVoid>> schedule(T executable);
	void schedule(T executable, AsyncPromise<JoinVoid> promise);
	void startEngine() throws IllegalStateException;
	void stopEngine() throws IllegalStateException;
	void pauseEngine() throws IllegalStateException;
}
