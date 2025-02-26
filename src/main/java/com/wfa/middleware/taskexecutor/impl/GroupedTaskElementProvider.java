package com.wfa.middleware.taskexecutor.impl;

import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wfa.middleware.taskexecutor.api.IExecutable;
import com.wfa.middleware.taskexecutor.api.IGroupedTaskElement;
import com.wfa.middleware.taskexecutor.api.IGroupedTaskElementProvider;
import com.wfa.middleware.taskexecutor.api.ITaskElement;
import com.wfa.middleware.taskexecutor.api.ITaskExecutorEngine;
import com.wfa.middleware.utils.AsyncJoinedJoinablePromise;
import com.wfa.middleware.utils.AsyncPromise;
import com.wfa.middleware.utils.JoinVoid;
import com.wfa.middleware.utils.api.IAsyncCallback;
import com.wfa.middleware.utils.api.IJoinable;
import com.wfa.middleware.utils.api.IJoinedJoinable;
import java.util.Comparator;

@Component
public class GroupedTaskElementProvider implements IGroupedTaskElementProvider{
	private ITaskExecutorEngine engine;
	
	@Autowired
	public GroupedTaskElementProvider(ITaskExecutorEngine engine) {
		this.engine = engine;
	}
	
	@Override
	public IGroupedTaskElement getGroupedTaskElement() {
		return this.getGroupedTaskElement(null);
	}

	@Override
	public IGroupedTaskElement getGroupedTaskElement(List<ITaskElement> parallelTasks) {
		return this.getGroupedTaskElement(parallelTasks, null);
	}

	@Override
	public IGroupedTaskElement getGroupedTaskElement(List<ITaskElement> parallelTasks, ITaskElement nextTask) {
		IGroupedTaskElement groupedTask =  new IGroupedTaskElement() {
			private ITaskElement childTask = nextTask;
			private PriorityBlockingQueue<ITaskElement> parallelyExecutableTasks = new PriorityBlockingQueue<ITaskElement>(
					parallelTasks != null ? parallelTasks.size() : 0,
					new Comparator<ITaskElement>() {

						@Override
						public int compare(ITaskElement e1, ITaskElement e2) {
							return e1.getPriorityWeight() - e2.getPriorityWeight();
						}
				
			});
			private int priorityWeight = 0;
			private AsyncPromise<JoinVoid> replyPromise;
			private IJoinedJoinable<AsyncPromise<JoinVoid>> combinedSubTaskPromise;
			
			// Multi-threading guard
			private AtomicBoolean concluded = new AtomicBoolean(false);
			
			@Override
			public ITaskElement next() {
				return this.childTask;
			}

			@Override
			public void setNext(ITaskElement childTask) {
				this.childTask = childTask;
			}

			@Override
			public void preexecute() {
				// do nothing
			}

			@Override
			public void execute() {
				IJoinable<AsyncPromise<JoinVoid>> firstSubTaskPromise = null;
				
				while(parallelyExecutableTasks.peek() != null) {
					ITaskElement subTask = parallelyExecutableTasks.poll();
					if (firstSubTaskPromise == null) {
						firstSubTaskPromise = engine.schedule(subTask);
					} else if (combinedSubTaskPromise == null){
						combinedSubTaskPromise = AsyncJoinedJoinablePromise.getNewJoinedJoinablePromise(firstSubTaskPromise, 
								engine.schedule(subTask));
					} else {
						combinedSubTaskPromise = combinedSubTaskPromise.joinableJoinTo(engine.schedule(subTask));
					}
				}
				
				combinedSubTaskPromise.get().appendCallback(getCallbackForTaskCompletion());
			}
			
			private synchronized IAsyncCallback<JoinVoid> getCallbackForTaskCompletion() {
				return new IAsyncCallback<JoinVoid>() {
					@Override
					public void onSuccess(JoinVoid result) {
						if (replyPromise != null) {
							performPostExecution();
						}
					}
					
					@Override
					public void onFailure(JoinVoid result) {
						if (replyPromise != null) {
							performPostExecution();
						}
					}					
				};
			}

			@Override
			public synchronized void postexecute(AsyncPromise<JoinVoid> promise) {
				replyPromise = promise;
				performPostExecution();
			}
			
			private void performPostExecution() {
				if (combinedSubTaskPromise.get().isConcluded() 
						&& concluded.compareAndSet(false, true)) {
					if (combinedSubTaskPromise.get().hasSucceeded()) {
						if (nextTask != null)
							engine.schedule(nextTask, replyPromise);
						else {
							replyPromise.succeed(combinedSubTaskPromise.get().getResult());
						}
					} else {
						replyPromise.fail(combinedSubTaskPromise.get().getResult());
					}
				}				
			}

			@Override
			public void setPriorityWeight(int priority) {
				this.priorityWeight = priority;		
			}

			@Override
			public int getPriorityWeight() {
				return this.priorityWeight;
			}

			@Override
			public int compareTo(IExecutable other) {
				return this.getPriorityWeight() - other.getPriorityWeight();
			}

			@Override
			public void addParallelTask(ITaskElement subTask) {
				// Make weight relative to the Grouped Task
				subTask.setPriorityWeight(this.getPriorityWeight() + subTask.getPriorityWeight());
				this.parallelyExecutableTasks.add(subTask);
			}
		};
		
		if (parallelTasks != null) {
			for (ITaskElement subTask : parallelTasks) {
				groupedTask.addParallelTask(subTask);
			}
		}
		
		return groupedTask;
	}

}
