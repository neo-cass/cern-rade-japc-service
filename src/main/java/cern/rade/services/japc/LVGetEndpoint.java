package cern.rade.services.japc;

import cern.rade.services.japc.rbac.RBACClient;
import cern.rade.services.japc.rbac.RBACManager;
import cern.rbac.common.TokenFormatException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/rest/get", produces = "application/labview")
public class LVGetEndpoint {
    RBACManager  rbacManager = ContextProvider.getRbacManager();
    @GetMapping("/{selector}/{device}/{property}")
    public ResponseEntity<Byte[]> get(@PathVariable("selector") String selector,
                                      @PathVariable("device") String device,
                                      @PathVariable("property") String property)
    {

    }

    @GetMapping("/{selector}/{device}/{property}")
    public ResponseEntity<Byte[]> get(@PathVariable("selector") String selector,
                                      @PathVariable("device") String device,
                                      @PathVariable("property") String property,
                                      @RequestHeader(value = "rbac_id") int rbacId)
    {
        RBACClient rbacClient = rbacManager.getClient(rbacId);

    }

    @GetMapping("/{selector}/{device}/{property}")
    public ResponseEntity<Byte[]> get(@PathVariable("selector") String selector,
                                      @PathVariable("device") String device,
                                      @PathVariable("property") String property,
                                      @RequestHeader(value = "rbac_token") String rbac_token)
            throws TokenFormatException {
        RBACClient rbacClient = rbacManager.createClient(rbac_token.getBytes());
    }

    @GetMapping("/{selector}/{device}/{property}")
    public ResponseEntity<Byte[]> get(@PathVariable("selector") String selector,
                                      @PathVariable("device") String device,
                                      @PathVariable("property") String property,
                                      @RequestHeader(value = "username") String username,
                                      @RequestHeader(value = "password") String password) {
        RBACClient rbacClient = rbacManager.createClient(username, password.toCharArray());

    }

    @GetMapping(value = "/{selector}/{device}/{property}/{field}")
    public ResponseEntity<Byte[]> get(@PathVariable("selector") String selector,
                                      @PathVariable("device") String device,
                                      @PathVariable("property") String property,
                                      @PathVariable("field") String field) {

    }

    @GetMapping("/{selector}/{device}/{property}/{field}")
    public ResponseEntity<Byte[]> get(@PathVariable("selector") String selector,
                                      @PathVariable("device") String device,
                                      @PathVariable("property") String property,
                                      @PathVariable("field") String field,
                                      @RequestHeader(value = "rbac_id") int rbacId)
    {
        RBACClient rbacClient = rbacManager.getClient(rbacId);
    }

    @GetMapping("/{selector}/{device}/{property}/{field}")
    public ResponseEntity<Byte[]> get(@PathVariable("device") String device,
                                      @PathVariable("property") String property,
                                      @PathVariable("field") String field,
                                      @RequestHeader(value = "rbac_token") String rbac_token)
            throws TokenFormatException {
        RBACClient rbacClient = rbacManager.createClient(rbac_token.getBytes());
    }

    @GetMapping("/{selector}/{device}/{property}/{field}")
    public ResponseEntity<Byte[]> get(@PathVariable("selector") String selector,
                                      @PathVariable("device") String device,
                                      @PathVariable("property") String property,
                                      @PathVariable("field") String field,
                                      @RequestHeader(value = "username") String username,
                                      @RequestHeader(value = "password") String password) {
        RBACClient rbacClient = rbacManager.createClient(username, password.toCharArray());
    }

    @GetMapping("/{selector}/{device}")
    public ResponseEntity<Byte[]> get(@PathVariable("selector") String selector,
                                      @PathVariable("device") String device,
                                      @RequestHeader(value = "type_descriptor") String type_descriptor) {

    }

    @GetMapping("/{selector}/{device}")
    public ResponseEntity<Byte[]> get(@PathVariable("selector") String selector,
                                      @PathVariable("device") String device,
                                      @RequestHeader(value = "rbac_id") int rbacId)
    {
        RBACClient rbacClient = rbacManager.getClient(rbacId);
    }

    @GetMapping("/{selector}/{device}")
    public ResponseEntity<Byte[]> get(@PathVariable("selector") String selector,
                                      @PathVariable("device") String device,
                                      @RequestHeader(value = "rbac_token") String rbac_token)
            throws TokenFormatException {

        RBACClient rbacClient = rbacManager.createClient(rbac_token.getBytes());
    }

    @GetMapping("/{selector}/{device}")
    public ResponseEntity<Byte[]> get(@PathVariable("selector") String selector,
                                      @PathVariable("device") String device,
                                      @RequestHeader(value = "username") String username,
                                      @RequestHeader(value = "password") String password) {
        RBACClient rbacClient = rbacManager.createClient(username, password.toCharArray());

    }


}
