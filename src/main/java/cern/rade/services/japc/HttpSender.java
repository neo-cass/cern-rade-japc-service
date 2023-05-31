package cern.rade.services.japc;

import cern.rade.services.common.communication.MetaData;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class HttpSender {
    private HttpServletResponse response;
    private OutputStreamWriter writer;
    private OutputStream out;
    public HttpSender(HttpServletResponse response) throws IOException {
        this.response = response;
        this.out=response.getOutputStream();
        this.writer = new OutputStreamWriter(out);
    }

    public void send(String message) {
        try {
            writer.write(message);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(String message, MetaData metaData) {
        response.addHeader("MetaData", metaData.flatten());
        send(message);
    }

    public void send(byte[] data, MetaData metaData)  {
        response.setHeader("MetaData", metaData.flatten());
        try {
            out.write(data);
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
