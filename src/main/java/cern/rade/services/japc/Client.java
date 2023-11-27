package cern.rade.services.japc;

import cern.japc.core.Parameter;
import cern.japc.core.Selector;
import cern.rade.services.common.communication.AuthenticationType;
import cern.rade.services.common.communication.RequestAction;
import cern.rade.services.common.communication.RequestLanguage;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;
import cern.rade.services.japc.rbac.RBACClient;

public class Client {
    private Parameter parameter;
    private Selector selector;
    private DataContainer dataContainer;
    private RBACClient rbacClient;
    private boolean valid;
    private String invalidationReason;
    private byte[] rawData;
    private short[] type_descriptor;
    private LVTypeDescriptor lvTypeDescriptor;
    private RequestAction action;
    private AuthenticationType authenticationType;
    private RequestLanguage requestLanguage;
    private String remoteHost;
    public Client(){
        this.valid=true;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public String getRemoteHost() {
        return remoteHost;
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

    public byte[] getRawData() {
        return rawData;
    }

    public void setRawData(byte[] rawData) {
        this.rawData = rawData;
    }

    public void invalidate(String reason){
        this.valid = false;
        this.invalidationReason = reason;
    }

    public RequestAction getAction() {
        return action;
    }

    public void setAction(RequestAction action) {
        this.action = action;
    }

    public void setLvTypeDescriptor(LVTypeDescriptor lvTypeDescriptor) {
        this.lvTypeDescriptor = lvTypeDescriptor;
    }

    public LVTypeDescriptor getLvTypeDescriptor() {
        return lvTypeDescriptor;
    }

    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(AuthenticationType authenticationType) {
        this.authenticationType = authenticationType;
    }

    public RequestLanguage getRequestLanguage() {
        return requestLanguage;
    }

    public void setRequestLanguage(RequestLanguage requestLanguage) {
        this.requestLanguage = requestLanguage;
    }

    public void setType_descriptor(short[] type_descriptor) {
        this.type_descriptor = type_descriptor;
    }

    public short[] getType_descriptor() {
        return type_descriptor;
    }
}
