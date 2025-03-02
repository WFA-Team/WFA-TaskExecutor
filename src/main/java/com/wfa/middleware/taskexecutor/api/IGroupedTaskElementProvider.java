package com.wfa.middleware.taskexecutor.api;

import java.util.List;

import com.wfa.middleware.utils.api.IJoinable;

@TaskProvider
public interface IGroupedTaskElementProvider {
	<R extends IJoinable<R>> IGroupedTaskElement<R> getGroupedTaskElement();
	<R extends IJoinable<R>> IGroupedTaskElement<R> getGroupedTaskElement(List<ITaskElement<R>> parallelTasks);
	<R extends IJoinable<R>> IGroupedTaskElement<R> getGroupedTaskElement(List<ITaskElement<R>> parallelTasks, ITaskElement<R> nextTask);
}
