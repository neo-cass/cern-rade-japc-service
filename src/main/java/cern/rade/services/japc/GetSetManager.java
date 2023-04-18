package cern.rade.services.japc;

import cern.japc.core.*;
import cern.japc.core.factory.MapParameterValueFactory;
import cern.japc.core.factory.ParameterFactory;
import cern.japc.core.factory.SelectorFactory;
import cern.japc.value.MapParameterValue;
import cern.japc.value.ParameterValue;
import cern.rade.services.common.types.JavaContainer;
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
        this.propertyName=property.getFieldName();
    }
    public String get() throws ParameterException {
        String name = Parameters.buildParameterName(device, propertyName);
        Parameter parameter;
        parameter = ParameterFactory.newInstance().newParameter(name);
        Selector selectorObj;
        selectorObj = SelectorFactory.newSelector(selector);
        AcquiredParameterValue apv = parameter.getValue(selectorObj);

        return apv.getValue().castAsMap().getString();
    }

    public void set() throws ParameterException {
        String name = Parameters.buildParameterName(device, propertyName);
        Parameter parameter;
        parameter = ParameterFactory.newInstance().newParameter(name);
        Selector selectorObj;
        selectorObj = SelectorFactory.newSelector(selector);
        MapParameterValue map = MapParameterValueFactory.newMapParameterValue();
        map.setDouble("dbl0", 1);
        map.setDouble("dbl1", 3);
        parameter.setValue(selectorObj, map);


    }
}
