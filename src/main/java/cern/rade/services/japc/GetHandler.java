package cern.rade.services.japc;

import cern.japc.core.AcquiredParameterValue;
import cern.japc.core.Parameter;
import cern.japc.core.ParameterException;
import cern.japc.core.Selector;
import cern.japc.core.factory.SelectorFactory;
import cern.rade.services.codec.japcjava.JAPCDecoder;
import cern.rade.services.codec.javatolv.LVDataEncoder;
import cern.rade.services.codec.javatolv.LVTypeEncoder;
import cern.rade.services.common.communication.AuthenticationType;
import cern.rade.services.common.communication.MetaData;
import cern.rade.services.common.communication.Request;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.java.PropertyContainer;
import cern.rade.services.japc.examples.JAPCActions;
import cern.rade.services.japc.rbac.RBACClient;
import cern.rade.services.japc.rbac.RBACManager;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.RbaToken;
import static cern.rade.services.common.BytesUtil.byteArrToShortArr;
import cern.rbac.util.holder.MiddleTierRbaTokenHolder;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;

public class GetHandler {
    private HttpServletResponse response;
    private Request request;
    private DataContainer data;
    private MetaData metaData;
    private Parameter parameter;
    private Selector selector;
    private AcquiredParameterValue apv;
    private RBACClient rbacClient;
    private int rbac_id;
    private HttpSender sender;

    public GetHandler(HttpServletResponse response, Request request){
        this.request = request;
        this.response = response;
    }
    private void init() throws ParameterException, IOException {
        this.sender = new HttpSender(response);
        this.metaData = request.getMetaData();
        this.data = request.getData();
        this.parameter = JAPCActions.buildParameter(metaData.getProperty(), metaData.getDevice(), null);
        this.selector = SelectorFactory.newSelector(metaData.getSelector());
        if(request.getAuthenticationType() != AuthenticationType.NORMAL){
            initRBAC();
        }
    }

    private void initRBAC() {
        this.rbac_id = metaData.getRbac_id();
        this.rbacClient = RBACManager.getClient(rbac_id);
        if(rbac_id == 0 || rbacClient==null){
            initNewRBAC();
        }
    }

    private void initNewRBAC() {
        try{
            this.rbacClient = new RBACClient(metaData.getUsername(), metaData.getPassword());
            RBACManager.addClient(rbacClient);
        }
        catch (AuthenticationException e){
           sender.send("auth failed");
        }
    }

    public void get() throws ParameterException {
        switch(request.getAuthenticationType()){
            case NORMAL -> japcGET();
            case SECURE -> japcGET_SECURE();
        }
    }

    private void japcGET() throws ParameterException {
        apv = parameter.getValue(selector);
        if(data==null){data = JAPCDecoder.decode(apv);}
        else{JAPCDecoder.decode((PropertyContainer) data, apv);}
    }

    private void japcGET_SECURE() throws ParameterException {
        RbaToken token = rbacClient.getApplicationToken();
        MiddleTierRbaTokenHolder.set(token);
        japcGET();
        MiddleTierRbaTokenHolder.clear();
    }

    private void send() throws IOException {
        switch(request.getRequestLanguage()){
            case LABVIEW -> sendLV();
            case PYTHON -> sendPY();
        }
    }

    private void sendLV() throws IOException {
        MetaData responseMetaData = new MetaData();
        responseMetaData.setAcq_stamp(apv.getHeader().getAcqStamp());
        if (request.getData() == null){
            short[] type_desc = byteArrToShortArr(LVTypeEncoder.encodeTypeDescriptor(data));
            responseMetaData.setType_descriptor(type_desc);
        }

        if(request.getAuthenticationType() != AuthenticationType.NORMAL){
            responseMetaData.setRbac_id(rbac_id);
        }

        sender.send(LVDataEncoder.encode(data), responseMetaData);
    }

    private void sendPY() throws IOException {
        sender.send(data.toString());
    }

    public void execute(){
        try {
            init();
        } catch (ParameterException | IOException e) {
            sender.send(Arrays.toString(e.getStackTrace()));
            return;
        }

        try {
            get();
        } catch (ParameterException e) {
            sender.send(Arrays.toString(e.getStackTrace()));
            return;
        }

        try {
            send();
        } catch (IOException e) {
            sender.send(Arrays.toString(e.getStackTrace()));
        }
    }
}
