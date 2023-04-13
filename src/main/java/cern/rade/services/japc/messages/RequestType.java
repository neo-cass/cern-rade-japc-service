package cern.rade.services.japc.messages;

public enum RequestType {
    LV_GET("LV_GET"),
    LV_SET("LV_SET"),
    LV_SUBSCRIBE("LV_SUBSCRIBE"),
    PY_SUBSCRIBE("PY_SUBSCRIBE");
    private String str;
    RequestType(String str){
        this.str = str;
    }
    public String getStr() {
        return str;
    }
}
