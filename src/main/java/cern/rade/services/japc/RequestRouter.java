package cern.rade.services.japc;
import cern.japc.core.ParameterException;
import cern.rade.services.codec.RequestDecoder;
import cern.rade.services.codec.javatolv.LVDataEncoder;
import cern.rade.services.codec.javatolv.LVTypeEncoder;
import cern.rade.services.common.communication.client.Request;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.japc.subscription.SubscriptionClient;
import cern.rade.services.japc.subscription.SubscriptionManager;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import cern.rade.services.common.communication.server.ServerMetaData;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import static cern.rade.services.common.BytesUtil.byteArrToShortArr;

@WebServlet(value = "/request")
public class RequestRouter extends HttpServlet {
    //TODO: put type descriptor in json (remember encoding issue)
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Request japcRequest = RequestDecoder.decodeMessage(request);
        requestCaller(japcRequest, response);
    }

    public void requestCaller(Request request, HttpServletResponse response) throws IOException {
        switch(request.getRequestAction()){
            case GET -> handleJAPCGet(request, response);
            case SET -> handleJAPCSet(request, response);
            case SUBSCRIBE-> handleJAPCSub(request, response);
        }
    }

    public void sendData(HttpServletResponse response, DataContainer data, Request request) throws IOException {
        switch(request.getRequestLanguage()){
            case LABVIEW -> sendLVData(response, data);
            case PYTHON -> {return;} //TODO: implement
        }

    }

    public void sendLVData(HttpServletResponse response, DataContainer data) throws IOException {
        OutputStream out;
        try {
            out = response.getOutputStream();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        byte[] LVData = LVDataEncoder.encode(data);
        byte[] typeDesc = LVTypeEncoder.encodeTypeDescriptor(data);
        Gson gson = new Gson();

        ServerMetaData metaData = new ServerMetaData(10L, byteArrToShortArr(typeDesc));
        String flattenedMetaData = gson.toJson(metaData);
        response.addHeader("MetaData", flattenedMetaData);
        out.write(LVData);
    }

    private void sendRawData(HttpServletResponse response, OutputStream out){

    }

    public void handleJAPCGet(Request request, HttpServletResponse response) throws IOException {
        //TODO: send data or error message
        DataContainer<?> javaContainer = JAPCActions.get(request);
        sendData(response, javaContainer, request);
    }

    public void handleJAPCSet(Request request, HttpServletResponse response) throws IOException {
        //TODO: send data or error message
        JAPCActions.set(request);
    }

    public void handleJAPCSub(Request request, HttpServletResponse response) throws IOException {
        SubscriptionClient client;
        String message;
        PrintWriter writer = response.getWriter();
        try {
             client = new SubscriptionClient(request);
             SubscriptionManager.addSubClient(request.getId(), client);
             message= String.valueOf(request.getId());

        } catch (ParameterException e) {
            e.printStackTrace();
            message = "Failed to create parameter, please check the server, device, property, field and the entry in CCDE";
        }
        writer.println(message);
        writer.flush();
    }
}
