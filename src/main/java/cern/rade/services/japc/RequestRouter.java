package cern.rade.services.japc;
import cern.rade.services.codec.lvtojava.LVDecoder;
import cern.rade.services.japc.messages.RequestType;
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
        //TODO: this is messy change it
        switch(RequestType.getType(metaData.reqType)){
            case LV_GET, LV_SET, LV_SUBSCRIBE -> {return decodeLVRequest(request, metaData);}
            //TODO: cases for other languages
        }
        return null;
    }

    private Request decodeLVRequest(HttpServletRequest request, MetaData metaData) throws IOException {
        ServletInputStream reader = request.getInputStream();
        ByteBuffer buf = ByteBuffer.wrap(reader.readAllBytes());
        short typeLen = buf.getShort();
        ByteBuffer typeBuf = buf.slice(0, typeLen);
        ByteBuffer dataBuf = buf.slice(typeLen, buf.capacity() - typeLen);
        return new Request(metaData, LVDecoder.decode(typeBuf, dataBuf), metaData.reqType);
    }

    public void requestCaller(Request request, HttpServletResponse response) throws IOException {
        switch(request.requestType){
            case LV_GET -> handleJAPCGet(request, response);
            case LV_SET -> handleJAPCSet(request, response);
            case LV_SUBSCRIBE -> handleJAPCSub(request, response);
        }
    }

    public void handleJAPCGet(Request request, HttpServletResponse response) throws IOException {
        //TODO: send data or error message
        GetSetHandler.get(request);
        response.getWriter().println("");
    }

    public void handleJAPCSet(Request request, HttpServletResponse response) throws IOException {
        //TODO: send data or error message
        GetSetHandler.set(request);
        response.getWriter().println("");
    }

    public void handleJAPCSub(Request request, HttpServletResponse response) throws IOException {
        SubscriptionHandler.addRequest(request);
        response.getWriter().println(request.id);
        response.getWriter().flush();
    }
}





   // String name = Parameters.buildParameterName(metaData.device, jc.getFieldName());
//
//        PrintWriter out = response.getWriter();
//
//        Selector s = SelectorFactory.newSelector(metaData.selector);
//        ValueCallback valueCallback = new ValueCallback();
//
//        Parameter p = null;
//        SubscriptionHandle sh;
//        try {
//            p = ParameterFactory.newInstance().newParameter(name);
//            sh = p.createSubscription(s, valueCallback);
//            sh.startMonitoring();
//        } catch (ParameterException e) {
//            System.out.println(Arrays.toString(e.getStackTrace()));
//        }
