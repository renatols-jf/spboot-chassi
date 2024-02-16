package com.github.renatolsjf.application.entrypoint.rest;

import com.github.renatolsjf.application.entrypoint.request.TestProjectionRequest;
import com.github.renatolsjf.application.entrypoint.request.TestRequest;
import com.github.renatolsjf.application.entrypoint.request.TestRequestB;
import com.github.renatolsjf.chassi.monitoring.request.HealthRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TestController {


    @GetMapping("test")
    public ResponseEntity test() {
        return ResponseEntity.ok(new TestRequest().process().render());
    }

    @GetMapping("testb")
    public ResponseEntity testb() {
        return ResponseEntity.ok(new TestRequestB().process().render());
    }

    @GetMapping("testprojection")
    public ResponseEntity testprojection(@RequestParam(required = false) List<String> projection) {
        return ResponseEntity.ok(new TestProjectionRequest(projection).process().render());
    }

    @GetMapping("status")
    public ResponseEntity health() {
        return ResponseEntity.ok(new HealthRequest("HEALTH", null, null, null, null).process().render());
    }

}
