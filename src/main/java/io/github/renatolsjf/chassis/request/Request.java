package io.github.renatolsjf.chassis.request;

import io.github.renatolsjf.chassis.Chassis;
import io.github.renatolsjf.chassis.context.AppRegistry;
import io.github.renatolsjf.chassis.context.Context;
import io.github.renatolsjf.chassis.context.ContextCreator;
import io.github.renatolsjf.chassis.context.data.Classified;
import io.github.renatolsjf.chassis.context.data.cypher.IgnoringCypher;
import io.github.renatolsjf.chassis.monitoring.request.HealthIgnore;
import io.github.renatolsjf.chassis.rendering.Media;
import io.github.renatolsjf.chassis.rendering.transforming.MediaTransformerFactory;
import io.github.renatolsjf.chassis.util.StringConcatenator;
import io.github.renatolsjf.chassis.util.genesis.ObjectExtractor;
import io.github.renatolsjf.chassis.validation.ValidationException;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A request is the base class to represent a unit of processing. One may think of it as a unit of behavior.
 * It deals with common concerns like metrics, logging, and context initialization. The idea is to abstract
 * concerns not linked to the domain leaving the implementations free to implement behavior specific to the application domain.
 *
 * A request is entrypoint agnostic. It does not matter whether we are receiving an HTTP call, dequeuing a message, or some other sort of
 * communication mean. This is so because we generally see information generated (e.g. metrics) related to the entrypoint and
 * we desire those information to be related solely to the behavior.
 */
@ContextCreator
public abstract class Request {

    @Classified(IgnoringCypher.class)
    protected Context context;

    @Classified(IgnoringCypher.class)
    protected RequestOutcome outcome;

    public Request(String operation, String transactionId, String correlationId) {
        this(operation, transactionId, correlationId, Collections.emptyList(), Collections.emptyMap(), null);
    }

    public Request(String operation, String transactionId, String correlationId, List<String> projection) {
        this(operation, transactionId, correlationId, projection, Collections.emptyMap(), null);
    }

    public Request(String operation, String transactionId, String correlationId, String requestContextEntries) {
        this(operation, transactionId, correlationId, Collections.emptyList(), requestContextEntries);
    }

    public Request(String operation, String transactionId, String correlationId, Map<String, String> requestContextEntries) {
        this(operation, transactionId, correlationId, Collections.emptyList(), requestContextEntries, null);
    }

    public Request(String operation, String transactionId, String correlationId, List<String> projection,
                   String requestContextEntries) {
        this(operation, transactionId, correlationId, projection, new EntryResolver(requestContextEntries).getMapRepresentation(), null);
    }

    public Request(String operation, String transactionId, String correlationId, List<String> projection,
                   String requestContextEntries, String traceParent) {
        this(operation, transactionId, correlationId, projection, new EntryResolver(requestContextEntries).getMapRepresentation(), traceParent);
    }

    public Request(String operation, String transactionId, String correlationId, List<String> projection,
                   Map<String, String> requestContextEntries) {
        this (operation, transactionId, correlationId, projection, requestContextEntries, null);
    }

    public Request(String operation, String transactionId, String correlationId, List<String> projection,
                   Map<String, String> requestContextEntries, String traceParent) {

        if (requestContextEntries == null) {
            requestContextEntries = Collections.emptyMap();
        }

        this.context = Context.initialize(transactionId, correlationId)
                .withOperation(operation)
                .withProjection(projection);

        //if (!this.getClass().isAnnotationPresent(NotTraceable.class)) {
            this.context.withTracing(this.getClass().getName(),
                    StringConcatenator.of(this.getClass().getSimpleName(), operation).twoColons(), traceParent);
        //}

        requestContextEntries.entrySet().forEach(e -> this.context.withRequestContextEntry(e.getKey(), e.getValue()));

        for(Field f: new ObjectExtractor(this).getFields()) {
            if (f.getAnnotation(Inject.class) != null) {
                try {
                    f.trySetAccessible();
                    f.set(this, this.requestResource(f.getType()));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

    }

    protected abstract Media doProcess();
    protected abstract RequestOutcome doResolveError(Throwable t);

    protected <T> T requestResource(Class<T> clazz) { return AppRegistry.getResource(clazz); }

    protected RequestOutcome resolveError(Throwable t) {

        RequestOutcome requestOutcome;
        if (t instanceof ValidationException) {
            return RequestOutcome.CLIENT_ERROR;
        } else {
            requestOutcome = this.doResolveError(t);
        }

        if (requestOutcome == null) {
            requestOutcome = RequestOutcome.SERVER_ERROR;
        }
        return requestOutcome;

    }

    public final Media process() {

        boolean healthIgnore = this.getClass().isAnnotationPresent(HealthIgnore.class);

        try {

            if (!healthIgnore) {
                Chassis.getInstance().getApplicationHealthEngine().operationStarted();
            }

            this.context.createLogger()
                    .info("Starting request: {}", this.getClass().getSimpleName())
                    .attachObject(this)
                    .log();

            Media m = doProcess().transform(MediaTransformerFactory.createTransformerFromContext(context));

            this.context.createLogger()
                    .info("Completed request: {}", this.getClass().getSimpleName())
                    .attach("result", "success")
                    .log();

            this.outcome = RequestOutcome.SUCCESS;

            return m;

        } catch (Throwable e) {

            this.outcome = this.resolveError(e);

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

            if (!healthIgnore) {
                Chassis.getInstance().getApplicationHealthEngine().operationEnded(this.outcome);
            }
            this.context.clear(this.outcome == RequestOutcome.SUCCESS);

        }
    }

}





