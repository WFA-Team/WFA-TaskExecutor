package com.wfa.middleware.taskexecutor.impl;

import com.wfa.middleware.taskexecutor.api.IExecutable;
import com.wfa.middleware.taskexecutor.api.IExecutorEngine;
import com.wfa.middleware.taskexecutor.api.IPrioritizedRunnable;
import com.wfa.middleware.utils.AsyncJoinablePromise;
import com.wfa.middleware.utils.AsyncPromise;
import com.wfa.middleware.utils.JoinVoid;
import com.wfa.middleware.utils.PlayType;
import com.wfa.middleware.utils.api.IJoinable;
import com.wfa.middleware.utils.beans.api.IThreadPool;
import com.wfa.middleware.utils.beans.api.IThreadPoolFactory;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.Comparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/* Bridge between Executable concept and Java Runnable
 * author -> tortoiseDev
 */
@Component
public class ExecutorEngine <T extends IExecutable>implements IExecutorEngine<T> {
	private static final int DEFAULT_PARALLELISM = 32;
	private static final int DEFAULT_EXECUTABLE_CAPACITY = 17;
	private int allowedTotalParallelism;
	private volatile PlayType engineState;
	private IThreadPool<IPrioritizedRunnable> threadPool;
		
	@Autowired
	public ExecutorEngine(IThreadPoolFactory<IPrioritizedRunnable> threadPoolFactory) {
		this.allowedTotalParallelism = DEFAULT_PARALLELISM;
		this.engineState = PlayType.NOT_STARTED;
		configureThreadPool(threadPoolFactory);
	}
	
	private void configureThreadPool(IThreadPoolFactory<IPrioritizedRunnable> threadPoolFactory) {
		this.threadPool = threadPoolFactory.getNewThreadPool();
		threadPool.setMaxParallelism(allowedTotalParallelism);
		threadPool.submitRunnableQueue(new PriorityBlockingQueue<IPrioritizedRunnable>(DEFAULT_EXECUTABLE_CAPACITY,
				new Comparator<IPrioritizedRunnable>() {

					@Override
					public int compare(IPrioritizedRunnable e1, IPrioritizedRunnable e2) {
						return e1.getPriorityWeight() - e2.getPriorityWeight();
					}
			
		}));
	}
	
	@Override
	public void setMaxParallelism(int parallelism) throws IllegalStateException {
		if (engineState.equals(PlayType.STARTED) || engineState.equals(PlayType.PAUSED)) {
			throw new IllegalStateException("Cannot set parallelism while engine is running");
		}
		
		allowedTotalParallelism = parallelism;
		threadPool.setMaxParallelism(allowedTotalParallelism);
	}

	@Override
	public IJoinable<AsyncPromise<JoinVoid>> schedule(T executable) {
		IJoinable<AsyncPromise<JoinVoid>> promise = AsyncJoinablePromise.getNewJoinablePromise();
		IPrioritizedRunnable runnable = new IPrioritizedRunnable() {
			private int priorityWeight = 0;
			
			@Override
			public void run() {
				executable.preexecution();
				executable.execute();
				executable.postexecution(promise.get());
			}

			@Override
			public int compareTo(IPrioritizedRunnable other) {
				return this.getPriorityWeight() - other.getPriorityWeight();
			}

			@Override
			public void setPriorityWeight(int weight) {
				priorityWeight = weight;
			}

			@Override
			public int getPriorityWeight() {
				return priorityWeight;
			}
			
		};
		
		runnable.setPriorityWeight(executable.getPriorityWeight());
		// Configured this runnable translation of the executable
		// in PriorityQueue of ThreadPool
		this.threadPool.getRunnableQueue().add(runnable);
		
		return promise;
	}

	@Override
	public void startEngine() throws IllegalStateException{
		if (engineState.equals(PlayType.STARTED)) {
			throw new IllegalStateException("Engine has already been started");
		}
		
		this.threadPool.start();
		engineState = PlayType.STARTED;
	}

	@Override
	public void stopEngine() throws IllegalStateException{
		if (engineState.equals(PlayType.STOPPED) || engineState.equals(PlayType.NOT_STARTED)) {
			throw new IllegalStateException("Engine is already stopped");
		}
		
		this.threadPool.stop();
		engineState = PlayType.STOPPED;
	}

	@Override
	public void pauseEngine() throws IllegalStateException {
		if (!engineState.equals(PlayType.STARTED)) {
			throw new IllegalStateException("Engine is not started. Cannot Pause");			
		}
		
		this.threadPool.pause();
		engineState = PlayType.PAUSED;
	}
}
