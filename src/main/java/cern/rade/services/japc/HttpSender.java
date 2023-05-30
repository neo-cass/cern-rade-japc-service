package cern.rade.services.japc;

import cern.rade.services.common.communication.MetaData;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class HttpSender {
    private HttpServletResponse response;
    private PrintWriter writer;
    private OutputStream out;
    public HttpSender(HttpServletResponse response) throws IOException {
        this.response = response;
        this.writer = response.getWriter();
        this.out=response.getOutputStream();
    }

    public void send(String message){
        writer.println(message);
    }

    public void send(String message, MetaData metaData){
        response.addHeader("MetaData", metaData.flatten());
        send(message);
    }

    public void send(byte[] data, MetaData metaData) throws IOException {
        response.addHeader("MetaData", metaData.flatten());
        out.write(data);
        out.flush();
        out.close();
    }
}
