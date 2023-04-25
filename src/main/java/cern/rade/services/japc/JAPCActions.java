package cern.rade.services.japc;

import cern.japc.core.*;
import cern.japc.core.factory.MapParameterValueFactory;
import cern.japc.core.factory.ParameterFactory;
import cern.japc.core.factory.SelectorFactory;
import cern.japc.value.MapParameterValue;
import cern.japc.value.ParameterValue;
import cern.rade.services.codec.japcjava.JAPCDecoder;
import cern.rade.services.codec.japcjava.JAPCEncoder;
import cern.rade.services.common.types.JavaContainer;
import cern.rade.services.common.types.PropertyContainer;
import cern.rade.services.japc.messages.MetaData;
import cern.rade.services.japc.messages.Request;
import zmq.socket.reqrep.Req;

public final class JAPCActions {

    public static JavaContainer<?> get(Request request) {
        JavaContainer<?> data = request.getData();
        MetaData metaData = request.getMetaData();
        Parameter parameter = buildParameter(metaData.property, metaData.device, null);
        Selector selectorObj = SelectorFactory.newSelector(metaData.selector);
        AcquiredParameterValue apv = get(parameter,selectorObj);

        if(data==null){JAPCDecoder.decode(apv);}
        else{JAPCDecoder.decode((PropertyContainer) data, apv);}
        return data;
    }

    public static AcquiredParameterValue get(Parameter parameter, Selector selector) {
        AcquiredParameterValue apv = null;
        try {
            apv = parameter.getValue(selector);
        } catch (ParameterException e) {
            System.out.println("Get operation failed");
        }
        return apv;
    }

    public static void set(Request request) {
        JavaContainer<?> data = request.getData();
        MetaData metaData = request.getMetaData();
        Parameter parameter = buildParameter(data.getName(), metaData.device, null);
        Selector selectorObj = SelectorFactory.newSelector(metaData.selector);
        ParameterValue pv = JAPCEncoder.encode(data);
        System.out.println(pv);
        set(parameter, selectorObj, pv);
    }

    public static void set(Parameter parameter, Selector selector, ParameterValue parameterValue) {
        try {
            parameter.setValue(selector, parameterValue);
        } catch (ParameterException e) {
            System.out.println("Set operation failed");
        }
    }


    public static Parameter buildParameter(String property, String device, String field) {
        String url;
        if(field==null){url = Parameters.buildParameterName(device, property);;}
        else{url = Parameters.buildParameterName(device, property, field);}

        Parameter parameter = null;
        try {
            parameter = ParameterFactory.newInstance().newParameter(url);
        } catch (ParameterException e) {
            System.out.println("Failed to create parameter");
        }
        return parameter;
    }


}
