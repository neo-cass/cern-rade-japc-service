package communication.messages;
public class MetaData {
    public String username;
    public String password;
    public RequestType reqType;
    public String selector;
    public String device;
    public MetaData(String username,
                    String password,
                    String reqType,
                    String selector,
                    String device)
    {
        this.username=username;
        this.password=password;
        this.reqType=setReqType(reqType);
        this.selector=selector;
        this.device=device;
    }

    protected RequestType setReqType(String reqString){
        if(reqString.equals(RequestType.GET.getStr())){
            return RequestType.GET;
        }
        else if(reqString.equals(RequestType.SET.getStr())){
            return RequestType.SET;
        }
        else{
            return RequestType.SUBSCRIBE;
        }
    }


    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("username: ");
        stringBuilder.append(username);
        stringBuilder.append("\n");

        stringBuilder.append("request type: ");
        stringBuilder.append(reqType);
        stringBuilder.append("\n");

        stringBuilder.append("selector: ");
        stringBuilder.append(selector);
        stringBuilder.append("\n");

        stringBuilder.append("device: ");
        stringBuilder.append(device);
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }
}
