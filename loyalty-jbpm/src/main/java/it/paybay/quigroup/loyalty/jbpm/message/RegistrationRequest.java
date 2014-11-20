package it.paybay.quigroup.loyalty.jbpm.message;

import java.io.Serializable;

public class RegistrationRequest implements Serializable {

    private static final long serialVersionUID = -6097635701783502292L;

    private String name;

    public RegistrationRequest(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}