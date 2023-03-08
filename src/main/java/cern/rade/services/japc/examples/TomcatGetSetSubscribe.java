package cern.rade.services.japc.examples;

import cern.japc.core.*;
import cern.japc.core.factory.ParameterFactory;
import cern.japc.core.factory.SelectorFactory;
import cern.japc.core.factory.SimpleParameterValueFactory;
import cern.japc.core.spi.subscription.SubscriptionFilter;
import cern.japc.value.ParameterValue;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

@WebServlet(value = "/japcTest")
public class TomcatGetSetSubscribe extends HttpServlet{
    private SubscriptionHandle sh;
    private HashMap<UDPSender, Thread> threadPool;
    private ArrayList<Integer> portPool;
    public void init() {
        threadPool = new HashMap<UDPSender, Thread>();
        portPool = new ArrayList<Integer>();
        for (int i = 1024; i<49152; i++){
            portPool.add(i);
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String requestType = request.getHeader("RequestType");
        //String deviceName = request.getHeader("DeviceName");
        String propertyName = request.getHeader("PropertyName");
        String deviceName = request.getHeader("DeviceName");
        String fieldName = request.getHeader("FieldName");
        System.out.println(propertyName + deviceName + fieldName);
        try {
            switch (requestType) {
                case "Get" -> get(fieldName, deviceName, propertyName, response);
                case "Subscribe" -> subscribe(propertyName, response);
                case "Set" -> {String valueString = request.getHeader("Value");set(Double.parseDouble(valueString), propertyName, response);}
            }
        }
        catch(ParameterException | IOException e){
            e.printStackTrace();
        }
        response.getWriter().println("Build Successful");
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        out.println("Build Successful");
    }


    private void get(String fieldName, String deviceName, String propertyName, HttpServletResponse response) throws ParameterException, IOException {
        PrintWriter out = response.getWriter();
        String name = Parameters.buildParameterName(deviceName, propertyName, fieldName);
        System.out.println(name);
        Parameter p = ParameterFactory.newInstance().newParameter(name);
        cern.japc.core.Selector s = SelectorFactory.newSelector("");
        AcquiredParameterValue apv = p.getValue(s);
        out.println(apv.getValue().castAsSimple().getDouble());
    }

    private void set(double value, String parameterName, HttpServletResponse response) throws ParameterException, IOException {
        PrintWriter out = response.getWriter();
        Parameter p = ParameterFactory.newInstance().newParameter(parameterName);
        Selector s = SelectorFactory.newSelector("");
        ParameterValue pv = SimpleParameterValueFactory.newSimpleParameterValue(value);
        p.setValue(s, pv);
        out.println("Value set");
    }

    public void subscribe(String parameterName, HttpServletResponse response) throws ParameterException, IOException {
        LinkedBlockingQueue<AcquiredParameterValue> queue = new LinkedBlockingQueue<AcquiredParameterValue>();
        Parameter p = ParameterFactory.newInstance().newParameter(parameterName);
        PrintWriter out = response.getWriter();
        //set selector string to "" as the parameter is cycle-independent
        Selector s = SelectorFactory.newSelector("");

        //pass in queue which will be used to pass values to this class when pvl's valueReceived method is called by JAPC
        TestParameterValueListener pvl = new TestParameterValueListener(queue);
        sh = p.createSubscription(s, pvl);
        SubscriptionFilter sf;
        sf = new SubscriptionFilter() {
            @Override
            protected ParameterSelectorSubscriptions createNewPSS(Parameter parameter, Selector selector, ParameterValueListener pvl) throws ParameterException {
                return null;
            }
        };
        //start subscription
        sh.startMonitoring();
        int hostPort = portPool.get(0);
        portPool.remove(0);
        int sendPort = portPool.get(0);
        portPool.remove(0);
        out.println(sendPort);
        UDPSender udpSender = new UDPSender(sendPort, hostPort, "localhost", sh, queue, response);
        Thread udpThread = new Thread(udpSender);
        threadPool.put(udpSender, udpThread);
        udpThread.start();
    }


    public void destroy() {
        sh.stopMonitoring();
    }
    
}








