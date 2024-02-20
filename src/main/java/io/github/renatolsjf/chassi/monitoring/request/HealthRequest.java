package io.github.renatolsjf.chassi.monitoring.request;

import io.github.renatolsjf.chassi.Chassi;
import io.github.renatolsjf.chassi.rendering.Media;
import io.github.renatolsjf.chassi.request.Request;
import io.github.renatolsjf.chassi.request.RequestOutcome;

import java.util.List;
import java.util.Map;

@HealthIgnore
public class HealthRequest extends Request {

    public HealthRequest(String action, String transactionId, String correlationId,
                         List<String> projection, Map<String, String> requestContextEntries) {
        super(action, transactionId, correlationId, projection, requestContextEntries);
    }

    @Override
    protected Media doProcess() {
        return Media.ofRenderable(Chassi.getInstance().getApplicationHealthEngine().getCurrentApplicationHealth());
    }

    @Override
    protected RequestOutcome resolveError(Throwable t) {
        return RequestOutcome.SERVER_ERROR;
    }
}
