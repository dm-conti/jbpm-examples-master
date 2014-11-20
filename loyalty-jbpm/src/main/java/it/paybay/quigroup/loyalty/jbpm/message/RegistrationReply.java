package it.paybay.quigroup.loyalty.jbpm.message;

import java.io.Serializable;

public class RegistrationReply implements Serializable {

    private static final long serialVersionUID = -2119692510721245260L;

    private String name;
    private int confirmationId;

    public RegistrationReply(String name, int confirmationId) {
        this.name = name;
        this.confirmationId = confirmationId;
    }

    public String toString() {
        return (confirmationId >= 0) 
                ? name + ": Confirmed #" + confirmationId 
                : name + ": Not Confirmed";
    }
}
