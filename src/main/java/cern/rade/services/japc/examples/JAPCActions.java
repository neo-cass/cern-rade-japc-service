package cern.rade.services.japc.examples;

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

    public static DataContainer<?> get(Request request) throws ParameterException {
        DataContainer<?> data = request.getData();
        MetaData metaData = request.getMetaData();
        Parameter parameter = buildParameter(metaData.getProperty(), metaData.getDevice(), null);
        Selector selectorObj = SelectorFactory.newSelector(metaData.getSelector());
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

    public static void set(Request request) throws ParameterException {
        DataContainer<?> data = request.getData();
        MetaData metaData = request.getMetaData();
        Parameter parameter = buildParameter(data.getName(), metaData.getDevice(), null);
        Selector selectorObj = SelectorFactory.newSelector(metaData.getSelector());
        ParameterValue pv = JAPCEncoder.encode(data);
        System.out.println(pv);
        set(parameter, selectorObj, pv);
    }

    public static void set(Parameter parameter, Selector selector, ParameterValue parameterValue) {
        try {
            parameter.setValue(selector, parameterValue);

        } catch (ParameterException e) {
            System.out.println("Set operation failed");
            e.printStackTrace();
        }
    }


    public static Parameter buildParameter(String property, String device, String field) throws ParameterException {
        String url;
        if(field==null){url = Parameters.buildParameterName(device, property);}
        else{url = Parameters.buildParameterName(device, property, field);}

        Parameter parameter = null;

        parameter = ParameterFactory.newInstance().newParameter(url);

        return parameter;
    }


}
