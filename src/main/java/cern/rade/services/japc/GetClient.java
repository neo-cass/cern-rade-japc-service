package cern.rade.services.japc;

import cern.japc.core.AcquiredParameterValue;
import cern.japc.core.Parameter;
import cern.japc.core.ParameterException;
import cern.japc.core.Selector;
import cern.japc.core.factory.SelectorFactory;
import cern.rade.services.codec.japcjava.JAPCDecoder;
import cern.rade.services.codec.javatolv.LVDataEncoder;
import cern.rade.services.codec.javatolv.LVTypeEncoder;
import cern.rade.services.common.communication.client.MetaData;
import cern.rade.services.common.communication.client.Request;
import cern.rade.services.common.communication.client.RequestAction;
import cern.rade.services.common.containers.java.DataContainer;
import cern.rade.services.common.containers.java.PropertyContainer;
import cern.rade.services.japc.examples.JAPCActions;
import cern.rade.services.japc.rbac.RBACClient;
import cern.rbac.common.RbaToken;
import static cern.rade.services.common.BytesUtil.byteArrToShortArr;
import cern.rbac.util.holder.MiddleTierRbaTokenHolder;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;

public class GetClient {
    private HttpServletResponse response;
    private Request request;
    private DataContainer data;
    private MetaData metaData;
    private Parameter parameter;
    private Selector selector;
    private AcquiredParameterValue apv;
    private RBACClient rbacClient;

    public GetClient(HttpServletResponse response, Request request){
        this.request = request;
        this.response = response;
    }
    private void init() throws ParameterException {
        this.metaData = request.getMetaData();
        if(request.getRequestAction() == RequestAction.GET_SECURE){
            this.rbacClient = new RBACClient(metaData.getUsername(), metaData.getPassword());
        }
        this.data = request.getData();
        this.parameter = JAPCActions.buildParameter(metaData.getProperty(), metaData.getDevice(), null);
        this.selector = SelectorFactory.newSelector(metaData.getSelector());
    }

    public void get() throws ParameterException {
        switch(request.getRequestAction()){
            case GET -> japcGET();
            case GET_SECURE -> japcGET_SECURE();
        }
    }

    private void japcGET() throws ParameterException {
        apv = parameter.getValue(selector);
        if(data==null){data = JAPCDecoder.decode(apv);}
        else{JAPCDecoder.decode((PropertyContainer) data, apv);}
    }

    private void japcGET_SECURE() throws ParameterException {
        RbaToken token = rbacClient.login();
        MiddleTierRbaTokenHolder.set(token);
        japcGET();
        MiddleTierRbaTokenHolder.clear();
    }

    private void send() throws IOException {
        switch(request.getRequestLanguage()){
            case LABVIEW -> sendLV();
            case PYTHON -> sendPY();
        }
    }

    private void sendLV() throws IOException {
        response.addHeader("acq_stamp", String.valueOf(apv.getHeader().getAcqStamp()));
        response.addHeader("type_descriptor", Arrays.toString(byteArrToShortArr(LVTypeEncoder.encodeTypeDescriptor(data))));
        OutputStream out = response.getOutputStream();
        out.write(LVDataEncoder.encode(data));
        out.flush();
        out.close();
    }

    private void sendPY() throws IOException {
        response.addHeader("acq_stamp", String.valueOf(apv.getHeader().getAcqStamp()));
        response.addHeader("type_descriptor", Arrays.toString(LVTypeEncoder.encodeTypeDescriptor(data)));
        PrintWriter out = response.getWriter();
        out.write(data.toString());
        out.flush();
        out.close();
    }

    public void execute(){
        try {
            init();
        } catch (ParameterException e) {
            throw new RuntimeException(e);
        }

        try {
            get();
        } catch (ParameterException e) {
            throw new RuntimeException(e);
        }

        try {
            send();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
