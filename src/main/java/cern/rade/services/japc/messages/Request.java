package cern.rade.services.japc.messages;

import cern.rade.services.common.types.JavaContainer;

public class Request {
    public MetaData metaData;
    public JavaContainer<?> data;
    public RequestType requestType;
    public String id;

    public Request(MetaData metaData, JavaContainer<?> data, RequestType requestType)
    {
        this.metaData = metaData;
        this.data=data;
        this.requestType=requestType;

        this.id =createID();
    }
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("meta data: ");
        stringBuilder.append(metaData);
        stringBuilder.append("\n");

        stringBuilder.append("data: ");
        stringBuilder.append(data);
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }
    private String createID(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(metaData.username).append("_");
        stringBuilder.append(metaData.device).append("_");
        stringBuilder.append(data.getFieldName()).append("_");
        if(!metaData.selector.equals("")){
            stringBuilder.append(metaData.selector);
        }
        else{
            stringBuilder.append("NO_SELECTOR");
        }
        return stringBuilder.toString();
    }
    public MetaData getMetaData() {
        return metaData;
    }

    public JavaContainer<?> getData() {
        return data;
    }
}
