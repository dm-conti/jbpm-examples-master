package it.paybay.quigroup.loyalty.jbpm;
import java.util.HashMap;
import java.util.Map;

import it.paybay.quigroup.loyalty.jbpm.wihandler.STDemoHandler;

import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;


public class ShitDemoTaskProcessTest extends JbpmJUnitBaseTestCase{
	private Map<String, Object> params;
	
	public ShitDemoTaskProcessTest() {
        // setup data source, enable persistence
        super(false, false);
    }
	
	@Before
	public void init(){
		
		params = new HashMap<String, Object>();
        params.put("param1", "Dia duit Domhanda");
        params.put("param2", "Hallo Welt");
        params.put("param3", "Bonjour tout le monde");	
	}


    @Test
    public void testProcessWhenTypical() {
        // create runtime manager with single process - hello.bpmn
        createRuntimeManager("bpmn/stdemoprocess.bpmn");

        // take RuntimeManager to work with process engine
        RuntimeEngine runtimeEngine = getRuntimeEngine();

        // get access to KieSession instance
        KieSession ksession = runtimeEngine.getKieSession();
        ksession.getWorkItemManager().registerWorkItemHandler("shitService", new STDemoHandler());

        // start process
        ProcessInstance processInstance = ksession.startProcess("stdemoprocess", params);

        // check whether the process instance has completed successfully
        assertProcessInstanceCompleted(processInstance.getId(), ksession);

        // check what nodes have been triggered
        assertNodeTriggered(processInstance.getId(), "StartEvent", "ShitService", "StopEvent");
    }
}