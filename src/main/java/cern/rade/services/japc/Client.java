package cern.rade.services.japc;

import cern.japc.core.Parameter;
import cern.japc.core.Selector;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.japc.rbac.RBACClient;

public class Client {
    private Parameter parameter;
    private Selector selector;
    private DataContainer dataContainer;
    private RBACClient rbacClient;
    private boolean valid;
    private String invalidationReason;

    public Client(){
        this.valid=true;
    }

    public void setDataContainer(DataContainer dataContainer) {
        this.dataContainer = dataContainer;
    }

    public void setParameter(Parameter parameter) {
        this.parameter = parameter;
    }

    public void setRbacClient(RBACClient rbacClient) {
        this.rbacClient = rbacClient;
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    public Parameter getParameter() {
        return parameter;
    }

    public Selector getSelector() {
        return selector;
    }

    public DataContainer getDataContainer() {
        return dataContainer;
    }

    public RBACClient getRbacClient() {
        return rbacClient;
    }

    public boolean isValid(){
        return valid;
    }

    public String getInvalidationReason(){
        return invalidationReason;
    }

    public void invalidate(String reason){
        this.valid = false;
        this.invalidationReason = reason;
    }


}
