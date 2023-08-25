package cern.rade.services.japc;

import cern.japc.core.*;
import cern.japc.core.factory.ParameterFactory;
import cern.rade.services.common.communication.MetaData;
import cern.rade.services.common.communication.Request;
import cern.rade.services.common.containers.java.DataContainer;


public final class Util {

    public static Parameter buildParameter(Request request) throws ParameterException {
        MetaData metaData = request.getMetaData();
        DataContainer data = request.getData();
        String name;
        if(data != null){
            name = Parameters.buildParameterName(metaData.getDevice(), data.getName());
        }
        else{
            name = metaData.getReq_url();
        }

        Parameter parameter = ParameterFactory.newInstance().newParameter(name);
        return parameter;
    }
    public static String createErrorMessage(Exception e, String message){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ERROR: ").append(message).append("MESSAGE: ").append(e.getMessage()).append("AT: ").append(e.getStackTrace()[0].toString());
        return stringBuilder.toString();
    }

}
