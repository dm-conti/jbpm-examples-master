package it.paybay.quigroup.loyalty.jbpm;

import it.paybay.quigroup.loyalty.jbpm.EnrollmentAddProcessTest.EnrollmentAddProcessConfig;
import it.paybay.quigroup.loyalty.jbpm.handler.CustomerWorkerItemHandler;
import it.paybay.quigroup.loyalty.jbpm.handler.LoyWorkerItemHandler;
import it.paybay.quigroup.loyalty.jbpm.handler.RedisWorkerItemHandler;
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
import org.apache.activemq.spring.ActiveMQConnectionFactory;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.connection.SingleConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes=EnrollmentAddProcessConfig.class, loader=AnnotationConfigContextLoader.class)
public class EnrollmentAddProcessTest extends JbpmJUnitBaseTestCase {
	private Map<String, Object> params;

	private KieSession ksession;

	@Autowired private LoyWorkerItemHandler loyWorkerItemHandler;
	@Autowired private VCWorkerItemHandler vCWorkerItemHandler;
	@Autowired private CustomerWorkerItemHandler customerWorkerItemHandler;
	@Autowired private RedisWorkerItemHandler redisWorkerItemHandler;
	
	@Mocked private JmsTemplate jmsTemplate;
	@Mocked private RedisTemplate<String, String> redisTemplate;
	@Mocked private BoundListOperations<String, String> boundListOperations;

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
		vCWorkerItemHandler.setKSession(ksession);
		customerWorkerItemHandler.setKSession(ksession);
		redisWorkerItemHandler.setKSession(ksession);

		ksession.getWorkItemManager().registerWorkItemHandler("loySAInvoker", loyWorkerItemHandler);
		ksession.getWorkItemManager().registerWorkItemHandler("vCSAInvoker", vCWorkerItemHandler);
		ksession.getWorkItemManager().registerWorkItemHandler("customerSAInvoker", customerWorkerItemHandler);
		ksession.getWorkItemManager().registerWorkItemHandler("redisInvoker", redisWorkerItemHandler);
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

			jmsTemplate.receive((Destination) any); returns((Message) response);
			
			redisTemplate.boundListOps(anyString); returns(boundListOperations);
			boundListOperations.leftPush(anyString); returns(1L);

		} };

		// start process
		ProcessInstance processInstance = ksession.startProcess("enrollment_add", params);

		// check whether the process instance has completed successfully
		// assertProcessInstanceCompleted(processInstance.getId(), ksession);

		// check what nodes have been triggered
		assertNodeTriggered(processInstance.getId(), 
			"StartEvent", 
			"LoySAInvoker", 
			"VCSAInvoker", 
			"CustomerSAInvoker", 
			"RedisInvoker",
			"StopEvent");
	}
	
	@Test @Ignore
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
	
	
	
	/**
	 * Class <code>EnrollmentAddProcess2Test.java</code>
	 * is a Inner Configuration Class which makes 
	 * the test class self-contained.
	 *
	 * @author Domenico Conti [domenico.conti@quigroup.it]
	 *
	 */
	
	@Configuration
	static class EnrollmentAddProcessConfig {
		final String HOSTNAME = "localhost";
		final int PORT = 6379;
		
		public @Bean JmsTemplate jmsTemplate(){
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory();
			connectionFactory.setBrokerURL("vm://" + HOSTNAME);
			
			SingleConnectionFactory jmsProducerConnectionFactory = new SingleConnectionFactory();
			jmsProducerConnectionFactory.setTargetConnectionFactory(connectionFactory);
			
			return new JmsTemplate(jmsProducerConnectionFactory); 
		}
		
		public @Bean RedisTemplate<String, String> redisTemplate(){
			JedisConnectionFactory connectionFactory = new JedisConnectionFactory();
			connectionFactory.setUsePool(false);
			connectionFactory.setHostName(HOSTNAME);
			connectionFactory.setPort(PORT);
			connectionFactory.setPassword("");
			
			RedisTemplate<String, String> redisTemplate = new RedisTemplate<String, String>();
			redisTemplate.setConnectionFactory(connectionFactory);
			
			return redisTemplate; 
		}
		
		//WorkItemHandler definitions
		public @Bean LoyWorkerItemHandler loyWorkerItemHandler(){ return new LoyWorkerItemHandler(); }
		public @Bean VCWorkerItemHandler vCWorkerItemHandler(){ return new VCWorkerItemHandler(); }
		public @Bean CustomerWorkerItemHandler customerWorkerItemHandler(){ return new CustomerWorkerItemHandler(); }
		public @Bean RedisWorkerItemHandler redisWorkerItemHandler(){ return new RedisWorkerItemHandler(); }
	}
}