package cern.rade.services.japc.communication;

import cern.japc.core.*;
import cern.japc.core.factory.ParameterFactory;
import cern.japc.core.factory.SelectorFactory;
import cern.rade.services.codec.javatolv.LVDataEncoder;
import cern.rade.services.codec.javatolv.LVTypeEncoder;
import cern.rade.services.codec.lvtojava.LVDecoder;
import cern.rade.services.japc.examples.TestParameterValueListener;
import cern.rade.services.japc.examples.UDPSender;
import cern.rade.services.japc.requesthandlers.SubscriptionHandler;
import cern.rade.services.japc.requesthandlers.ValueCallback;
import com.google.gson.Gson;
import cern.rade.services.japc.communication.messages.LVMetaData;
import cern.rade.services.japc.communication.messages.MetaData;
import cern.rade.services.japc.communication.messages.Request;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import cern.rade.services.common.types.JavaContainer;
import cern.rade.services.common.types.labview.LVRawTypeDescriptor;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class LVEntryPoint extends HttpServlet {
    private ServerSocket server;
    //TODO: put type descriptor in json
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Request req = decodeMessage(request);
        MetaData metaData = req.getMetaData();
        JavaContainer<?> jc = req.getData();

        String name = Parameters.buildParameterName(metaData.device, jc.getFieldName());

        ServletOutputStream out = response.getOutputStream();

        Selector s = SelectorFactory.newSelector(metaData.selector);
        ValueCallback valueCallback = new ValueCallback();

        Parameter p = null;
        SubscriptionHandle sh;
        try {
            p = ParameterFactory.newInstance().newParameter(name);
            sh = p.createSubscription(s, valueCallback);
            sh.startMonitoring();
        } catch (ParameterException e) {
            System.out.println("Parameter could not be created.");
            System.out.println("Subscription could not be started.");
            return;
        }

        Socket client = server.accept();
        SubscriptionThread thread = new SubscriptionThread(client, sh, valueCallback);
        ArrayList<Thread> threads = new ArrayList<>();
        Thread t = new Thread(thread);
        threads.add(t);
        t.start();
    }



    public Request decodeMessage(HttpServletRequest request) throws IOException {
        Gson gson = new Gson();
        String jsonMsg = request.getHeader("metaData");

        ServletInputStream reader = request.getInputStream();
        ByteBuffer buf = ByteBuffer.wrap(reader.readAllBytes());
        short typeLen = buf.getShort();
        ByteBuffer typeBuf = buf.slice(0, typeLen);
        ByteBuffer dataBuf = buf.slice(typeLen, buf.capacity() - typeLen);

        MetaData metaData = gson.fromJson(jsonMsg, MetaData.class);

        return new Request(metaData, LVDecoder.decode(typeBuf, dataBuf));
    }

    public void requestCaller(LVMetaData lvClientMessage){
//        Request request = lvClientMessage.toRequest();
//        System.out.println(request);
//        switch(lvClientMessage.reqType){
//            case GET -> new
//            case SET ->
//            case SUBSCRIBE ->
//        }
    }

    @Override
    public void init() throws ServletException {
        super.init();
        try {
            server = new ServerSocket(9999);
        } catch (IOException e) {
            System.out.println("Server port occupied");
        }
    }
}
