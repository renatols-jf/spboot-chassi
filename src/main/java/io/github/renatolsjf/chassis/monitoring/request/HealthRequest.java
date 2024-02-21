package io.github.renatolsjf.chassis.monitoring.request;

import io.github.renatolsjf.chassis.Chassis;
import io.github.renatolsjf.chassis.rendering.Media;
import io.github.renatolsjf.chassis.request.Request;
import io.github.renatolsjf.chassis.request.RequestOutcome;

import java.util.List;
import java.util.Map;

@HealthIgnore
public class HealthRequest extends Request {

    private static String OPERATION_NAME = "HEALTH_CHECK";

    public HealthRequest() {
        this(OPERATION_NAME);
    }

    public HealthRequest(String operation) {
        this(operation, null, null, null, null);
    }

    public HealthRequest(String operation, String transactionId, String correlationId,
                         List<String> projection, Map<String, String> requestContextEntries) {
        super(operation, transactionId, correlationId, projection, requestContextEntries);
    }

    @Override
    protected Media doProcess() {
        return Media.ofRenderable(Chassis.getInstance().getApplicationHealthEngine().getCurrentApplicationHealth());
    }

    @Override
    protected RequestOutcome resolveError(Throwable t) {
        return RequestOutcome.SERVER_ERROR;
    }
}
