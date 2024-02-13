package com.github.renatolsjf.chassi.request;

import com.github.renatolsjf.chassi.Chassi;
import com.github.renatolsjf.chassi.MetricRegistry;
import com.github.renatolsjf.chassi.context.AppRegistry;
import com.github.renatolsjf.chassi.context.Context;
import com.github.renatolsjf.chassi.context.ContextCreator;
import com.github.renatolsjf.chassi.context.data.Classified;
import com.github.renatolsjf.chassi.context.data.cypher.IgnoringCypher;
import com.github.renatolsjf.chassi.monitoring.ApplicationHealthEngine;
import com.github.renatolsjf.chassi.rendering.Media;
import com.github.renatolsjf.chassi.rendering.transforming.MediaTransformerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@ContextCreator
public abstract class Request {

    @Classified(IgnoringCypher.class)
    protected Context context;

    @Classified(IgnoringCypher.class)
    protected RequestOutcome outcome;

    public Request(String action, String transactionId, String correlationId) {
        this(action, transactionId, correlationId, Collections.emptyList(), Collections.emptyMap());
    }

    public Request(String action, String transactionId, String correlationId, List<String> projection) {
        this(action, transactionId, correlationId, projection, Collections.emptyMap());
    }

    public Request(String action, String transactionId, String correlationId, Map<String, String> requestContextEntries) {
        this(action, transactionId, correlationId, Collections.emptyList(), requestContextEntries);
    }

    public Request(String action, String transactionId, String correlationId, List<String> projection,
                   Map<String, String> requestContextEntries) {
        if (requestContextEntries == null) {
            requestContextEntries = Collections.emptyMap();
        }
        this.context = Context.initialize(transactionId, correlationId).withAction(action).withProjection(projection);
        requestContextEntries.entrySet().forEach(e -> this.context.withRequestContextEntry(e.getKey(), e.getValue()));
    }

    protected abstract Media doProcess();
    protected abstract RequestOutcome resolveError(Throwable t);

    protected <T> T requestResource(Class<T> clazz) { return AppRegistry.getResource(clazz); }

    public final Media process() {
        try {

            this.context.createLogger()
                    .info("Starting request: {}", this.getClass().getSimpleName())
                    .attachObject(this)
                    .log();

            Media m =  doProcess().transform(MediaTransformerFactory.createTransformerFromContext(context));

            this.context.createLogger()
                    .info("Completed request: {}", this.getClass().getSimpleName())
                    .attach("result", "success")
                    .log();

            //ApplicationHealthEngine.addContextBasedRequestData(this.context);
            this.outcome = RequestOutcome.SUCCESS;

            return m;

        } catch (Exception e) {

            this.outcome = this.resolveError(e);
            //ApplicationHealthEngine.addContextBasedRequestData(this.context,
                    //outcome == RequestOutcome.CLIENT_ERROR,
                    //outcome == RequestOutcome.SERVER_ERROR);

            String errorMessage = e.getClass().getSimpleName();
            if (e.getMessage() != null) {
                errorMessage += ": " + e.getMessage();
            }

            this.context.createLogger()
                    .error("Completed request {} with error: {}", this.getClass().getSimpleName(),
                            errorMessage, e)
                    .attach("result", this.outcome.toString())
                    .log();

            throw e;

        } finally {

            Chassi.getInstance().getMetricRegistry().createBuilder("operation_request_seconds")
                    .withLabel("action", context.getAction())
                    .withLabel("outcome", this.outcome.toString())
                    .buildHistogram(MetricRegistry.HistogramRanges.REQUEST_DURATION)
                    .observe(context.getElapsedSeconds());

            Context.clear();
        }
    }

}





