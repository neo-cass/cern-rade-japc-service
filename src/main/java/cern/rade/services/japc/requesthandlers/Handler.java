package cern.rade.services.japc.requesthandlers;

import cern.rade.services.japc.communication.messages.Request;

public class Handler {
    private Request request;

    public Handler(Request request){
        this.request = request;
    }
}
