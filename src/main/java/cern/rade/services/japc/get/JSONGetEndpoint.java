package cern.rade.services.japc.get;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/get", produces = "application/json")
public class JSONGetEndpoint extends GetEndpoint{
    //TODO once codec for JSON is finished.
}
