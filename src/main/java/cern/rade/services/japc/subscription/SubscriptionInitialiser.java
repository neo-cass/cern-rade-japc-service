package cern.rade.services.japc.subscription;

import cern.japc.core.ParameterException;
import cern.rade.services.common.communication.AuthenticationType;
import cern.rade.services.common.communication.MetaData;
import cern.rade.services.common.communication.Request;
import cern.rade.services.japc.HttpSender;
import cern.rade.services.japc.rbac.RBACClient;
import cern.rade.services.japc.rbac.RBACClientFactory;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.TokenFormatException;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;

public class SubscriptionInitialiser {
    private Request request;
    private HttpSender sender;
    private HttpServletResponse response;
    private SubscriptionHandler handler;
    private Logger logger = LoggerFactory.getLogger(SubscriptionInitialiser.class);

    public SubscriptionInitialiser(Request request, HttpServletResponse response){
        this.request=request;
        this.response=response;
    }

    private void init() throws IOException, ParameterException, AuthenticationException {
        this.sender = new HttpSender(response);
        this.handler = new SubscriptionHandler(request);
        handler.init();
        SubscriptionManager.addSubClient(handler.getSub_id(), handler);
    }



    private void send(){
        int sub_id = handler.getSub_id();
        if(request.getAuthenticationType()==null){
            sender.send(String.valueOf(sub_id));
        }
        else{
            MetaData metaData = new MetaData();
            metaData.setRbac_id(handler.getRbac_id());
            sender.send(String.valueOf(sub_id), metaData);
        }
    }

    public void execute() {
        try {
            init();
        }
        catch (AuthenticationException | TokenFormatException e){
            logger.error("Error initialising RBAC.", e);
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
        } catch (ParameterException |TokenFormatException e) {
            logger.error("Error creating parameter, check CCDB.", e);;
        }

        send();
    }
}
