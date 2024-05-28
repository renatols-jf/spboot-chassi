package io.github.renatolsjf.chassis.context;

import io.github.renatolsjf.chassis.Chassis;
import io.github.renatolsjf.chassis.Labels;
import io.github.renatolsjf.chassis.context.data.LoggingAttribute;
import io.github.renatolsjf.chassis.monitoring.tracing.TelemetryContext;
import io.github.renatolsjf.chassis.monitoring.tracing.TracingContext;
import io.github.renatolsjf.chassis.rendering.transforming.Projection;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Represents a context for a given request. The idea is to store information that needs to be available to the whole
 * request duration but is, most likely, not domain related. That's the case for fields like a transaction id and
 * correlation id. They generally need to be available for logging and API calls, but it does not make sense
 * to use them as parameters for domain methods.
 *
 * The application expects a context to exist during a request lifecycle. In case of absence, an error is likely to be thrown.
 * Be it as it may, by default, a context can not be created anywhere. It's necessary to annotate the calling as a ContextCreator
 * @see ContextCreator
 *
 * The current implementation has no support for NIO. A relatively easy solution is to implement a snapshot, which holds a context object
 * and clears the context ThreadLocal. A restore method then would reinitialize it. A less manual solution would likely require code manipulation.
 */
public class Context {

    private static ThreadLocal<Context> tlContext = new ThreadLocal<>();

    public static boolean isAvailable() {
        return tlContext.get() != null;
    }

    public static Context forRequest() {
        Context c = tlContext.get();
        if (c == null) {
            throw new InvalidContextStateException("No context available for current request");
        } else {
            return c;
        }
    }

