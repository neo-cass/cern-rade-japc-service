package cern.rade.services.japc.examples;

import cern.japc.core.AcquiredParameterValue;
import cern.japc.core.SubscriptionHandle;
import cern.rade.services.japc.handlers.ValueCallback;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SubscriptionThread implements Runnable{
    Socket client;
    SubscriptionHandle sh;
    ValueCallback vc;
    public SubscriptionThread(Socket client, SubscriptionHandle sh, ValueCallback vc){
      this.sh=sh;
      this.client=client;
      this.vc=vc;
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
            double val;
             val = apv.getValue().castAsMap().getDouble("ts0");
            dos.writeDouble(val);
        }
    }
}
