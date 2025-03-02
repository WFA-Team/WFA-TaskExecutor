package com.wfa.middleware.taskexecutor.api;

import com.wfa.middleware.utils.AsyncPromise;
import com.wfa.middleware.utils.api.IJoinable;

/**
 * Executor engine, will handle scheduling of independent executions, 
 * avoiding deadlocks.
 * 
 * author -> tortoiseDev
 */
public interface IExecutorEngine <T extends IExecutable<?>> {
	void setMaxParallelism(int parallelism) throws IllegalStateException; // call only when engine is stopped
	<R extends IJoinable<R>> IJoinable<AsyncPromise<R>> scheduleJoinable(T executable);
	<R> AsyncPromise<R> schedule(T executable);
	<R> void schedule(T executable, AsyncPromise<R> promise);
	void startEngine() throws IllegalStateException;
	void stopEngine() throws IllegalStateException;
	void pauseEngine() throws IllegalStateException;
}
