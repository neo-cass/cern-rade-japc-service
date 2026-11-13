package cern.rade.services.japc.security;

import cern.rade.services.japc.ApiPaths;
import cern.rade.services.japc.rbac.RbacHeaders;
import cern.rade.services.japc.rbac.RbacSession;
import cern.rade.services.japc.rbac.RbacSessionRegistry;
import cern.rbac.client.authentication.AuthenticationException;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The session of the calling client. The login itself happens in {@link RbacHeaderFilter}, which only lets
 * {@code PUT} log in with credentials; this endpoint then hands out the cached id. The caller can only look at or
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
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CacheControl.noStore());
        RbacHeaders.setCached(headers, session());
        return ResponseEntity.noContent().headers(headers).build();
    }

    @GetMapping
    public SessionInfoView info() {
        return SessionInfoView.of(session());
    }

    @DeleteMapping
    public ResponseEntity<Void> logout() {
        registry.revoke(session());
        return ResponseEntity.noContent().build();
    }

    private static RbacSession session() {
        return RbacAuthentication.current().orElseThrow(() -> new AuthenticationException("No RBAC session"));
    }
}
