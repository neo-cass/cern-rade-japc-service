package cern.rade.services.japc;

import cern.rade.services.codec.lvtojava.LVDecoder;
import cern.rade.services.common.communication.*;
import cern.rade.services.common.containers.java.DataContainer;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.nio.ByteBuffer;

import static cern.rade.services.common.BytesUtil.shortArrToByteArr;


public final class RequestDecoder {
    public static Request decodeMessage(HttpServletRequest request) throws IOException {
        String jsonMsg = request.getHeader("MetaData");
        if(jsonMsg==null){
            throw new IOException("Missing header \"MetaData\"");
        }

        MetaData metaData = new MetaData(JsonParser.parseString(jsonMsg).getAsJsonObject());

        switch(metaData.getRequest_language()){
            case LABVIEW -> {return decodeLVRequest(request, metaData);}
            case PYTHON -> {return decodePYRequest(request, metaData);}
            default -> throw new RuntimeException();
        }
    }

    private static boolean checkLVMessage(MetaData metaData){

        if(metaData.getRequest_language() == null || metaData.getRequest_action() == null || metaData.getSelector() == null){
            return false;
        }

        if(metaData.getAuth_type() != null){
            switch(metaData.getAuth_type()){
                case RBAC_EXPLICIT -> {
                    if (metaData.getPassword() == null || metaData.getUsername() == null){
                        return false;
                    }
                }
                case RBAC_CACHED ->{
                    if(metaData.getRbac_id() == -1){
                        return false;
                    }
                }
                case RBAC_LOCATION -> {
                    if(metaData.getRbac_token() == null){
                        return false;
                    }
                }
            }
        }

        if(metaData.getReq_url() == null && metaData.getType_descriptor() == null){
            return false;
        }

        if((metaData.getType_descriptor() != null && metaData.getDevice() == null) || (metaData.getType_descriptor()==null && metaData.getDevice()!=null)){
            return false;
        }

        if(metaData.getType_descriptor() != null && metaData.getDevice() != null && metaData.getReq_url() != null){
            return false;
        }

        return true;
    }

    private static Request decodeLVRequest(HttpServletRequest request, MetaData metaData) throws IOException {
        ServletInputStream reader = request.getInputStream();
        DataContainer decodedData = null;
        byte[] data = reader.readAllBytes();
        short[] type_descriptor = metaData.getType_descriptor();
        if(data.length > 0  && type_descriptor.length > 0){
            decodedData = decodeLVData(data, type_descriptor);
        }
        return new Request(metaData, decodedData);
    }

    private static DataContainer decodeLVData(byte[] data, short[] type_descriptor){
        ByteBuffer dataBuf = ByteBuffer.wrap(data);
        ByteBuffer typeBuf = ByteBuffer.wrap(shortArrToByteArr(type_descriptor));
        return LVDecoder.decode(typeBuf, dataBuf);
    }

    private static Request decodePYRequest(HttpServletRequest request, MetaData metaData)  {
        return new Request(metaData, null);
    }
}
