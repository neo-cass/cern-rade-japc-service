package cern.rade.services.japc.messages;
public class MetaData {
    public String username;
    public String password;
    public String request_type;
    public String selector;
    public String device;
    public String property;
    public short[] type_descriptor;
    public MetaData(String username,
                    String password,
                    String request_type,
                    String selector,
                    String device,
                    String property,
                    short[] type_descriptor)
    {
        this.username=username;
        this.password=password;
        this.request_type = request_type;
        this.selector=selector;
        this.device=device;
        this.property=property;
        this.type_descriptor = type_descriptor;
    }



    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("username: ");
        stringBuilder.append(username);
        stringBuilder.append("\n");

        stringBuilder.append("request type: ");
        stringBuilder.append(request_type);
        stringBuilder.append("\n");

        stringBuilder.append("selector: ");
        stringBuilder.append(selector);
        stringBuilder.append("\n");

        stringBuilder.append("device: ");
        stringBuilder.append(device);
        stringBuilder.append("\n");

        stringBuilder.append("property: ");
        stringBuilder.append(property);
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }
}
