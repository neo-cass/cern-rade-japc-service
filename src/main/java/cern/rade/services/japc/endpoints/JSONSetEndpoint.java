package cern.rade.services.japc.endpoints;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/set", produces = "application/json")
public class JSONSetEndpoint extends SetEndpoint{
    //TODO once codec for JSON is finished.
}
