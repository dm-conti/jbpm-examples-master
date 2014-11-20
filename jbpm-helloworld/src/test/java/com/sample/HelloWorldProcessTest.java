package com.sample;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;


public class HelloWorldProcessTest extends JbpmJUnitBaseTestCase{
	
	public HelloWorldProcessTest() {
        // setup data source, enable persistence
        super(true, true);
    }


    @Test
    public void testProcessWhenTypical() {
        // create runtime manager with single process - hello.bpmn
        createRuntimeManager("com/sample/hello.bpmn");

        // take RuntimeManager to work with process engine
        RuntimeEngine runtimeEngine = getRuntimeEngine();

        // get access to KieSession instance
        KieSession ksession = runtimeEngine.getKieSession();

        // start process
        ProcessInstance processInstance = ksession.startProcess("com.sample.bpmn.hello");

        // check whether the process instance has completed successfully
        assertProcessInstanceCompleted(processInstance.getId(), ksession);

        // check what nodes have been triggered
        assertNodeTriggered(processInstance.getId(), "StartEvent", "Hello", "EndEvent");
    }
}