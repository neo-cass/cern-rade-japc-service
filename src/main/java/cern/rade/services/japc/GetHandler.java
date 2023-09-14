package cern.rade.services.japc;

import cern.japc.core.AcquiredParameterValue;
import cern.japc.core.Parameter;
import cern.japc.core.ParameterException;
import cern.japc.core.Selector;
import cern.rade.services.codec.CodecException;
import cern.rade.services.codec.japcjava.JAPCDecoder;
import cern.rade.services.codec.javatolv.LVDataEncoder;
import cern.rade.services.codec.javatolv.LVTypeEncoder;
import cern.rade.services.common.communication.AuthenticationType;
import cern.rade.services.common.communication.MetaData;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.java.PropertyContainer;
import cern.rade.services.japc.rbac.RBACClient;
import cern.rade.services.japc.rbac.RBACManager;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.RbaToken;

import cern.rbac.util.holder.MiddleTierRbaTokenHolder;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GetHandler {
    private HttpServletResponse response;
    private Client client;
    private DataContainer data;
    private MetaData responseMetaData;
    private Parameter parameter;
    private Selector selector;
    private AcquiredParameterValue apv;
    private RBACClient rbacClient;
    private HttpSender sender;
    private final RBACManager rbacManager = ContextProvider.getRbacManager();
    private final Logger logger = LoggerFactory.getLogger(GetHandler.class);

    public GetHandler(HttpServletResponse response, Client client){
        this.client = client;
        this.response = response;
        this.responseMetaData  = new MetaData();
        this.data = client.getDataContainer();
        this.parameter = client.getParameter();
        this.selector = client.getSelector();
        this.rbacClient=client.getRbacClient();

    }

    private void init() throws IOException {
        this.sender= new HttpSender(response);

    }

    public void get() throws ParameterException, AuthenticationException, CodecException {
        if (client.getAuthenticationType()==AuthenticationType.NONE){
            japcGET();
        }
        else{
            japcGET_SECURE();
        }
    }

    private void japcGET() throws ParameterException, CodecException {
        apv = parameter.getValue(selector);
        if(data==null){
            data = JAPCDecoder.decode(apv);
            short[] type_descriptor = LVTypeEncoder.encodeTypeDescriptor(data);
            responseMetaData.setType_descriptor(type_descriptor);
        }
        else{
            JAPCDecoder.decode((PropertyContainer) data, apv);
        }
    }

    private void japcGET_SECURE() throws ParameterException, AuthenticationException, CodecException {
        RbaToken token = rbacClient.getApplicationToken();
        MiddleTierRbaTokenHolder.set(token);
        japcGET();
        MiddleTierRbaTokenHolder.clear();
    }

    private boolean respond() throws CodecException {
        switch(client.getRequestLanguage()){
            case LABVIEW -> {
                return respondLV();
            }
            case PYTHON -> {
                return respondPY();
            }
        }
        return false;
    }

    private boolean respondLV() throws CodecException {
        responseMetaData.setAcq_stamp(apv.getHeader().getAcqStamp());
        if(client.getAuthenticationType() != AuthenticationType.NONE){
            responseMetaData.setRbac_id(rbacClient.getRbac_id());
        }
        return sender.send(LVDataEncoder.encode(data), responseMetaData);
    }

    private boolean respondPY()  {
        //TODO py client
        return sender.send(data.toString());
    }

    public void execute(){
        try {
            init();
        } catch (IOException e) {
            logger.error("Exception initialising HTTP sender.", e);
        } catch (AuthenticationException e){
            ErrorUtil.sendErrorResponse(sender, logger, responseMetaData, "Exception initialising RBAC", e);
            return;
        }

        try {
            get();
        } catch (ParameterException e) {
            ErrorUtil.sendErrorResponse(sender, logger, responseMetaData, "Exception executing GET", e);
            return;
        } catch (AuthenticationException e){
            ErrorUtil.sendErrorResponse(sender, logger, responseMetaData, "RBAC Token expired", e);
            return;
        }catch (CodecException e) {
            ErrorUtil.sendErrorResponse(sender, logger, responseMetaData, "Error encoding data", e);
            return;
        }
        try {
            //TODO would be better if encoding done in separate step
            if (!respond() && client.getAuthenticationType() != AuthenticationType.RBAC_CACHED){
                rbacManager.removeRBACClient(rbacClient.getRbac_id());
            }
        } catch (CodecException e) {
            ErrorUtil.sendErrorResponse(sender, logger, responseMetaData, "Exception encoding data.", e);
        }
    }
}
