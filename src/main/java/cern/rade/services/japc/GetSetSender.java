package cern.rade.services.japc;

import cern.japc.core.AcquiredParameterValue;
import cern.rade.services.common.communication.MetaData;
import cern.rade.services.common.communication.Request;
import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializer;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.websocket.RemoteEndpoint;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

public class GetSetSender {
    private HttpServletResponse response;
    private PrintWriter writer;
    private OutputStream out;
    public GetSetSender (HttpServletResponse response) throws IOException {
        this.response = response;
        this.writer = response.getWriter();
        this.out=response.getOutputStream();
    }

    public void send(String message){
        writer.println(message);
    }

    public void send(byte[] data, MetaData metaData) throws IOException {
        response.addHeader("MetaData", metaData.flatten());
        out.write(data);
        out.flush();
        out.close();
    }
}
