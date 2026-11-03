package cern.rade.services.japc.get;

import cern.rade.services.japc.rbac.RbacHeaders;
import cern.rade.services.japc.security.RbacAuthentication;
import org.springframework.http.HttpHeaders;

public abstract class GetEndpoint {
    public static final String ACQ_STAMP_HEADER = "Acq-Stamp";
    public static final String CYCLE_STAMP_HEADER = "Cycle-Stamp";

    protected GetService getService(String selector, String device, String property, String field) {
        return new GetService(selector, device, property, field, RbacAuthentication.current().orElse(null));
    }

    protected HttpHeaders responseHeaders(GetService getService) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(ACQ_STAMP_HEADER, Long.toString(getService.getAcquisitionStamp()));
        headers.set(CYCLE_STAMP_HEADER, Long.toString(getService.getCycleStamp()));
        RbacHeaders.setCached(headers, getService.getRbacSession());
        return headers;
    }
}
