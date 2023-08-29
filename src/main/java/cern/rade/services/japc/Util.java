package cern.rade.services.japc;

import cern.japc.core.*;
import cern.japc.core.factory.ParameterFactory;
import cern.rade.services.common.communication.MetaData;
import cern.rade.services.common.communication.Request;
import cern.rade.services.common.containers.java.DataContainer;


public final class Util {
    public static String createErrorMessage(Exception e, String message){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(message).append(" : ").append(e.getMessage()).append(" : ").append(e.getStackTrace()[0].toString());
        return stringBuilder.toString();
    }

}
