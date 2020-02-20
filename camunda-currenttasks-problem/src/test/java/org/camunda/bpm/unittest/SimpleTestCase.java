package org.camunda.bpm.unittest;

import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.init;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineAssertions.processEngine;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.complete;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.processInstanceQuery;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.runtimeService;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.task;

import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author Daniel Meyer
 * @author Martin Schimak
 */
@RunWith(JUnit4.class)
public class SimpleTestCase {

    public static final String MSG_BOUNDARY_USERTASK_3 = "MSG_BOUNDARY_USERTASK3";

    protected RuntimeService runtimeService;
    protected TaskService taskService;
    protected HistoryService historyService;
    protected ProcessEngineConfigurationImpl processEngineConfiguration;

    private final UserTask7Listener userTask7Listener = new UserTask7Listener();

    @Rule
    public ProcessEngineRule rule = new ProcessEngineRule();

    @Before
    public void setup() {
        init(rule.getProcessEngine());
        Mocks.reset();
    }

    @Test
    @Deployment(resources = {"testProcess.bpmn"})
    public void shouldExecuteProcess() {

        Mocks.register("userTask7Listener", userTask7Listener);

        // Given we create a new process instance
        ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("testProcess");
        // Then it should be active
        assertThat(processInstance).isActive();

        // And it should be the only instance
        assertThat(processInstanceQuery().count()).isEqualTo(1);
        // And there should exist just a single task within that process instance
        assertThat(task(processInstance)).isNotNull();

        // Complete userTask1
        runtimeService().setVariable(processInstance.getProcessInstanceId(), "condition1", true);
        complete(task("userTask1"));

        // Complete userTask5 and userTask6
        complete(task("userTask5"));
        complete(task("userTask6"));

        assertThat(task("userTask3")).isNotNull();

        runtimeService().correlateMessage("MSG_BOUNDARY_USERTASK3");

        List<HistoricActivityInstance> currentActivities = ProcessUtils.getProcessCurrentActivities(processEngine().getHistoryService(), processInstance.getProcessInstanceId());
        System.out.print("from TestCase: Activities from HistoryService: ");
        for (HistoricActivityInstance activityInstance : currentActivities) {
            System.out.print(activityInstance.getActivityId());
            System.out.print(" / ");
        }
        System.out.println();

        runtimeService().setVariable(processInstance.getProcessInstanceId(), "condition2", true);
        runtimeService().correlateMessage(("MSG_TASK4"));

        assertThat(task("userTask7")).isNotNull();

        String executionId = runtimeService().createExecutionQuery().processInstanceId(processInstance.getProcessInstanceId()).singleResult().getId();
        List<String> ids = runtimeService().getActiveActivityIds(executionId);
        assertThat(ids.size() == 1);
    }


}
