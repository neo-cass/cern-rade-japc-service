package cern.rade.services.japc;

import cern.rade.services.common.communication.MetaData;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class HttpSender {
    private HttpServletResponse response;
    private OutputStreamWriter writer;
    private OutputStream out;
    private Logger logger = LoggerFactory.getLogger(HttpSender.class);
    public HttpSender(HttpServletResponse response) throws IOException {
        this.response = response;
        this.out=response.getOutputStream();
        this.writer = new OutputStreamWriter(out);
    }

    public boolean send(String message) {
        try {
            writer.write(message);
            writer.flush();
            writer.close();
            return true;
        } catch (IOException e) {
            logger.error("Exception sending data with OutputStreamWriter.", e);
            return false;
        }
    }

    public boolean send(String message, MetaData metaData) {
        response.addHeader("MetaData", metaData.flatten());
        return send(message);
    }

    public boolean send(byte[] data, MetaData metaData)  {
        response.setHeader("MetaData", metaData.flatten());
        try {
            out.write(data);
            out.flush();
            out.close();
            return true;
        } catch (IOException e) {
            logger.error("Exception sending data with OutputStream.", e);
            return false;
        }
    }
}
