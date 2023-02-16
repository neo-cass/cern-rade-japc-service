package cern.rade.services.japc.examples;

import cern.japc.core.AcquiredParameterValue;
import cern.japc.core.SubscriptionHandle;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;

public class UDPSender implements Runnable{
    private String host;
    private int sendPort;
    private int hostPort;
    private LinkedBlockingQueue<AcquiredParameterValue> queue;
    private SubscriptionHandle sh;
    private HttpServletResponse response;
    boolean end=false;
    PrintWriter out;
    public UDPSender(int sendPort, int hostPort, String host, SubscriptionHandle sh, LinkedBlockingQueue<AcquiredParameterValue> queue, HttpServletResponse response){
        System.out.println("INIT");
        this.hostPort=hostPort;
        this.sendPort=sendPort;
        this.host=host;
        this.queue=queue;
        this.sh=sh;
        this.response=response;
    }



    @Override
    public void run() {

        try {
            initialiseSend();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }


    }

    private void initialiseSend() throws IOException, InterruptedException {
        InetAddress address = InetAddress.getByName(host);
        DatagramSocket socket = new DatagramSocket(hostPort);
        while (!end) {
            AcquiredParameterValue apv = queue.take();
            byte[] curVal = apv.getValue().getString().getBytes(StandardCharsets.UTF_8);
            DatagramPacket currentValue = new DatagramPacket(curVal, curVal.length, address, sendPort);
            socket.send(currentValue);
        }
        sh.stopMonitoring();
        socket.close();
    }
}
