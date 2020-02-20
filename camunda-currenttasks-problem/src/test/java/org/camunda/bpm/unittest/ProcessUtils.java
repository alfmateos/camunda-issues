/*
 * Copyright 2013 Grupo Castilla Software. All rights reserved.
 */

package org.camunda.bpm.unittest;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.runtimeService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;

public class ProcessUtils {

    public static void sendMessage(String messageName, String processInstanceId) {

        if (processInstanceId != null) {
            String foundExecutionId = null;
            ExecutionQuery query = runtimeService().createExecutionQuery().processInstanceId(processInstanceId).messageEventSubscriptionName(messageName);

            List<Execution> executions = query.list();
            if (executions != null && executions.size() > 0) {
                foundExecutionId = executions.get(0).getId();
            }

            if (foundExecutionId != null) {
                runtimeService().messageEventReceived("MSG_BOUNDARY_USERTASK3", foundExecutionId);
            }
        }

    }


    public static List<HistoricActivityInstance> getProcessCurrentActivities(HistoryService historyService, String processInstanceId) {
        List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).unfinished().list();
        if (activities == null) {
            return new ArrayList<HistoricActivityInstance>();
        } else {
            Iterator<HistoricActivityInstance> activitiesIt = activities.iterator();
            while (activitiesIt.hasNext()) {
                HistoricActivityInstance activity = activitiesIt.next();
                if (activity.getActivityType() == null) {
                    activitiesIt.remove();
                } else {
                    switch (activity.getActivityType().toLowerCase()) {
                        case "subprocess":
                        case "parallelgateway":
                        case "exclusivegateway":
                        case "inclusivegateway":
                        case "eventbasedgateway":
                        case "complexgateway":
                            activitiesIt.remove();
                    }
                }
            }
            return activities;
        }
    }
}
