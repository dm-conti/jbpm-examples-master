package it.paybay.quigroup.loyalty.jbpm.handler;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VCWorkerItemHandler implements WorkItemHandler{
	private Logger LOG = LoggerFactory.getLogger(this.getClass());
	public static final String HANDLER_ID = "VCSAInvoker";

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		
		manager.completeWorkItem(workItem.getId(), null);
	
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		LOG.info(":: call ABORT ::");
	}
}