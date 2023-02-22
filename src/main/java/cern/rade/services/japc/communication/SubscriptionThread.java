package cern.rade.services.japc.communication;

import cern.japc.core.AcquiredParameterValue;
import cern.japc.core.SubscriptionHandle;
import cern.rade.services.japc.requesthandlers.ValueCallback;
import jakarta.servlet.http.HttpServletResponse;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;

public class SubscriptionThread implements Runnable{
    Socket client;
    SubscriptionHandle sh;
    ValueCallback vc;
    public SubscriptionThread(Socket client, SubscriptionHandle sh, ValueCallback vc){
      this.sh=sh;
      this.client=client;
      this.vc=vc;
    }

    private int coinFlip(){
        if (Math.random() < 0.5){
            return 1;
        }else{
            return 0;
        }
    }


    @Override
    public void run() {
        try {
            pushData();
        } catch (IOException e) {
            System.out.println("Could not write data to stream");
        }
    }

    private void pushData() throws IOException {
        DataOutputStream dos = new DataOutputStream(client.getOutputStream());
        while(true){
            AcquiredParameterValue apv = null;
            try {
                apv = vc.valueQueue().take();
            } catch (InterruptedException e) {
                System.out.println("Could not get data for subscription");
                return;
            }
            int flip = coinFlip();
            double val;
            if(flip == 1){ val = apv.getValue().castAsMap().getDouble("ts0");}
            else{ val = apv.getValue().castAsMap().getDouble("ts1");}
            dos.writeDouble(val);
        }
    }
}
