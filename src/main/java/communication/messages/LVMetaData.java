package communication.messages;

import cern.rade.services.common.types.labview.LVRawTypeDescriptor;

import java.nio.ByteBuffer;

public class LVMetaData extends MetaData {
    public ByteBuffer typeBuf;
    public LVMetaData(String username,
                      String password,
                      String reqType,
                      String selector,
                      String device)
    {
        super(username, password, reqType, selector, device);
    }

    public void setTypeBuf(ByteBuffer typeBuf){
        this.typeBuf = typeBuf;
    }

    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(super.toString());
        stringBuilder.append("type string: ");
        stringBuilder.append(LVRawTypeDescriptor.wrap(typeBuf.array()));
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }
    public Request toRequest() {
        return null; //new Request(username, password, LVDecoder.decode(typeString, data), selector, device);
    }
}
