package com.wfa.middleware.taskexecutor.impl;

import java.util.List;

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
		return this.getGroupedTaskElement(null, null);
	}

	@Override
	public IGroupedTaskElement getGroupedTaskElement(List<ITaskElement> parallelTasks, ITaskElement nextTask) {
		return new IGroupedTaskElement() {
			private ITaskElement childTask = nextTask;
			private List<ITaskElement> parallelyExecutableTasks = parallelTasks;
			private int priorityWeight = 0;
			private AsyncPromise<JoinVoid> replyPromise;
			private IJoinedJoinable<AsyncPromise<JoinVoid>> combinedSubTaskPromise;
			
			@Override
			public ITaskElement next() {
				return this.childTask;
			}

			@Override
			public void setNext(ITaskElement childTask) {
				this.childTask = childTask;
			}

			@Override
			public void preexecution() {
				// do nothing
			}

			@Override
			public void execute() {
				IJoinable<AsyncPromise<JoinVoid>> firstSubTaskPromise = null;
				for (ITaskElement subTask : parallelyExecutableTasks) {
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
			
			private IAsyncCallback<JoinVoid> getCallbackForTaskCompletion() {
				return new IAsyncCallback<JoinVoid>() {
					@Override
					public void onSuccess(JoinVoid result) {
						if (replyPromise != null) {
							replyPromise.succeed(JoinVoid.JoinVoidInstance);
							engine.schedule(nextTask);
						}
					}
					
					@Override
					public void onFailure(JoinVoid result) {
						if (replyPromise != null) {
							replyPromise.fail(JoinVoid.JoinVoidInstance);
						}
						// TODO-> Does it make sense here to schedule next task?
					}					
				};
			}

			@Override
			public void postexecution(AsyncPromise<JoinVoid> promise) {
				replyPromise = promise;
				
				if (combinedSubTaskPromise.get().isDone()) {
					if (combinedSubTaskPromise.get().hasSucceeded()) {
						replyPromise.succeed(JoinVoid.JoinVoidInstance);
						engine.schedule(nextTask);
					} else {
						replyPromise.fail(JoinVoid.JoinVoidInstance);
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
	}

}
