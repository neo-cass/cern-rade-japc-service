package cern.rade.services.japc;

import cern.japc.core.*;
import cern.japc.core.factory.ParameterFactory;


public final class Util {

    public static Parameter buildParameter(String property, String device, String field) throws ParameterException {
        String url;
        if(field==null){url = Parameters.buildParameterName(device, property);}
        else{url = Parameters.buildParameterName(device, property, field);}

        Parameter parameter = null;

        parameter = ParameterFactory.newInstance().newParameter(url);

        return parameter;
    }
    public static String createErrorMessage(Exception e, String message){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ERROR: ").append(message).append("MESSAGE: ").append(e.getMessage()).append("AT: ").append(e.getStackTrace()[0].toString());
        return stringBuilder.toString();
    }

}
