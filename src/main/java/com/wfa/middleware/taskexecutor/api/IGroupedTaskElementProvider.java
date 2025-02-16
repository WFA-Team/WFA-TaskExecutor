package com.wfa.middleware.taskexecutor.api;

import java.util.List;

public interface IGroupedTaskElementProvider {
	IGroupedTaskElement getGroupedTaskElement();
	IGroupedTaskElement getGroupedTaskElement(List<ITaskElement> parallelTasks);
	IGroupedTaskElement getGroupedTaskElement(List<ITaskElement> parallelTasks, ITaskElement nextTask);
}
