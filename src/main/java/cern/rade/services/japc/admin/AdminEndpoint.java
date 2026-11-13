package cern.rade.services.japc.admin;

import cern.rade.services.japc.ApiPaths;
import cern.rade.services.japc.admin.view.MeView;
import cern.rade.services.japc.admin.view.ServiceInfoView;
import cern.rade.services.japc.admin.view.SessionView;
import cern.rade.services.japc.security.RbacAuthentication;
import cern.rade.services.japc.stats.ClientRequests;
import cern.rade.services.japc.subscription.SubscriptionInfo;
import cern.rbac.client.authentication.AuthenticationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Admin API, used by the admin panel and callable with the {@code Rbac} header. Access needs the admin RBAC role.
 */
@RestController
@RequestMapping(ApiPaths.ADMIN_API)
public class AdminEndpoint {
    private final AdminService adminService;

    public AdminEndpoint(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/me")
    public MeView me() {
        return RbacAuthentication.current().map(MeView::of)
                .orElseThrow(() -> new AuthenticationException("No RBAC session"));
    }

    @GetMapping("/info")
    public ServiceInfoView info() {
        return adminService.info();
    }

    @GetMapping("/sessions")
    public List<SessionView> sessions() {
        return adminService.sessions();
    }

    @DeleteMapping("/sessions/{handle}")
    public ResponseEntity<Void> revokeSession(@PathVariable("handle") String handle) {
        return noContentOrNotFound(adminService.revokeSession(handle));
    }

    @DeleteMapping("/users/{user}/sessions")
    public ResponseEntity<Void> revokeUserSessions(@PathVariable("user") String user) {
        return noContentOrNotFound(adminService.revokeUserSessions(user) > 0);
    }

    @DeleteMapping("/sessions")
    public ResponseEntity<Void> revokeAllSessions() {
        adminService.revokeAllSessions();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/subscriptions")
    public List<SubscriptionInfo> subscriptions() {
        return adminService.subscriptions();
    }

    @DeleteMapping("/subscriptions/{id}")
    public ResponseEntity<Void> closeSubscription(@PathVariable("id") String id) {
        return noContentOrNotFound(adminService.closeSubscription(id));
    }

    @GetMapping("/clients")
    public List<ClientRequests> clients() {
        return adminService.clients();
    }

    private static ResponseEntity<Void> noContentOrNotFound(boolean found) {
        return found ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
