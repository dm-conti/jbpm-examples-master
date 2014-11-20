package it.paybay.quigroup.loyalty.jbpm.handler;

import java.util.HashMap;
import java.util.Map;

import it.paybay.titan.model.TitanMessage;
import it.paybay.titan.model.TitanMessageBuilder;

import org.drools.core.spi.ProcessContext;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoyWorkerItemHandler implements WorkItemHandler{
	private Logger LOG = LoggerFactory.getLogger(this.getClass());
	private KieSession ksession;

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		LOG.info(":: call EXECUTE ::");
		ProcessContext kcontext = new ProcessContext(this.ksession);
		
		Map<String, Object> payload = new HashMap<String, Object>();
		payload.put("NextStep", "VCSAInvoker");
		
		TitanMessage message = TitanMessageBuilder
				.newDocumentMessage()
				.withPayload(payload)
				.toTitanMessage();
		
		kcontext.getKnowledgeRuntime().insert(message);
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		LOG.info(":: call ABORT ::");
	}
	
	public void setKSession(KieSession ksession){
		this.ksession = ksession;
	}
}