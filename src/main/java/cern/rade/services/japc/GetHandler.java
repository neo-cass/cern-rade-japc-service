package cern.rade.services.japc;

import cern.japc.core.AcquiredParameterValue;
import cern.japc.core.Parameter;
import cern.japc.core.ParameterException;
import cern.japc.core.Selector;
import cern.rade.services.codec.japcjava.JAPCDecoder;
import cern.rade.services.codec.javatolv.LVDataEncoder;
import cern.rade.services.codec.javatolv.LVTypeEncoder;
import cern.rade.services.common.communication.AuthenticationType;
import cern.rade.services.common.communication.MetaData;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.java.PropertyContainer;
import cern.rade.services.japc.rbac.RBACClient;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.RbaToken;
import cern.rbac.util.holder.MiddleTierRbaTokenHolder;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

public class GetHandler {
    private HttpServletResponse response;
    private Client client;
    private DataContainer data;
    private MetaData responseMetaData;
    private Parameter parameter;
    private Selector selector;
    private AcquiredParameterValue apv;
    private RBACClient rbacClient;
    private int rbac_id;
    private HttpSender sender;
    private Logger logger = LoggerFactory.getLogger(GetHandler.class);

    public GetHandler(HttpServletResponse response, Client client){
        this.client = client;
        this.response = response;
        this.responseMetaData  = new MetaData();
    }

    private void init() throws IOException {
        this.sender= new HttpSender(response);
        this.data = client.getDataContainer();
        this.parameter = client.getParameter();
        this.selector = client.getSelector();
        this.rbacClient=client.getRbacClient();
    }

    public void get() throws ParameterException, AuthenticationException {
        if (client.getAuthenticationType()==AuthenticationType.NONE){
            japcGET();
        }
        else{
            japcGET_SECURE();
        }
    }

    private void japcGET() throws ParameterException {
        apv = parameter.getValue(selector);
        if(data==null){data = JAPCDecoder.decode(apv);}
        else{JAPCDecoder.decode((PropertyContainer) data, apv);}
    }

    private void japcGET_SECURE() throws ParameterException, AuthenticationException {
        RbaToken token = rbacClient.getApplicationToken();
        MiddleTierRbaTokenHolder.set(token);
        japcGET();
        MiddleTierRbaTokenHolder.clear();
    }

    private void respond() throws IOException {
        switch(client.getRequestLanguage()){
            case LABVIEW -> respondLV();
            case PYTHON -> respondPY();
        }
    }

    private void respondLV() throws IOException {
        responseMetaData.setAcq_stamp(apv.getHeader().getAcqStamp());
        if (client.getData() == null){
            short[] type_desc = LVTypeEncoder.encodeTypeDescriptor(data);
            responseMetaData.setType_descriptor(type_desc);
        }

        if(client.getAuthenticationType() != AuthenticationType.NONE){
            responseMetaData.setRbac_id(rbacClient.getRbac_id());
        }
        sender.send(LVDataEncoder.encode(data), responseMetaData);
    }

    private void respondPY() throws IOException {
        sender.send(data.toString());
    }

    public void execute(){
        try {
            init();
        } catch (ParameterException e) {
            sender.send("ERROR:" + e.getStackTrace()[0]);
            logger.error("Exception initialising parameter, check CCDB", e);
            return;
        } catch (IOException e) {
            sender.send("ERROR:" + Arrays.toString(e.getStackTrace()));
            logger.error("Exception initialising ", e);
        }
        catch (AuthenticationException e){
            sender.send("ERROR:" + Arrays.toString(e.getStackTrace()));
            logger.error("Exception initialising RBAC client.", e);
            return;
        }

        try {
            get();
        } catch (ParameterException e) {
            sender.send("ERROR:" + Arrays.toString(e.getStackTrace()));
            logger.error("Exception executing GET", e);
            return;
        } catch (AuthenticationException e){
            sender.send("ERROR:" + Arrays.toString(e.getStackTrace()));
            logger.error("Exception obtaining RBAC application token.", e);
            return;
        }

        try {
            respond();
        } catch (IOException e) {
            sender.send("ERROR:" + Arrays.toString(e.getStackTrace()));
            logger.error("Exception initialising RBAC", e);
        }
    }
}
