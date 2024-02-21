package io.github.renatolsjf.chassis.monitoring.request;

import io.github.renatolsjf.chassis.Chassi;
import io.github.renatolsjf.chassis.rendering.Media;
import io.github.renatolsjf.chassis.request.Request;
import io.github.renatolsjf.chassis.request.RequestOutcome;

import java.util.List;
import java.util.Map;

@HealthIgnore
public class HealthRequest extends Request {

    public HealthRequest(String operation, String transactionId, String correlationId,
                         List<String> projection, Map<String, String> requestContextEntries) {
        super(operation, transactionId, correlationId, projection, requestContextEntries);
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
