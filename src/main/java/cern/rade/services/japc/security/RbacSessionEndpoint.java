package cern.rade.services.japc.security;

import cern.rade.services.japc.ApiPaths;
import cern.rade.services.japc.rbac.LoginType;
import cern.rade.services.japc.rbac.RbacHeaders;
import cern.rade.services.japc.rbac.RbacSession;
import cern.rade.services.japc.rbac.RbacSessionRegistry;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * The session of the calling client. The login itself happens in {@link RbacHeaderFilter}, which only lets
 * {@code PUT} log in with credentials; this endpoint then hands out the cached id. A {@code Token} request has no
 * session to create or revoke, so PUT and DELETE answer 400. The caller can only look at or
 * revoke the session whose id it sent. PUT rather than POST because the LabVIEW client cannot send POST.
 */
@RestController
@RequestMapping(ApiPaths.RBAC_SESSION)
public class RbacSessionEndpoint {
    private final RbacSessionRegistry registry;

    public RbacSessionEndpoint(RbacSessionRegistry registry) {
        this.registry = registry;
    }

    /**
     * Logs in, or confirms an existing session. The response is the only place that hands out the cached id.
     */
    @PutMapping
    public ResponseEntity<Void> login() {
        RbacSession session = RbacAuthentication.required();
        if (session.loginType() == LoginType.TOKEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rbac: Token is sent on every request; there is no session to create");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.noStore());
        RbacHeaders.setCached(headers, session);
        return ResponseEntity.noContent().headers(headers).build();
    }

    @GetMapping
    public SessionInfoView info() {
        return SessionInfoView.of(RbacAuthentication.required());
    }

    @DeleteMapping
    public ResponseEntity<Void> logout() {
        RbacSession session = RbacAuthentication.required();
        if (session.loginType() == LoginType.TOKEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rbac: Token requests have no cached session to log out");
        }
        registry.revoke(session);
        return ResponseEntity.noContent().build();
    }
}
