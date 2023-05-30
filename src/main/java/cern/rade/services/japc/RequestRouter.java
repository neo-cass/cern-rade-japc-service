package cern.rade.services.japc;
import cern.japc.core.ParameterException;
import cern.rade.services.codec.RequestDecoder;
import cern.rade.services.common.communication.Request;
import cern.rade.services.japc.subscription.SubscriptionClient;
import cern.rade.services.japc.subscription.SubscriptionManager;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(value = "/request")
public class RequestRouter extends HttpServlet {
    //TODO: put type descriptor in json (remember encoding issue)
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Request japcRequest = RequestDecoder.decodeMessage(request);
        try {
            requestCaller(japcRequest, response);
        } catch (ParameterException e) {
            throw new RuntimeException(e);
        }
    }

    public void requestCaller(Request request, HttpServletResponse response) throws IOException, ParameterException {
        switch(request.getRequestAction()){
            case GET, GET_SECURE -> handleJAPCGet(new GetClient(response, request));
            case SET, SET_SECURE -> handleJAPCSet(new SetClient(response, request));
            case SUBSCRIBE -> handleJAPCSub(request, response);
        }
    }

    public void handleJAPCGet(GetClient handler)  {
       handler.execute();
    }

    public void handleJAPCSet(SetClient handler)  {
       handler.execute();
    }

    public void handleJAPCSub(Request request, HttpServletResponse response) throws IOException {
        String message;
        PrintWriter writer = response.getWriter();
        SubscriptionClient client = new SubscriptionClient(request);
        SubscriptionManager.addSubClient(request.getId(), client);
        message= String.valueOf(request.getId());
        writer.println(message);
        writer.flush();
    }
}
