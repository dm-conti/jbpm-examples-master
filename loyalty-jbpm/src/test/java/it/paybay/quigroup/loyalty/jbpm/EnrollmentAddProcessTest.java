package it.paybay.quigroup.loyalty.jbpm;
import it.paybay.quigroup.loyalty.jbpm.handler.CustomerWorkerItemHandler;
import it.paybay.quigroup.loyalty.jbpm.handler.LoyWorkerItemHandler;
import it.paybay.quigroup.loyalty.jbpm.handler.VCWorkerItemHandler;
import it.paybay.titan.model.TitanMessage;
import it.paybay.titan.model.TitanMessageBuilder;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;


public class EnrollmentAddProcessTest extends JbpmJUnitBaseTestCase{
	private Map<String, Object> params;
	
	public EnrollmentAddProcessTest() {
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
        createRuntimeManager("bpmn/enrollment_add.bpmn");

        // take RuntimeManager to work with process engine
        RuntimeEngine runtimeEngine = getRuntimeEngine();

        // get access to KieSession instance
        KieSession ksession = runtimeEngine.getKieSession();
        
        LoyWorkerItemHandler loyWorkerItemHandler = new LoyWorkerItemHandler();
        loyWorkerItemHandler.setKSession(ksession);
        
        ksession.getWorkItemManager().registerWorkItemHandler("loySAInvoker", loyWorkerItemHandler);
        ksession.getWorkItemManager().registerWorkItemHandler("vCSAInvoker", new VCWorkerItemHandler());
        ksession.getWorkItemManager().registerWorkItemHandler("customerSAInvoker", new CustomerWorkerItemHandler());
        
        TitanMessage titanMessage = TitanMessageBuilder
        		.newCommandMessage("addAcc")
        		.withPayload(new HashMap<String, Object>())
        		.toTitanMessage();
        params.put("message", titanMessage);

        // start process
        ProcessInstance processInstance = ksession.startProcess("enrollment_add", params);

        // check whether the process instance has completed successfully
        assertProcessInstanceCompleted(processInstance.getId(), ksession);

        // check what nodes have been triggered
        assertNodeTriggered(processInstance.getId(), "StartEvent", "LoyInvoker", "VCSAInvoker", "CustomerSAInvoker", "StopEvent");
    }
}