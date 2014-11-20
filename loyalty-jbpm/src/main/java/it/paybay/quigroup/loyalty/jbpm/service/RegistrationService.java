package it.paybay.quigroup.loyalty.jbpm.service;

import it.paybay.quigroup.loyalty.jbpm.message.RegistrationReply;
import it.paybay.quigroup.loyalty.jbpm.message.RegistrationRequest;

import java.util.HashMap;
import java.util.Map;

public class RegistrationService {

    private Map registrations = new HashMap();
    private int counter = 100;

    public RegistrationReply processRequest(RegistrationRequest request) {
        int id = counter++;
        if (id % 5 == 0) {
            id = -1;
        }
        else {
            registrations.put(new Integer(id), request);
        }
        return new RegistrationReply(request.getName(), id);
    }
}
