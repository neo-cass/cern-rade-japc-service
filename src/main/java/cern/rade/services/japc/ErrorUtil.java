package cern.rade.services.japc;

import cern.rade.services.common.communication.MetaData;
import org.slf4j.Logger;


public final class ErrorUtil {
    public static String createErrorMessage(Exception e, String message){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(message).append(" : ").append(e.getMessage()).append(" : ").append(e.getStackTrace()[0].toString());
        return stringBuilder.toString();
    }

    public static void sendErrorResponse(HttpSender sender, Logger logger, MetaData metaData, String message, Exception e){
        metaData.setError(createErrorMessage(e, message));
        logger.error(message, e);
        sender.send("FAILURE", metaData);
    }

}
