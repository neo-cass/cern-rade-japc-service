package cern.rade.services.japc.subscription;

import cern.japc.core.ParameterException;
import cern.japc.core.ParameterRuntimeException;
import cern.rade.services.common.communication.AuthenticationType;
import cern.rade.services.common.communication.MetaData;
import cern.rade.services.japc.ContextProvider;
import cern.rade.services.japc.Client;
import cern.rade.services.japc.HttpSender;
import cern.rade.services.japc.ErrorUtil;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.TokenFormatException;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SubscriptionInitialiser {
    private Client client;
    private HttpSender sender;
    private HttpServletResponse response;
    private SubscriptionHandler handler;
    private final MetaData responseMetaData = new MetaData();
    private final SubscriptionManager subManager = ContextProvider.getSubManager();
    private final Logger logger = LoggerFactory.getLogger(SubscriptionInitialiser.class);

    public SubscriptionInitialiser(HttpServletResponse response,Client client){
        this.client=client;
        this.response=response;
    }

    private void init() throws IOException, ParameterException, AuthenticationException {
        this.sender = new HttpSender(response);
        this.handler = new SubscriptionHandler(client);
        handler.init();
        subManager.addSubHandler(handler.getSub_id(), handler);
    }

    private boolean respond(){
        int sub_id = handler.getSub_id();
        responseMetaData.setSub_id(sub_id);
        if(client.getAuthenticationType()!=AuthenticationType.NONE){
            responseMetaData.setRbac_id(client.getRbacClient().getRbac_id());
        }
        if(client.getType_descriptor()==null){
            responseMetaData.setType_descriptor(handler.getType_descriptor());
        }
        return sender.send("SUCCESS", responseMetaData);
    }

    public void execute() {
        try {
            init();
        }
        catch (AuthenticationException | TokenFormatException e){
            ErrorUtil.sendErrorResponse(sender, logger, responseMetaData, "Error initialising RBAC.", e);
            return;
        } catch (IOException e) {
            logger.error("Error initialising response sender.", e);
            return;
        }
        catch (ParameterException e){
            logger.error("Error creating parameter, check CCDB.", e);
            return;
        }

        try {
            handler.init();
        } catch (ParameterException e) {
            ErrorUtil.sendErrorResponse(sender, logger, responseMetaData, "Error starting subscription", e);
            return;
        } catch(AuthenticationException e){
            ErrorUtil.sendErrorResponse(sender, logger, responseMetaData, "Error authenticating with RBAC.", e);
            return;
        } catch (ParameterRuntimeException e){
            ErrorUtil.sendErrorResponse(sender, logger, responseMetaData, "Unrecoverable problem encountered when starting subscription", e);
            return;
        }
        respond();
    }
}
