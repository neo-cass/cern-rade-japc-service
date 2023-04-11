package cern.rade.services.japc.messages;

public enum RequestType {
    LV_GET("lv_get"),
    LV_SET("lv_set"),
    LV_SUBSCRIBE("lv_subscribe"),
    PY_SUBSCRIBE("py_subscribe");
    private String str;
    RequestType(String str){
        this.str = str;
    }
    public String getStr() {
        return str;
    }
//TODO: this is messy
    public static RequestType getType(String reqString) {
        if(reqString.equals(RequestType.LV_GET.getStr())){
            return RequestType.LV_GET;
        }
        else if(reqString.equals(RequestType.LV_SET.getStr())){
            return RequestType.LV_SET;
        }
        else if(reqString.equals(RequestType.LV_SUBSCRIBE.getStr())){
            return RequestType.LV_SUBSCRIBE;
        }
        else{
            return RequestType.PY_SUBSCRIBE;
        }
    }

}
