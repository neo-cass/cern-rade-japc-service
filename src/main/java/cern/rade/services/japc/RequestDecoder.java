package cern.rade.services.japc;

import cern.japc.core.Parameter;
import cern.japc.core.ParameterException;
import cern.japc.core.Parameters;
import cern.japc.core.Selector;
import cern.japc.core.factory.ParameterFactory;
import cern.japc.core.factory.SelectorFactory;
import cern.rade.services.codec.lvtojava.LVDataContainerFactory;
import cern.rade.services.codec.lvtojava.LVTypeDecoder;
import cern.rade.services.common.communication.*;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.labview.LVClusterDescriptor;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;
import cern.rade.services.japc.rbac.RBACClientFactory;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.TokenFormatException;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.nio.ByteBuffer;

import static cern.rade.services.common.BytesUtil.shortArrToByteArr;


public final class RequestDecoder {
    public static Client decodeRequest(HttpServletRequest request) throws IOException {
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

    private static void validateMessage(Client client, MetaData metaData){
        //check required fields
        if(metaData.getRequest_language() == null || metaData.getRequest_action() == null || metaData.getSelector() == null){
            client.invalidate("req_lang, req_action or selector not provided.");
        }

        Selector selector = SelectorFactory.newSelector(metaData.getSelector());
        client.setSelector(selector);

        switch(metaData.getAuth_type()){
            case RBAC_EXPLICIT -> {
                if (metaData.getPassword() == null || metaData.getUsername() == null){
                    client.invalidate("password or username not provided.");
                }
            }
            case RBAC_CACHED ->{
                if(metaData.getRbac_id() == -1){
                    client.invalidate("rbac_id not provided.");
                }
            }
            case RBAC_LOCATION -> {
                if(metaData.getRbac_token() == null){
                    client.invalidate("rbac_token not provided.");
                }
            }
            case NONE -> {
            }
        }

        if(metaData.getAuth_type()!=AuthenticationType.NONE){
            try {
                client.setRbacClient(new RBACClientFactory(metaData).createClient());
            } catch (TokenFormatException | AuthenticationException e) {
                client.invalidate(Util.createErrorMessage(e, "RBAC authentication failed."));
            }
        }
    }

    private static void validateLVMessage(Client client, MetaData metaData){
        validateMessage(client, metaData);

        //no url can be generated with this info
        if(metaData.getReq_url() == null && (metaData.getType_descriptor() == null || metaData.getDevice() == null)){
            client.invalidate("URL or {type_descriptor, device} missing.");
            return;
        }

        //ambiguous which url they want us to generate, too much meta data provided
        if(metaData.getReq_url() != null && (metaData.getType_descriptor() != null || metaData.getDevice() != null)){
            client.invalidate("URL and {type_Descriptor, device} provided; ambiguous, remove one.");
            return;
        }

        if(metaData.getType_descriptor()==null){
            validateURL(client, metaData);
        }
        else{
            validateURLAndDecodeLV(client, metaData);
        }
    }

    private static void validatePYMessage(Client client, MetaData metaData){
        if(metaData.getReq_url()==null){
            client.invalidate("URL not provided");
            return;
        }
        validateURL(client, metaData);
    }

    private static void validateURL(Client client, MetaData metaData){
        try {
            Parameter parameter = ParameterFactory.newInstance().newParameter(metaData.getReq_url());
            client.setParameter(parameter);

        } catch (ParameterException e) {
            client.invalidate(Util.createErrorMessage(e, "CCDB lookup failed, check URL."));
        }
    }

    private static void validateURLAndDecodeLV(Client client, MetaData metaData){
        byte[] type_descriptor = shortArrToByteArr(metaData.getType_descriptor());
        //TODO what if decoding fails? add exceptions to type decoder
        LVTypeDescriptor lvTypeDescriptor = LVTypeDecoder.decode(ByteBuffer.wrap(type_descriptor));
        client.setDataContainer(LVDataContainerFactory.createPropertyContainer((LVClusterDescriptor) lvTypeDescriptor));
        String parameterName = Parameters.buildParameterName(metaData.getDevice(), lvTypeDescriptor.getName());
        try {
            Parameter parameter = ParameterFactory.newInstance().newParameter(parameterName);
            client.setParameter(parameter);
        }
        catch (ParameterException e) {
            client.invalidate(Util.createErrorMessage(e, "CCDB lookup failed, check device name and check that your cluster is formatted correctly."));
        }
    }

    private static Request decodeLVRequest(HttpServletRequest request, MetaData metaData) throws IOException {
        ServletInputStream reader = request.getInputStream();
        DataContainer decodedData = null;
        byte[] data = reader.readAllBytes();
        short[] type_descriptor = metaData.getType_descriptor();
        decodedData = decodeLVData(data, type_descriptor);
        System.out.println(decodedData);
        return new Request(metaData, decodedData);
    }

    private static DataContainer decodeLVData(byte[] data, short[] type_descriptor){
        return LVDataContainerFactory.createDataContainer(LVTypeDecoder.decode(ByteBuffer.wrap(shortArrToByteArr(type_descriptor))));
    }

    private static Request decodePYRequest(HttpServletRequest request, MetaData metaData) {
        return new Request(metaData, null);
    }
}
