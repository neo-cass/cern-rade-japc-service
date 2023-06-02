package cern.rade.services.japc;

import cern.japc.core.*;
import cern.japc.core.factory.ParameterFactory;
import cern.japc.core.factory.SelectorFactory;
import cern.japc.value.ParameterValue;
import cern.rade.services.codec.japcjava.JAPCDecoder;
import cern.rade.services.codec.japcjava.JAPCEncoder;

import cern.rade.services.common.communication.MetaData;
import cern.rade.services.common.communication.Request;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.java.PropertyContainer;



public final class JAPCActions {

    public static Parameter buildParameter(String property, String device, String field) throws ParameterException {
        String url;
        if(field==null){url = Parameters.buildParameterName(device, property);}
        else{url = Parameters.buildParameterName(device, property, field);}

        Parameter parameter = null;

        parameter = ParameterFactory.newInstance().newParameter(url);

        return parameter;
    }


}
