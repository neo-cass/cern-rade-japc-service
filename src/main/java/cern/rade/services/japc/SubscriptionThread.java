package cern.rade.services.japc;

import cern.japc.core.ParameterException;
import cern.japc.core.SubscriptionHandle;
import jakarta.websocket.RemoteEndpoint;

import java.io.IOException;
import java.io.Writer;


public class SubscriptionThread extends Thread{
    private SubscriptionHandle sh;
    private ValueCallback vc;
    private RemoteEndpoint.Basic writer;

    public SubscriptionThread(SubscriptionHandle sh, ValueCallback vc, RemoteEndpoint.Basic writer){
        this.sh = sh;
        this.vc=vc;
        this.writer=writer;
    }
    @Override
    public void run() {
        try {
            init();
        } catch (ParameterException e) {
            System.out.println("An error occurred starting the subscription. Here is the stack trace: \n");
            e.printStackTrace();
        }
        while(!isInterrupted()){
            try {
                writer.sendText(String.valueOf(vc.getLatest().getValue().castAsMap().getDouble("ts0")));
            } catch (InterruptedException e) {
                break;
            }
            catch(IOException e){
                System.out.println("An error occurred sending the latest value from the subscription. Here is the stack trace:\n");
                e.printStackTrace();
                break;
            }
        }
        close();
    }

    private void close() {
        sh.stopMonitoring();
    }

    private void init() throws ParameterException {
        sh.startMonitoring();
    }
}
