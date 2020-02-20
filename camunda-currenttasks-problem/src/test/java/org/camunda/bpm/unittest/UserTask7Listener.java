/*
 * Copyright 2013 Grupo Castilla Software. All rights reserved.
 */

package org.camunda.bpm.unittest;

import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.task.Task;

public class UserTask7Listener implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        List<String> activityIds = delegateTask.getProcessEngineServices().getRuntimeService().getActiveActivityIds(delegateTask.getExecutionId());
        System.out.print("from User7TaskDelegate: activities from runtimeService.getActivityIds: ");
        for (String activyId : activityIds) {
            System.out.print(activyId);
            System.out.print(" / ");
        }
        System.out.println();

        List<Task> tasks = delegateTask.getProcessEngineServices().getTaskService().createTaskQuery().processInstanceId(delegateTask.getProcessInstanceId()).list();
        System.out.print("from User7TaskDelegate: tasks from TaskService.createTaskQuery by ProcessInstance: ");
        for (Task task : tasks) {
            System.out.print(task.getTaskDefinitionKey());
            System.out.print(" / ");
        }
        System.out.println();

        List<HistoricActivityInstance> currentActivities = ProcessUtils.getProcessCurrentActivities(delegateTask.getProcessEngineServices().getHistoryService(), delegateTask.getProcessInstanceId());
        System.out.print("from User7TaskDelegate: Activities from HistoryService: ");
        for (HistoricActivityInstance activityInstance : currentActivities) {
            System.out.print(activityInstance.getActivityId());
            System.out.print(" / ");
        }
        System.out.println();

    }

}