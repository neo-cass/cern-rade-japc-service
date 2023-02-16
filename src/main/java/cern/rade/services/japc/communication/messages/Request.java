package cern.rade.services.japc.communication.messages;

import cern.rade.services.common.types.JavaContainer;

public class Request {
    public MetaData metaData;
    public JavaContainer<?> data;

    public Request(MetaData metaData, JavaContainer<?> data)
    {
        this.metaData = metaData;
        this.data=data;
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

    public MetaData getMetaData() {
        return metaData;
    }

    public JavaContainer<?> getData() {
        return data;
    }
}
