package it.paybay.quigroup.loyalty.jbpm;

import it.paybay.quigroup.loyalty.jbpm.message.RegistrationRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.kie.api.runtime.KieSession;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jms.core.JmsTemplate;


public class RunDemo {

	public static void main(String[] args) throws IOException {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:META-INF/spring/application-context.xml");
		KieSession session = (KieSession) context.getBean("ksession1");
		
		JmsTemplate jmsTemplate = (JmsTemplate) context.getBean("jmsTemplate");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));       

        System.out.println("OK :: RUNNING");

        for (;;) {
            System.out.print("To Register, Enter Name: ");
            String name = reader.readLine();
            RegistrationRequest request = new RegistrationRequest(name);
            jmsTemplate.convertAndSend(request);
        }
	}
}