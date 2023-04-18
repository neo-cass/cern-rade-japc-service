package cern.rade.services.japc;
import cern.cmw.data.DataType;
import cern.japc.core.ParameterException;
import cern.rade.services.codec.lvtojava.LVDecoder;
import cern.rade.services.common.types.JavaContainer;
import cern.rade.services.japc.messages.RequestType;
import cern.rade.services.japc.subscription.SubscriptionClient;
import cern.rade.services.japc.subscription.SubscriptionManager;
import com.google.gson.Gson;
import cern.rade.services.japc.messages.MetaData;
import cern.rade.services.japc.messages.Request;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

@WebServlet(value = "/request")
public class RequestRouter extends HttpServlet {
    //TODO: put type descriptor in json (remember encoding issue)
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Request japcRequest = decodeMessage(request);
        requestCaller(japcRequest, response);
    }

    public Request decodeMessage(HttpServletRequest request) throws IOException {
        Gson gson = new Gson();
        String jsonMsg = request.getHeader("MetaData");

        MetaData metaData = gson.fromJson(jsonMsg, MetaData.class);
        switch(RequestType.valueOf(metaData.request_type)){
            case LV_GET, LV_SET, LV_SUBSCRIBE -> {return decodeLVRequest(request, metaData);}
            case PY_SUBSCRIBE -> {return decodePYRequest(request, metaData);}
            //TODO: cases for other languages
        }
        return null;
    }

    private Request decodeLVRequest(HttpServletRequest request, MetaData metaData) throws IOException {
        ServletInputStream reader = request.getInputStream();
        byte[] bytes = reader.readAllBytes();

        ByteBuffer buf = ByteBuffer.wrap(reader.readAllBytes());

        short typeLen = buf.getShort();
        ByteBuffer typeBuf = buf.slice(0, typeLen);
        ByteBuffer dataBuf = buf.slice(typeLen, buf.capacity() - typeLen);
        return new Request(metaData, LVDecoder.decode(typeBuf, dataBuf), metaData.request_type);
    }

    private Request decodePYRequest(HttpServletRequest request, MetaData metaData) throws IOException {
        String name = metaData.property;
        return new Request(metaData, new JavaContainer<>(name, null, DataType.DT_DATA), metaData.request_type);
    }

    public void requestCaller(Request request, HttpServletResponse response) throws IOException {
        switch(request.getRequestType()){
            case LV_GET -> handleJAPCGet(request, response);
            case LV_SET -> handleJAPCSet(request, response);
            case LV_SUBSCRIBE, PY_SUBSCRIBE -> handleJAPCSub(request, response);
        }
    }

    public void handleJAPCGet(Request request, HttpServletResponse response) throws IOException {
        //TODO: send data or error message
        String val;
        try {
            val = new GetSetManager(request).get();
        } catch (ParameterException e) {
            throw new RuntimeException(e);
        }
        response.getWriter().println(val);
        response.getWriter().flush();
    }

    public void handleJAPCSet(Request request, HttpServletResponse response) throws IOException {
        //TODO: send data or error message
        try {
            new GetSetManager(request).set();
        } catch (ParameterException e) {
            throw new RuntimeException(e);
        }
        response.getWriter().flush();
    }

    public void handleJAPCSub(Request request, HttpServletResponse response) throws IOException {
        SubscriptionClient client;
        String message;
        try {
             client = new SubscriptionClient(request);
             SubscriptionManager.addSubClient(request.getId(), client);
             message= String.valueOf(request.getId());
        } catch (ParameterException e) {
            e.printStackTrace();
            message = "Failed to create parameter, please check the server, device, property, field and the entry in CCDE";
        }
        response.getWriter().println(message);
        response.getWriter().flush();
    }
}
