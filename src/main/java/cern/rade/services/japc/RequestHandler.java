package cern.rade.services.japc;

import cern.japc.core.Parameter;
import cern.japc.core.ParameterException;
import cern.japc.core.Parameters;
import cern.japc.core.factory.ParameterFactory;
import cern.japc.core.factory.SelectorFactory;
import cern.rade.services.codec.CodecException;
import cern.rade.services.codec.lvtojava.LVDataContainerFactory;
import cern.rade.services.codec.lvtojava.LVTypeDecoder;
import cern.rade.services.common.communication.*;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;
import cern.rade.services.japc.rbac.RBACClientFactory;
import cern.rbac.client.authentication.AuthenticationException;
import cern.rbac.common.TokenFormatException;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

import static cern.rade.services.common.BytesUtil.shortArrToByteArr;


public final class RequestHandler {
    static Logger logger = LoggerFactory.getLogger(RequestHandler.class);
    public static Client decodeRequest(HttpServletRequest request) throws IOException {
        Client client = new Client();
        client.setRawData(request.getInputStream().readAllBytes());
        String jsonMsg = request.getHeader("MetaData");
        if(jsonMsg==null){
            client.invalidate("Missing MetaData header in HTTP POST request.");
            logger.info("Received message from: {} but they provided no MetaData header, rejecting.", request.getRemoteHost());
            return client;
        }

        MetaData metaData = new MetaData(JsonParser.parseString(jsonMsg).getAsJsonObject());
        logger.info("Received request from: {} with MetaData: {}.", request.getRemoteHost(), metaData);

        if(metaData.getRequest_language() == null){
            client.invalidate("req_lang not provided.");
            return client;
        }

        client.setRequestLanguage(metaData.getRequest_language());

        switch(metaData.getRequest_language()){
            case LABVIEW -> {validateLVMessage(client, metaData);}
            case PYTHON -> {validatePYMessage(client, metaData);}
        }
        return client;
    }

    private static void validateMessage(Client client, MetaData metaData){
        //check required fields
        if(metaData.getRequest_action() == null){
            client.invalidate("selector not provided.");
        }
        else if(metaData.getSelector() == null) {
            client.invalidate("req_action not provided.");
        }

        client.setAction(metaData.getRequest_action());
        client.setSelector(SelectorFactory.newSelector(metaData.getSelector()));
        client.setAuthenticationType(metaData.getAuth_type());

        if(metaData.getAuth_type()!=AuthenticationType.NONE){
            validateRBACAuthentication(client, metaData);
        }
    }

    private static void validateRBACAuthentication(Client client, MetaData metaData){
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
        }

        try {
            client.setRbacClient(new RBACClientFactory(metaData).createClient());
        } catch (TokenFormatException | AuthenticationException e) {
            client.invalidate(ErrorUtil.createErrorMessage(e, "RBAC authentication failed."));
        }
    }

    private static void validateLVMessage(Client client, MetaData metaData){
        validateMessage(client, metaData);
        client.setType_descriptor(metaData.getType_descriptor());
        //no url can be generated with this info
        if(metaData.getReq_url() == null && (client.getType_descriptor() == null || metaData.getDevice() == null)){
            client.invalidate("URL or {type_descriptor, device} missing.");
            return;
        }

        //ambiguous which url they want us to generate, too much meta data provided
        if(metaData.getReq_url() != null && (client.getType_descriptor() != null || metaData.getDevice() != null)){
            client.invalidate("URL and {type_Descriptor, device} provided; ambiguous, remove one.");
            return;
        }

        if(client.getType_descriptor()==null){
            validateURL(client, metaData);
        }
        else{
            validateLVURL(client, metaData);
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
            client.invalidate(ErrorUtil.createErrorMessage(e, "CCDB lookup failed, check URL."));
        }
    }

    private static void validateLVURL(Client client, MetaData metaData){
        byte[] type_descriptor = shortArrToByteArr(client.getType_descriptor());
        //TODO what if decoding fails? add exceptions to type decoder

        LVTypeDescriptor lvTypeDescriptor;
        try {
            lvTypeDescriptor = LVTypeDecoder.decode(ByteBuffer.wrap(type_descriptor));
        } catch (CodecException e) {
            client.invalidate(ErrorUtil.createErrorMessage(e,"Failed to decode provided LabVIEW type descriptor."));
            return;
        }
        client.setLvTypeDescriptor(lvTypeDescriptor);
        try {
            client.setDataContainer(LVDataContainerFactory.createDataContainer(lvTypeDescriptor));
        } catch (CodecException e) {
            client.invalidate(ErrorUtil.createErrorMessage(e,"Failed to create data container from provided LabVIEW type descriptor."));
            return;
        }
        String parameterName = Parameters.buildParameterName(metaData.getDevice(), lvTypeDescriptor.getName());
        try {
            Parameter parameter = ParameterFactory.newInstance().newParameter(parameterName);
            client.setParameter(parameter);
        }
        catch (ParameterException e) {
            client.invalidate(ErrorUtil.createErrorMessage(e, "CCDB lookup failed, check device name and check that your cluster is formatted correctly."));
            return;
        }
    }
}
