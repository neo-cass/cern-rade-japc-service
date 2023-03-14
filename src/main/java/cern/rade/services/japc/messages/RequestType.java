package cern.rade.services.japc.messages;

public enum RequestType {
    LV_GET("lv_get"),
    LV_SET("lv_set"),
    LV_SUBSCRIBE("lv_subscribe");
    private String str;
    RequestType(String str){
        this.str = str;
    }
    public String getStr() {
        return str;
    }
}
