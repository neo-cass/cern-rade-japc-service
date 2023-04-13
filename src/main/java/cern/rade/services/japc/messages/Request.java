package cern.rade.services.japc.messages;

import cern.rade.services.common.types.JavaContainer;

public class Request {
    private MetaData metaData;
    private JavaContainer<?> data;
    private RequestType requestType;
    private int id;

    public Request(MetaData metaData, JavaContainer<?> data, String requestType)
    {
        this.metaData = metaData;
        this.data=data;
        this.requestType=RequestType.valueOf(requestType);
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
    private int createID(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(metaData.username).append("/");
        stringBuilder.append(metaData.device).append("/");
        stringBuilder.append(metaData.property).append("/");
        stringBuilder.append(metaData.selector);
        return stringBuilder.toString().hashCode();
    }
    public MetaData getMetaData() {
        return metaData;
    }

    public JavaContainer<?> getData() {
        return data;
    }

    public int getId() {
        return id;
    }

    public RequestType getRequestType() {
        return requestType;
    }
}
