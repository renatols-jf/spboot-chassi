package com.github.renatolsjf.application.entrypoint.request;

import com.github.renatolsjf.chassi.rendering.Media;
import com.github.renatolsjf.chassi.request.Request;
import com.github.renatolsjf.chassi.request.RequestOutcome;

import java.time.Duration;
import java.util.Random;

public class TestRequest extends Request {

    public TestRequest() {
        super("TEST ACTION", null, null);
    }

    @Override
    protected Media doProcess() {
        try {
            Thread.sleep(Duration.ofSeconds(new Random().nextInt(25) + 15));
        } catch (InterruptedException e) {

        }
        return Media.empty();
    }

    @Override
    protected RequestOutcome resolveError(Throwable t) {
        if (t instanceof IllegalArgumentException) {
            return RequestOutcome.CLIENT_ERROR;
        } else {
            return RequestOutcome.SERVER_ERROR;
        }
    }
}
