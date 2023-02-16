package communication;

import cern.rade.services.codec.javatolv.LVDataEncoder;
import cern.rade.services.codec.javatolv.LVTypeEncoder;
import cern.rade.services.codec.lvtojava.LVDecoder;
import com.google.gson.Gson;
import communication.messages.LVMetaData;
import communication.messages.MetaData;
import communication.messages.Request;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import cern.rade.services.common.types.JavaContainer;
import cern.rade.services.common.types.labview.LVRawTypeDescriptor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;

public class LVEntryPoint extends HttpServlet {
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Request req = decodeMessage(request);
        JavaContainer<?> jc = req.getData();
        byte[] typeStringBytes = LVTypeEncoder.encodeTypeDescriptor(jc);
        System.out.println(LVRawTypeDescriptor.wrap(typeStringBytes));
        byte[] dataBytes = LVDataEncoder.serialise(jc);
        ServletOutputStream out = response.getOutputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
        outputStream.write( typeStringBytes );
        outputStream.write(".....".getBytes());
        outputStream.write( dataBytes );
        outputStream.writeTo(out);
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
}