    public static Context initialize(String transactionId, String correlationId) {
        Context c = tlContext.get();
        if (c != null) {
            throw new InvalidContextStateException("A context already exists for current request");
        } else {

            if (Chassis.getInstance().getConfig().forbidUnauthorizedContextCreation()) {
                Class<?> callingClass = StackWalker.getInstance(
                        StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
                if (!(callingClass.isAnnotationPresent(ContextCreator.class))) {
                    throw new InvalidContextStateException("Caller is not authorized to initialize the context");
                }
            }

            c = new Context(transactionId, correlationId);
            tlContext.set(c);
            return c;

        }
    }

    public void clear(boolean success) {
        this.telemetryContext.clear(success);
        tlContext.remove();
    }

    //--------------

    private String transactionId = UUID.randomUUID().toString();
    private String correlationId;
    private String operation;
    private Projection projection = new Projection(Collections.emptyList());
    private Map<String, String> requestContext = new HashMap<>();
    private TelemetryContext telemetryContext = Chassis.getInstance().getTelemetryAgent().empty();

    private long requestStartingTime = System.currentTimeMillis();
    private Map<String, Long> operationTimeCounter = new HashMap<>();

    private Context(String transactionId) {
        if (transactionId != null && !(transactionId.isBlank())) {
            this.transactionId = transactionId;
        }
    }
    private Context(String transactionId, String correlationId) {
        this(transactionId);
        this.correlationId = correlationId;
    }

    public String getTransactionId() {
        return this.transactionId;
    }

    public String getCorrelationId() {
        return this.correlationId;
    }

    public Long getElapsedMillis() {
        return System.currentTimeMillis() - this.requestStartingTime;
    }

    public double getElapsedSeconds() {
        return this.getElapsedMillis() / 1000d;
    }

    public Duration getRequestDuration() {
        return Duration.ofMillis(this.getElapsedMillis());
    }

    public Projection getProjection() {
        return this.projection;
    }

    public Context withCorrelationId(String correlationId) {
        if (Chassis.getInstance().getConfig().allowContextCorrelationIdUpdate()) {
            this.correlationId = correlationId;
        }
        return this;
    }

    public Context withOperation(String operation) {
        this.operation = operation;
        return this;
    }

    public Context withRequestContextEntry(String key, String value) {
        this.requestContext.put(key, value);
        return this;
    }

    public Context withProjection(List<String> projection) {
        if (projection != null) {
            this.projection = new Projection(projection);
        } else {
            this.projection = new Projection(Collections.emptyList());
        }
        return this;
    }

    public Context withTracing(String scopeOwner, String traceName, String tracingHeader) {
        if (Chassis.getInstance().getConfig().tracingEnabled()) {
            this.telemetryContext = Chassis.getInstance().getTelemetryAgent().start(scopeOwner, traceName, tracingHeader);
        }
        return this;
    }

    public TelemetryContext getTelemetryContext() {
        return this.telemetryContext;
    }

    public Map<String, String> getRequestContext() {
        return this.requestContext;
    }

    public String getOperation() {
        return this.operation;
    }

    public Context recordOperationTime(String tag, long operationTime) {
        this.operationTimeCounter.put(tag,
                this.operationTimeCounter.getOrDefault(tag, 0L) + operationTime);
        return this;
    }

    public ApplicationLogger createLogger() {
        return this.createLogger(ExecutionContext.unavailable());
    }


    public ApplicationLogger createLogger(ExecutionContext executionContext) {
        if (Chassis.getInstance().getConfig().useCallingClassNameForLogging()) {
            return new ApplicationLogger(StackWalker.getInstance(
                    StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass(),
                    this.createFixedLoggingAttributes(executionContext));
        } else {
            return new ApplicationLogger(this.createFixedLoggingAttributes(executionContext));
        }
    }

    public ApplicationLogger createLogger(Class<?> clazz, ExecutionContext executionContext) {
        return new ApplicationLogger(clazz, this.createFixedLoggingAttributes(executionContext));
    }

    public Map<String, Long> getOperationTimeByType() {
        Map<String, Long> times = new HashMap<>(this.operationTimeCounter);
        AtomicLong taggedValues = new AtomicLong(0);
        this.operationTimeCounter.forEach((key, value) -> {
            taggedValues.addAndGet(value);
        });
        times.put("internal", this.getElapsedMillis() - taggedValues.get());
        return times;
    }

    private Map<String, LoggingAttribute> createFixedLoggingAttributes(ExecutionContext executionContext) {

        Chassis c = Chassis.getInstance();
        Map<String, LoggingAttribute> loggingAttributes = new HashMap<>();
        boolean overrideDefaultAttributes = c.getConfig().allowDefaultLoggingAttributesOverride();

        if (!overrideDefaultAttributes) {
            this.requestContext.entrySet().forEach(e -> loggingAttributes.put(e.getKey(), () -> e.getValue()));
        }

        if (c.labels().isAppNameAvailable()) {
            loggingAttributes.put(c.labels().getLabel(Labels.Field.LOGGING_APPLICATION_NAME), () -> c.labels().getLabel(Labels.Field.APPLICATION_NAME));
        }
        loggingAttributes.put(c.labels().getLabel(Labels.Field.LOGGING_TRANSACTION_ID), () -> this.transactionId);
        loggingAttributes.put(c.labels().getLabel(Labels.Field.LOGGING_CORRELATION_ID), () -> this.correlationId);
        loggingAttributes.put(c.labels().getLabel(Labels.Field.LOGGING_OPERATION), () -> this.operation);
        loggingAttributes.put(c.labels().getLabel(Labels.Field.LOGGING_ELAPSED_TIME), () -> this.getElapsedMillis().toString());
        loggingAttributes.put(c.labels().getLabel(Labels.Field.LOGGING_OPERATION_TIME), () -> {
            Map<String, Long> operationTimes = this.getOperationTimeByType();
            operationTimes.put("total", this.getElapsedMillis());
            return operationTimes.toString();
        });

        if (c.getConfig().printTraceIdOnLogs() && this.getTelemetryContext().isTracingEnabled()) {

            if (executionContext.isExecutionContextAvailable()) {
                String traceId = executionContext.getTraceId();
                String spanId = executionContext.getSpanId();
                loggingAttributes.put(c.labels().getLabel(Labels.Field.LOGGING_TRACE_ID), () -> traceId);
                loggingAttributes.put(c.labels().getLabel(Labels.Field.LOGGING_SPAN_ID), () -> spanId);
            } else {
                TracingContext tracingContext = this.telemetryContext.getTracingContext();
                String traceId = tracingContext.getTraceId();
                loggingAttributes.put(c.labels().getLabel(Labels.Field.LOGGING_TRACE_ID), () -> traceId);
                loggingAttributes.put(c.labels().getLabel(Labels.Field.LOGGING_SPAN_ID), () -> this.telemetryContext.getTracingContext().getSpanId());
            }

        }

        if (overrideDefaultAttributes) {
            this.requestContext.entrySet().forEach(e -> loggingAttributes.put(e.getKey(), () -> e.getValue()));
        }

        return loggingAttributes;

    }

    public static boolean isTracingEnabled() {
        return Context.isAvailable() && Context.forRequest().getTelemetryContext().isTracingEnabled();
    }

}