package cern.rade.services.japc.core;

import cern.rade.services.japc.communication.messages.Request;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class RequestHandler {
    private static ConcurrentHashMap<String, Request> subRequests;
    private static LinkedBlockingQueue<Request> getRequests;
    private static LinkedBlockingQueue<Request> setRequests;

    public static LinkedBlockingQueue<Request> getGetRequests() {
        if(getRequests==null){getRequests=new LinkedBlockingQueue<>();}
        return getRequests;
    }

    public static LinkedBlockingQueue<Request> getSetRequests() {
        if(setRequests==null){setRequests=new LinkedBlockingQueue<>();}
        return setRequests;
    }

    public static ConcurrentHashMap<String, Request> getSubRequests() {
        if(subRequests==null){subRequests=new ConcurrentHashMap<>();}
        return subRequests;
    }
}
