package cern.rade.services.japc;
import cern.rade.services.codec.RequestDecoder;
import cern.rade.services.common.communication.Request;
import cern.rade.services.japc.subscription.SubscriptionInitialiser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(value = "/request")
public class RequestRouter extends HttpServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Request japcRequest = RequestDecoder.decodeMessage(request);
        requestCaller(japcRequest, response);
    }

    public void requestCaller(Request request, HttpServletResponse response)  {
        switch(request.getRequestAction()){
            case GET -> handleJAPCGet(new GetHandler(response, request));
            case SET -> handleJAPCSet(new SetHandler(response, request));
            case SUBSCRIBE -> handleJAPCSub(new SubscriptionInitialiser(request, response));
        }
    }

    public void handleJAPCGet(GetHandler handler)  {
       handler.execute();
    }

    public void handleJAPCSet(SetHandler handler)  {
       handler.execute();
    }

    public void handleJAPCSub(SubscriptionInitialiser handler) {
        handler.execute();
    }
}
