package com.sample;

import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;

@Ignore
public class SampleIntegrationProcessTest extends JbpmJUnitBaseTestCase{
	
	@Test
	public void testSampleIntegrationProcessWhenTypical() {

	    // create runtime manager with single process - sample-integration-process.bpmn
	    createRuntimeManager("com/sample/sample-integration-process.bpmn");

	    // take RuntimeManager to work with process engine
	    RuntimeEngine runtimeEngine = getRuntimeEngine();

	    // get access to KieSession instance
	    KieSession ksession = runtimeEngine.getKieSession();

	    // register a test handler for "Email"
	    TestWorkItemHandler testHandler = getTestWorkItemHandler();
	    ksession.getWorkItemManager().registerWorkItemHandler("Email", testHandler);

	    // start the process
	    ProcessInstance processInstance = ksession.startProcess("com.sample.bpmn.integration.process");
	    assertProcessInstanceActive(processInstance.getId(), ksession);
	    assertNodeTriggered(processInstance.getId(), "StartProcess", "Email");

	    // check whether the email has been requested
	    WorkItem workItem = testHandler.getWorkItem();
	    assertNotNull(workItem);
	    assertEquals("Email", workItem.getName());
	    assertEquals("me@mail.com", workItem.getParameter("From"));
	    assertEquals("you@mail.com", workItem.getParameter("To"));

	    // notify the engine the email has been sent
	    ksession.getWorkItemManager().abortWorkItem(workItem.getId());
	    assertProcessInstanceAborted(processInstance.getId(), ksession);
	    assertNodeTriggered(processInstance.getId(), "Gateway", "Failed", "Error");
	}
}