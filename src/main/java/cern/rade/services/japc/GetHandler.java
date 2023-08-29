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
import cern.rade.services.japc.rbac.RBACClient;
import cern.rade.services.japc.rbac.RBACClientFactory;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.RbaToken;
import cern.rbac.common.TokenFormatException;
import cern.rbac.util.holder.MiddleTierRbaTokenHolder;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private Logger logger = LoggerFactory.getLogger(GetHandler.class);

    public GetHandler(HttpServletResponse response, Request request){
        this.request = request;
        this.response = response;
    }

    private void init() throws ParameterException, AuthenticationException, TokenFormatException, IOException {
        //TODO data filter?
        this.sender = new HttpSender(response);
        this.metaData = request.getMetaData();
        this.data = request.getData();
        this.parameter = Util.buildParameter(request);
        this.selector = SelectorFactory.newSelector(metaData.getSelector());
        if(request.getAuthenticationType()!=null){initRBAC();}
    }

    private void initRBAC() throws AuthenticationException, TokenFormatException{
        this.rbacClient = new RBACClientFactory(request).createClient();
    }

    public void get() throws ParameterException, AuthenticationException {
        if (request.getAuthenticationType()==null){
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
        switch(request.getRequestLanguage()){
            case LABVIEW -> respondLV();
            case PYTHON -> respondPY();
        }
    }

    private void respondLV() throws IOException {
        MetaData responseMetaData = new MetaData();
        responseMetaData.setAcq_stamp(apv.getHeader().getAcqStamp());
        if (request.getData() == null){
            short[] type_desc = LVTypeEncoder.encodeTypeDescriptor(data);
            responseMetaData.setType_descriptor(type_desc);
        }

        if(request.getAuthenticationType() != null){
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
