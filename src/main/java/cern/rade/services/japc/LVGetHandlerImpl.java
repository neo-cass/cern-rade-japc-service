package cern.rade.services.japc;

import cern.japc.core.ParameterException;
import cern.rade.services.codec.CodecException;
import cern.rade.services.codec.lvtojava.LVDataContainerFactory;
import cern.rade.services.codec.lvtojava.LVTypeDecoder;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.labview.LVTypeDescriptor;
import cern.rade.services.japc.rbac.RBACClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;

public class LVGetHandlerImpl {
    private byte[] type_descriptor;
    private final Logger logger = LoggerFactory.getLogger(LVGetHandlerImpl.class);
    private GetHandlerImpl getHandler;

    public LVGetHandlerImpl(String selectorName, String device, String property, String field) {
        this.getHandler = new GetHandlerImpl(selectorName, device, property, field);
    }

    public LVGetHandlerImpl(String selectorName, String device, String property) {
        this.getHandler = new GetHandlerImpl(selectorName, device, property);
    }

    public LVGetHandlerImpl(String selectorName, String device, String property, String field, RBACClient rbacClient) {
        this.getHandler = new GetHandlerImpl(selectorName, device, property, field, rbacClient);
    }

    public LVGetHandlerImpl(String selectorName, String device, String property, RBACClient rbacClient) {
        this.getHandler = new GetHandlerImpl(selectorName, device, property, rbacClient);
    }

    public LVGetHandlerImpl(String selectorName, String device, byte[] type_descriptor, RBACClient rbacClient) {
        this.getHandler = new GetHandlerImpl(selectorName, device, rbacClient);
        this.type_descriptor = type_descriptor;
    }

    public LVGetHandlerImpl(String selectorName, String device, byte[] type_descriptor) {
        this.getHandler = new GetHandlerImpl(selectorName, );
        this.type_descriptor = type_descriptor;
    }

    public void init() throws ParameterException {

    }

    public void initLV() throws CodecException {
        LVTypeDescriptor lvTypeDescriptor = LVTypeDecoder.decode(ByteBuffer.wrap(type_descriptor));
        DataContainer dataContainer = LVDataContainerFactory.createDataContainer(lvTypeDescriptor);
    }
}
