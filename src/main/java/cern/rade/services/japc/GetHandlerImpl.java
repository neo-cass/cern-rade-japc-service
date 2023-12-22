package cern.rade.services.japc;

import cern.japc.core.*;
import cern.japc.core.factory.ParameterFactory;
import cern.japc.core.factory.SelectorFactory;
import cern.rade.services.codec.CodecException;
import cern.rade.services.codec.japcjava.JAPCDecoder;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.java.PropertyContainer;
import cern.rade.services.japc.rbac.RBACClient;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.RbaToken;

import cern.rbac.util.holder.MiddleTierRbaTokenHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.crypto.Data;

public class GetHandlerImpl{
    protected DataContainer data;
    private Parameter parameter;
    private Selector selector;
    private AcquiredParameterValue apv;
    private String parameterName;
    protected String selectorName;
    protected RBACClient rbacClient;
    private final Logger logger = LoggerFactory.getLogger(GetHandlerImpl.class);

    public GetHandlerImpl(String selectorName, String device, String property, String field){
        this.parameterName = Parameters.buildParameterName(device, property, field);
        this.selectorName =selectorName;
    }

    public GetHandlerImpl(String selectorName, String device, String property){
        this.parameterName = Parameters.buildParameterName(device, property);
        this.selectorName =selectorName;
    }

    public GetHandlerImpl(String selectorName, String device, String property, String field, RBACClient rbacClient){
        this.parameterName = Parameters.buildParameterName(device, property, field);
        this.selectorName =selectorName;
        this.rbacClient = rbacClient;
    }

    public GetHandlerImpl(String selectorName, String device, String property, RBACClient rbacClient){
        this.parameterName = Parameters.buildParameterName(device, property);
        this.selectorName =selectorName;
        this.rbacClient = rbacClient;
    }

    public GetHandlerImpl(String selectorName, String device, RBACClient rbacClient){
        this.selectorName =selectorName;
        this.rbacClient = rbacClient;
    }

    public GetHandlerImpl(String selectorName, DataContainer data){
        this.selectorName = selectorName;
        this.data = data;
        this.parameterName = data.getName();
    }

    private void init() throws ParameterException {
        this.parameter = ParameterFactory.newInstance().newParameter(parameterName);
        this.selector = SelectorFactory.newSelector(selectorName);
    }

    private void get() throws ParameterException, AuthenticationException {
        if (rbacClient!=null){
            japcGET();
        }
        else{
            japcGET_SECURE();
        }
    }

    private void japcGET() throws ParameterException {
        apv = parameter.getValue(selector);
    }

    private void decode() throws CodecException {
        if(data==null){
            data = JAPCDecoder.decode(apv);
        }
        else{
            JAPCDecoder.decode((PropertyContainer) data, apv);
        }
    }

    private void japcGET_SECURE() throws ParameterException, AuthenticationException {
        RbaToken token = rbacClient.getApplicationToken();
        MiddleTierRbaTokenHolder.set(token);
        japcGET();
        MiddleTierRbaTokenHolder.clear();
    }

    public DataContainer execute() throws ParameterException, CodecException {
        init();
        get();
        decode();
        return data;
    }

}
