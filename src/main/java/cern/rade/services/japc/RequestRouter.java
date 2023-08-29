package cern.rade.services.japc;
import cern.rade.services.codec.lvtojava.LVDataContainerFactory;
import cern.rade.services.codec.lvtojava.LVTypeDecoder;
import cern.rade.services.japc.RequestDecoder;
import cern.rade.services.common.communication.Request;
import cern.rade.services.japc.subscription.SubscriptionInitialiser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@WebServlet(urlPatterns = "/request")

public class RequestRouter extends HttpServlet {
    Logger logger = LoggerFactory.getLogger(RequestRouter.class);
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Request japcRequest = RequestDecoder.decodeMessage(request);
        logger.info("Received message from: {} with request: {}", request.getRemoteHost(), japcRequest);
        requestCaller(japcRequest, response);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getWriter().write("SERVLET IS UP");
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
