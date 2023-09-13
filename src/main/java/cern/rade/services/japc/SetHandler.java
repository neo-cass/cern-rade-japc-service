package cern.rade.services.japc;

import cern.japc.core.Parameter;
import cern.japc.core.ParameterException;
import cern.japc.core.Selector;
import cern.japc.value.ParameterValue;
import cern.rade.services.codec.CodecException;
import cern.rade.services.codec.japcjava.JAPCEncoder;
import cern.rade.services.codec.lvtojava.LVDataDecoder;
import cern.rade.services.common.communication.AuthenticationType;
import cern.rade.services.common.communication.MetaData;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.japc.rbac.RBACClient;
import cern.rade.services.japc.rbac.RBACManager;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.RbaToken;
import cern.rbac.util.holder.MiddleTierRbaTokenHolder;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

public class SetHandler {
    private HttpServletResponse response;
    private Client client;
    private DataContainer data;
    private MetaData responseMetaData;
    private Parameter parameter;
    private Selector selector;
    private RBACClient rbacClient;
    private HttpSender sender;
    private final RBACManager rbacManager = ContextProvider.getRbacManager();
    private final Logger logger = LoggerFactory.getLogger(SetHandler.class);

    public SetHandler(HttpServletResponse response, Client client){
        this.client = client;
        this.response = response;
        this.responseMetaData  = new MetaData();
    }

    private void init() throws IOException, CodecException {
        this.sender= new HttpSender(response);
        this.data = client.getDataContainer();
        LVDataDecoder.decode(ByteBuffer.wrap(client.getRawData()),
                data, client.getLvTypeDescriptor());
        this.parameter = client.getParameter();
        this.selector = client.getSelector();
        this.rbacClient=client.getRbacClient();
    }

    private void set() throws ParameterException, CodecException {
        if(client.getAuthenticationType()==AuthenticationType.NONE){
            japcSET();
        }
        else{
            japcSET_SECURE();
        }
    }

    private void japcSET() throws ParameterException, CodecException {
        ParameterValue pv = JAPCEncoder.encode(data);
        parameter.setValue(selector, pv);
    }

    private void japcSET_SECURE() throws ParameterException, AuthenticationException, CodecException {
        RbaToken token = rbacClient.getApplicationToken();
        MiddleTierRbaTokenHolder.set(token);
        japcSET();
        MiddleTierRbaTokenHolder.clear();
    }

    private boolean respond() {
        if(client.getAuthenticationType()==AuthenticationType.NONE){
            return sender.send("SUCCESS");
        }
        else{
            responseMetaData.setRbac_id(rbacClient.getRbac_id());
            return sender.send("SUCCESS", responseMetaData);
        }
    }
    public void execute(){
        try {
            init();
        } catch (IOException e) {
            logger.error("Error handling set operation.", e);
            return;
        } catch (CodecException e){
            ErrorUtil.sendErrorResponse(sender, logger, responseMetaData, "Failed to decode data provided for the SET operation.", e);
        }

        try {
            set();
        } catch (ParameterException e) {
            ErrorUtil.sendErrorResponse(sender, logger, responseMetaData, "Exception executing SET", e);
            return;
        } catch (AuthenticationException e){
            ErrorUtil.sendErrorResponse(sender, logger, responseMetaData, "Failed to authenticate with RBAC", e);
            rbacManager.removeRBACClient(rbacClient.getRbac_id());
            return;
        }catch (CodecException e){
            ErrorUtil.sendErrorResponse(sender, logger, responseMetaData, "Failed to encode data to do the SET operation.", e);

        }
        //respond and if send fails (and the user is not using already cached rbac) remove the rbac client
        if (!respond() && client.getAuthenticationType() != AuthenticationType.RBAC_CACHED){
            rbacManager.removeRBACClient(rbacClient.getRbac_id());
        }
    }

}

