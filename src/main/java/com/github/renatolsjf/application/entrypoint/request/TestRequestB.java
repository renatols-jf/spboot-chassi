package com.github.renatolsjf.application.entrypoint.request;

import com.github.renatolsjf.chassi.integration.RestOperation;
import com.github.renatolsjf.chassi.rendering.Media;
import com.github.renatolsjf.chassi.request.Request;
import com.github.renatolsjf.chassi.request.RequestOutcome;

public class TestRequestB extends Request {

    public TestRequestB() {
        super("TEST B", null, null);
    }

    @Override
    protected Media doProcess() {
        RestOperation.get("GOOGLE", "GOOGLE", "SEARCH", "https://www.google.com", null, null).call(null);
        return Media.empty();
    }

    @Override
    protected RequestOutcome resolveError(Throwable t) {
        return RequestOutcome.SERVER_ERROR;
    }
}
