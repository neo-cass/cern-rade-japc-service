package cern.rade.services.japc.subscription;

import cern.japc.core.ParameterException;
import cern.japc.core.ParameterRuntimeException;
import cern.rade.services.common.communication.AuthenticationType;
import cern.rade.services.common.communication.MetaData;
import cern.rade.services.japc.ContextProvider;
import cern.rbac.client.authentication.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SubscriptionInitialiser {
    private ArrayList<String> header;
    private Map<String, List<String>> headers;
    private Client client;
    private HttpSender sender;
    private SubscriptionHandler handler;
    private final MetaData responseMetaData = new MetaData();
    private final SubscriptionManager subManager = ContextProvider.getSubManager();
    private final Logger logger = LoggerFactory.getLogger(SubscriptionInitialiser.class);

    public SubscriptionInitialiser(Map<String, List<String>> headers, Client client){
        this.client=client;
        this.headers = headers;
        this.header = new ArrayList<>(1);
        headers.put("MetaData", header);
    }

    private void init() throws ParameterException, AuthenticationException {
        this.handler = new SubscriptionHandler(client);
        handler.init();
        subManager.addSubHandler(handler.getSub_id(), handler);
    }
    public SubscriptionHandler getHandler(){
        return handler;
    }
    private void respond(){
        int sub_id = handler.getSub_id();
        responseMetaData.setSub_id(sub_id);
        if(client.getAuthenticationType()!=AuthenticationType.NONE){
            responseMetaData.setRbac_id(client.getRbacClient().getRbac_id());
        }
        if(client.getType_descriptor()==null){
            responseMetaData.setType_descriptor(handler.getType_descriptor());
        }
        setHeader();
    }

    private void setHeader(){
        header.add(responseMetaData.flatten());
    }

    private void handleError(Exception e, String message){
        responseMetaData.setError(ErrorUtil.createErrorMessage(e, message));
        logger.error(message, e);
        setHeader();
    }

    public void execute() {
        try {
            init();
        }
        catch (AuthenticationException e){
            handleError(e, "Error authenticating with RBAC.");
            return;
        }
        catch (ParameterException e){
            handleError(e, "Error in parameter.");
            return;
        }

        try {
            handler.init();
        } catch (ParameterException e) {
            handleError(e, "Error starting subscription.");
            return;
        } catch(AuthenticationException e){
            handleError(e, "Error authenticating with RBAC.");
            return;
        } catch (ParameterRuntimeException e){
            handleError(e, "Unrecoverable problem encountered when starting subscription.");
            return;
        }
        respond();
    }
}
