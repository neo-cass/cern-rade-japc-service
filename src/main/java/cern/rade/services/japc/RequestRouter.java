package cern.rade.services.japc;
import cern.rade.services.common.communication.MetaData;
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
        Client client = RequestHandler.decodeRequest(request);
        if(!client.isValid()){
            rejectClient(client, response);
            return;
        }
        requestCaller(client, response);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getWriter().write("SERVLET IS UP");
    }

    public void requestCaller(Client client, HttpServletResponse response)  {
        switch(client.getAction()){
            case GET -> handleJAPCGet(new GetHandler(response, client));
            case SET -> handleJAPCSet(new SetHandler(response, client));
            //case SUBSCRIBE -> handleJAPCSub(new SubscriptionInitialiser(response, client));
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

    public void rejectClient(Client client, HttpServletResponse response) throws IOException {
        HttpSender sender = new HttpSender(response);
        MetaData metaData = new MetaData();
        metaData.setError(client.getInvalidationReason());
        sender.send("FAILURE", metaData);
    }
}
