package it.paybay.quigroup.loyalty.jbpm.handler;

import it.paybay.titan.model.TitanMessage;

import java.util.Map;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

import org.apache.activemq.command.ActiveMQObjectMessage;
import org.apache.activemq.command.ActiveMQQueue;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;

public class CustomerWorkerItemHandler implements WorkItemHandler {
	private Logger LOG = LoggerFactory.getLogger(this.getClass());
	public static final String HANDLER_ID = "CustomerSAInvoker";

	private KieSession ksession;
	private final Destination destination = new ActiveMQQueue("queue.customer.in");
	private final Destination reply = new ActiveMQQueue("queue.customer.out");

	@Autowired
	private JmsTemplate jmsTemplate;

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		WorkflowProcessInstance process = (WorkflowProcessInstance) ksession.getProcessInstance(workItem.getProcessInstanceId());
		TitanMessage message = (TitanMessage) process.getVariable("message");

		prepareMessageAndSend(message);

		LOG.info("waiting for response ");
		ObjectMessage responseOMessage = (ObjectMessage) jmsTemplate.receive(reply); // waiting response

		TitanMessage responseMessage = null;
		try {
			responseMessage = (TitanMessage) responseOMessage.getObject();

			Map<String, Object> results = workItem.getResults();
			results.put("titanMessage", responseMessage);

			if (TitanMessage.MessageType.ERROR.name().equals(responseMessage.getMessageType())) {
				ksession.setGlobal("nextStep", "KO");
				LOG.info("Response is of type = {}; nextStep = KO ", responseMessage.getMessageType());
				manager.completeWorkItem(workItem.getId(), results);
			}

			LOG.info("Response is of type = {}; nextStep = {} ", responseMessage.getMessageType(), RedisWorkerItemHandler.HANDLER_ID);

			ksession.setGlobal("titanResponse", responseMessage);
			manager.completeWorkItem(workItem.getId(), results);

		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		LOG.info(":: call ABORT ::");
	}
	
	public void setKSession(KieSession ksession) {
		this.ksession = ksession;
	}

	// PRIVATE
	private void prepareMessageAndSend(TitanMessage message) {
		ObjectMessage omessage = new ActiveMQObjectMessage();
		try {
			omessage.setJMSReplyTo(reply);
			omessage.setObject(message);
		} catch (JMSException e) {
			e.printStackTrace();
		}

		LOG.info("send JMS Message ");
		jmsTemplate.convertAndSend(destination, omessage); // Send
	}

	private void printInputParameter(WorkItem workItem) {
		for (String currentKey : workItem.getParameters().keySet()) {
			System.out.println("Param :: key " + currentKey + " value " + workItem.getParameter(currentKey));
		}
	}
}