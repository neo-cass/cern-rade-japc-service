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

public class GetService {
    private DataContainer data;
    private Parameter parameter;
    private Selector selector;
    private AcquiredParameterValue apv;
    private String parameterName;
    private String selectorName;
    private RBACClient rbacClient;
    private long acquisitionStamp;
    private boolean isExecuted = false;
    private final Logger logger = LoggerFactory.getLogger(GetService.class);

    public GetService(String selectorName, String device, String property, String field){
        this.parameterName = Parameters.buildParameterName(device, property, field);
        this.selectorName = selectorName;
    }

    public GetService(String selectorName, String device, String property){
        this.parameterName = Parameters.buildParameterName(device, property);
        this.selectorName =selectorName;
    }

    public GetService(String selectorName, String device, String property, String field, RBACClient rbacClient){
        this.parameterName = Parameters.buildParameterName(device, property, field);
        this.selectorName =selectorName;
        this.rbacClient = rbacClient;
    }

    public GetService(String selectorName, String device, String property, RBACClient rbacClient){
        this.parameterName = Parameters.buildParameterName(device, property);
        this.selectorName = selectorName;
        this.rbacClient = rbacClient;
    }

    public GetService(String selectorName, String device, DataContainer data, RBACClient rbacClient){
        this.selectorName =selectorName;
        this.parameterName = Parameters.buildParameterName(device, data.getName());
        this.data = data;
        this.rbacClient = rbacClient;
    }

    public GetService(String selectorName, String device, DataContainer data){
        this.selectorName = selectorName;
        this.parameterName = Parameters.buildParameterName(device, data.getName());
        this.data = data;
    }




    private void init() throws ParameterException {
        this.parameter = ParameterFactory.newInstance().newParameter(parameterName);
        this.selector = SelectorFactory.newSelector(selectorName);
    }

    private void get() throws ParameterException, AuthenticationException {
        if (rbacClient==null){
            japcGET();
        }
        else{
            japcGET_SECURE();
        }
    }

    private void japcGET() throws ParameterException {
        this.apv = parameter.getValue(selector);
        this.acquisitionStamp = apv.getHeader().getAcqStamp();
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

    public void execute() throws ParameterException, CodecException {
        init();
        get();
        decode();
        this.isExecuted = true;
    }

    public boolean isExecuted() {
        return isExecuted;
    }

    public DataContainer getData() {
        if(!isExecuted){
            throw new RuntimeException("GetService has not been executed yet. You cant retrieve the data");
        }
        return data;
    }

    public long getAcquisitionStamp() {
        if(!isExecuted){
            throw new RuntimeException("GetService has not been executed yet. You cant retrieve the acquisition stamp.");
        }
        return acquisitionStamp;
    }

    public RBACClient getRbacClient() {
        return rbacClient;
    }
}

