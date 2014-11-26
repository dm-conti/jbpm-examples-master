package it.paybay.quigroup.loyalty.jbpm;

import it.paybay.quigroup.loyalty.jbpm.handler.CustomerWorkerItemHandler;
import it.paybay.quigroup.loyalty.jbpm.handler.LoyWorkerItemHandler;
import it.paybay.quigroup.loyalty.jbpm.handler.VCWorkerItemHandler;
import it.paybay.titan.model.TitanMessage;
import it.paybay.titan.model.TitanMessageBuilder;
import it.paybay.titan.model.headerkeys.ErrorHeaderKeys.ErrorType;

import java.util.HashMap;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import mockit.Mocked;
import mockit.NonStrictExpectations;

import org.apache.activemq.command.ActiveMQObjectMessage;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:enrollment-it-context.xml" })
public class EnrollmentAddProcessTest extends JbpmJUnitBaseTestCase {
	private Map<String, Object> params;

	private KieSession ksession;

	@Autowired
	private LoyWorkerItemHandler loyWorkerItemHandler;
	@Autowired
	private VCWorkerItemHandler vCWorkerItemHandler;
	@Autowired
	private CustomerWorkerItemHandler customerWorkerItemHandler;

	@Mocked
	@Autowired
	private JmsTemplate jmsTemplate;

	public EnrollmentAddProcessTest() {
		// setup data source, enable persistence
		super(false, false);
	}

	@Before
	public void init() {

		params = new HashMap<String, Object>();
		params.put("param1", "Dia duit Domhanda");
		params.put("param2", "Hallo Welt");
		params.put("param3", "Bonjour tout le monde");

		// create runtime manager with single process - hello.bpmn
		createRuntimeManager("bpmn/enrollment_add_cut.bpmn");

		// take RuntimeManager to work with process engine
		RuntimeEngine runtimeEngine = getRuntimeEngine();

		// get access to KieSession instance
		ksession = runtimeEngine.getKieSession();

		loyWorkerItemHandler.setKSession(ksession);

		ksession.getWorkItemManager().registerWorkItemHandler("loySAInvoker", loyWorkerItemHandler);
		ksession.getWorkItemManager().registerWorkItemHandler("vCSAInvoker", vCWorkerItemHandler);
		ksession.getWorkItemManager().registerWorkItemHandler("customerSAInvoker", customerWorkerItemHandler);
	}

	@Test
	public void startEnrollmentWhenTypical() throws JMSException {
		final TitanMessage titanMessage = TitanMessageBuilder.newCommandMessage("addAcc") // commandName
			.withPayload(new HashMap<String, Object>()).toTitanMessage();

		params.put("message", titanMessage);

		new NonStrictExpectations() { {
			jmsTemplate.convertAndSend((Destination) any, any);

			TitanMessage titanMessage = TitanMessageBuilder.newDocumentMessage()
					.withPayload(new HashMap<String, Object>() { { put("response", "OK"); } }).toTitanMessage();

			ActiveMQObjectMessage response = new ActiveMQObjectMessage();
			response.setObject(titanMessage);

			jmsTemplate.receive((Destination) any);
			returns((Message) response);

			
		} };

		// start process
		ProcessInstance processInstance = ksession.startProcess("enrollment_add", params);

		// check whether the process instance has completed successfully
		// assertProcessInstanceCompleted(processInstance.getId(), ksession);

		// check what nodes have been triggered
		assertNodeTriggered(processInstance.getId(), "StartEvent", "LoySAInvoker", "VCSAInvoker", "StopEvent");
		// assertNodeTriggered(processInstance.getId(), "CustomerSAInvoker");
		// assertNodeTriggered(processInstance.getId(), "StopEvent");
	}
	
	@Test
	public void startEnrollmentNOKResponse() throws JMSException {
		final TitanMessage titanMessage = TitanMessageBuilder.newCommandMessage("addAcc") // commandName
			.withPayload(new HashMap<String, Object>()).toTitanMessage();

		params.put("message", titanMessage);

		new NonStrictExpectations() { {
			jmsTemplate.convertAndSend((Destination) any, any);

			TitanMessage titanMessage = TitanMessageBuilder.newErrorMessage(ErrorType.ERROR)
					.withErrorMessage("NOK").toTitanMessage();

			ActiveMQObjectMessage response = new ActiveMQObjectMessage();
			response.setObject(titanMessage);

			jmsTemplate.receive((Destination) any);
			returns((Message) response);

			
		} };

		// start process
		ProcessInstance processInstance = ksession.startProcess("enrollment_add", params);

		// check what nodes have been triggered
		assertNodeTriggered(processInstance.getId(), "StartEvent", "LoySAInvoker", "LoyFail");
	}
}