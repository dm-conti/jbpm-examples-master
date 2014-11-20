package it.paybay.quigroup.loyalty.jbpm.wihandler;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;

public class STDemoHandler implements WorkItemHandler{

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		
		System.out.println(workItem.getParameter("param1") + "\n"
				+ workItem.getParameter("param2") + "\n"
				+ workItem.getParameter("param3"));
		manager.completeWorkItem(workItem.getId(), null);
	}
	
	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		manager.abortWorkItem(workItem.getId());
	}
}