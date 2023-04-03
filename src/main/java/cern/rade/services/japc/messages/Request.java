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
        this.requestType=setReqType(requestType);
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
        stringBuilder.append(data.getFieldName()).append("/");
        if(metaData.selector.equals("")){
            stringBuilder.append("NO_SELECTOR");
        }
        else{
            stringBuilder.append(metaData.selector);
        }

        return stringBuilder.toString().hashCode();
    }
    public MetaData getMetaData() {
        return metaData;
    }

    public JavaContainer<?> getData() {
        return data;
    }


    private RequestType setReqType(String reqString){
        if(reqString.equals(RequestType.LV_GET.getStr())){
            return RequestType.LV_SET;
        }
        else if(reqString.equals(RequestType.LV_SET.getStr())){
            return RequestType.LV_SET;
        }
        else{
            return RequestType.LV_SUBSCRIBE;
        }
    }

    public int getId() {
        return id;
    }

    public RequestType getRequestType() {
        return requestType;
    }
}
