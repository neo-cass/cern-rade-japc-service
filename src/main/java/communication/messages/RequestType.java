package communication.messages;

public enum RequestType {
    GET("get"),
    SET("set"),
    SUBSCRIBE("subscribe");
    private String str;
    RequestType(String str){
        this.str = str;
    }
    public String getStr() {
        return str;
    }
}
