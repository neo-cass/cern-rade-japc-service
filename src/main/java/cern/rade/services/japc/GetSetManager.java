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

public class GetSetManager {


    private String username;
    private String selector;
    private String device;
    private String propertyName;
    private Request request;
    private MetaData metaData;
    private JavaContainer<?> property;
    public GetSetManager(Request request){
        this.metaData=request.getMetaData();
        this.username=metaData.username;
        this.device = metaData.device;
        this.selector = metaData.selector;
        this.property=request.getData();
        this.propertyName=property.getName();
    }

    public JavaContainer<?> get() throws ParameterException {
        String name = Parameters.buildParameterName(device, propertyName);
        Parameter parameter;
        parameter = ParameterFactory.newInstance().newParameter(name);
        Selector selectorObj;
        selectorObj = SelectorFactory.newSelector(selector);

        AcquiredParameterValue apv = parameter.getValue(selectorObj);
        JAPCDecoder.decode((PropertyContainer) property, apv);
        return property;
    }

    public void set() throws ParameterException {
        String name = Parameters.buildParameterName(device, propertyName);
        Parameter parameter;
        parameter = ParameterFactory.newInstance().newParameter(name);
        Selector selectorObj;
        selectorObj = SelectorFactory.newSelector(selector);
        ParameterValue pv = JAPCEncoder.encode(property);
        System.out.println(pv);
        parameter.setValue(selectorObj, pv);
    }
}
