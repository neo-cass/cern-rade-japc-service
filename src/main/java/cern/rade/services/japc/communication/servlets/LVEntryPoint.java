package cern.rade.services.japc.communication.servlets;
import cern.rade.services.codec.lvtojava.LVDecoder;
import cern.rade.services.japc.core.RequestHandler;
import com.google.gson.Gson;
import cern.rade.services.japc.communication.messages.LVMetaData;
import cern.rade.services.japc.communication.messages.MetaData;
import cern.rade.services.japc.communication.messages.Request;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import cern.rade.services.common.types.JavaContainer;

import java.io.IOException;
import java.nio.ByteBuffer;

@WebServlet(value = "/entryPoint", loadOnStartup = 1)
public class LVEntryPoint extends HttpServlet {
    //TODO: put type descriptor in json (remember encoding issue)
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Request req = decodeMessage(request);
        requestCaller(req);
    }

    public Request decodeMessage(HttpServletRequest request) throws IOException {
        Gson gson = new Gson();
        String jsonMsg = request.getHeader("MetaData");

        ServletInputStream reader = request.getInputStream();
        ByteBuffer buf = ByteBuffer.wrap(reader.readAllBytes());
        short typeLen = buf.getShort();
        ByteBuffer typeBuf = buf.slice(0, typeLen);
        ByteBuffer dataBuf = buf.slice(typeLen, buf.capacity() - typeLen);

        MetaData metaData = gson.fromJson(jsonMsg, MetaData.class);

        return new Request(metaData, LVDecoder.decode(typeBuf, dataBuf), metaData.reqType);
    }

    public void requestCaller(Request req){
        switch(req.requestType){
            case GET -> RequestHandler.getGetRequests().add(req);
            case SET ->RequestHandler.getSetRequests().add(req);
            case SUBSCRIBE -> RequestHandler.getSubRequests().put(req.id, req);
        }
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
