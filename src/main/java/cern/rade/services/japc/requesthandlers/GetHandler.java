package cern.rade.services.japc.requesthandlers;

import cern.japc.core.AcquiredParameterValue;
import cern.japc.core.Parameter;
import cern.japc.core.ParameterException;
import cern.japc.core.Parameters;
import cern.japc.core.factory.ParameterFactory;
import cern.japc.core.factory.SelectorFactory;
import cern.rade.services.japc.communication.messages.MetaData;
import cern.rade.services.japc.communication.messages.Request;
import cern.rade.services.common.types.JavaContainer;

public class GetHandler {
    public JavaContainer<?> get(Request request) throws ParameterException {
        MetaData metaData = request.getMetaData();
        String name = Parameters.buildParameterName(metaData.device, request.getData().getFieldName());
        Parameter p = ParameterFactory.newInstance().newParameter(name);
        cern.japc.core.Selector s = SelectorFactory.newSelector(metaData.selector);
        AcquiredParameterValue apv = p.getValue(s);
        apv.getValue().castAsSimple().getDouble();
        return null;
    }

    private JavaContainer<?> apvCast(AcquiredParameterValue apv){
        //apv.getValue().castAsMap()
        return null;
    }
}
