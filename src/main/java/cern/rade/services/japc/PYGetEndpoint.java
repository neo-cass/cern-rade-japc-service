package cern.rade.services.japc;

import cern.rade.services.japc.rbac.RBACClient;
import cern.rade.services.japc.rbac.RBACManager;
import cern.rbac.common.TokenFormatException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/rest/get", produces = "application/json")
public class PYGetEndpoint {
    RBACManager  rbacManager = ContextProvider.getRbacManager();
    @GetMapping("/{device}/{property}")
    public ResponseEntity<Byte[]> get(@PathVariable("selector") String selector,
                                      @PathVariable("device") String device,
                                      @PathVariable("property") String property)
    {

    }

    @GetMapping("/{device}/{property}")
    public ResponseEntity<Byte[]> get(@PathVariable("device") String device,
                                      @PathVariable("property") String property,
                                      @RequestHeader(value = "rbac_id") int rbacId)
    {
        RBACClient rbacClient = rbacManager.getClient(rbacId);

    }

    @GetMapping("/{device}/{property}")
    public ResponseEntity<Byte[]> get(@PathVariable("device") String device,
                                      @PathVariable("property") String property,
                                      @RequestHeader(value = "rbac_token") String rbac_token)
            throws TokenFormatException {
        RBACClient rbacClient = rbacManager.createClient(rbac_token.getBytes());
    }

    @GetMapping("/{device}/{property}")
    public ResponseEntity<Byte[]> get(@PathVariable("device") String device,
                                      @PathVariable("property") String property,
                                      @RequestHeader(value = "username") String username,
                                      @RequestHeader(value = "password") String password) {
        RBACClient rbacClient = rbacManager.createClient(username, password.toCharArray());

    }

    @GetMapping(value = "/{device}/{property}/{field}")
    public ResponseEntity<Byte[]> get(@PathVariable("device") String device,
                                      @PathVariable("property") String property,
                                      @PathVariable("field") String field) {

    }

    @GetMapping("/{device}/{property}/{field}")
    public ResponseEntity<Byte[]> get(@PathVariable("device") String device,
                                      @PathVariable("property") String property,
                                      @PathVariable("field") String field,
                                      @RequestHeader(value = "rbac_id") int rbacId)
    {
        RBACClient rbacClient = rbacManager.getClient(rbacId);
    }

    @GetMapping("/{device}/{property}/{field}")
    public ResponseEntity<Byte[]> get(@PathVariable("device") String device,
                                      @PathVariable("property") String property,
                                      @PathVariable("field") String field,
                                      @RequestHeader(value = "rbac_token") String rbac_token)
            throws TokenFormatException {
        RBACClient rbacClient = rbacManager.createClient(rbac_token.getBytes());
    }

    @GetMapping("/{device}/{property}/{field}")
    public ResponseEntity<Byte[]> get(@PathVariable("device") String device,
                                      @PathVariable("property") String property,
                                      @PathVariable("field") String field,
                                      @RequestHeader(value = "username") String username,
                                      @RequestHeader(value = "password") String password) {
        RBACClient rbacClient = rbacManager.createClient(username, password.toCharArray());
    }
}
