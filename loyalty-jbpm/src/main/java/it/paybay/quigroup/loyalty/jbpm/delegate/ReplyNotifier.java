package it.paybay.quigroup.loyalty.jbpm.delegate;

import it.paybay.quigroup.loyalty.jbpm.message.RegistrationReply;

public class ReplyNotifier {

    public void notify(RegistrationReply reply) {
        System.out.println(reply);
    }
}
