package it.paybay.quigroup.loyalty.jbpm;
import it.paybay.quigroup.loyalty.jbpm.wihandler.STDemoHandler;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;


public class STDemoProcessTest {
	private KieSession kSession;
	private Map<String, Object> params;
	
	@Before
	public void init(){
		//Get a Kie Session with the kwnoledge in the default classpath
		kSession = KieServices.Factory.get().getKieClasspathContainer().newKieSession();
        
        params = new HashMap<String, Object>();
        params.put("variable1", "Dia duit Domhanda");
        params.put("variable2", "Hallo Welt");
        params.put("variable3", "Bonjour tout le monde");
	}
	
    @Test
    public void testProcessWhenTypical() {
        // start a new process instance
    	STDemoHandler stdHandler = new STDemoHandler();
        kSession.getWorkItemManager().registerWorkItemHandler("shitService", stdHandler);
        
        // start process
        ProcessInstance processInstance = kSession.startProcess("stdemoprocess", params);

        // check whether the process instance has completed successfully
        Assert.assertEquals(ProcessInstance.STATE_COMPLETED, processInstance.getState());
    }
}