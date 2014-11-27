package it.paybay.quigroup.loyalty.jbpm.handler;

import it.paybay.titan.model.TitanMessage;
import it.paybay.titan.model.TitanMessageBuilder;

import java.util.Map;
import java.util.UUID;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisWorkerItemHandler implements WorkItemHandler{
	private Logger LOG = LoggerFactory.getLogger(this.getClass());
	public static final String HANDLER_ID = "RedisInvoker";
	
	private KieSession ksession;
	
	@Autowired private RedisTemplate<String, String> redisTemplate;

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		WorkflowProcessInstance process = (WorkflowProcessInstance) ksession.getProcessInstance(workItem.getProcessInstanceId());
		
		TitanMessage message = (TitanMessage) process.getVariable("message"); //estrarre i dati da elaborare
		
		LOG.info("waiting for response ");
		
		UUID key = UUID.randomUUID();
		Long index = redisTemplate.boundListOps(key.toString()).leftPush("dummy_cdm_object");
		
		Map<String, Object> results = workItem.getResults();
		results.put(key.toString(), index);
		
		ksession.setGlobal( "titanResponse", TitanMessageBuilder.newDocumentMessage().withPayload(results).toTitanMessage() );
		manager.completeWorkItem(workItem.getId(), results);
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		LOG.info(":: call ABORT ::");
	}
	
	public void setKSession(KieSession ksession){
		this.ksession = ksession;
	}
}