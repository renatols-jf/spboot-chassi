package com.github.renatolsjf.chassi.context;

import com.github.renatolsjf.chassi.Chassi;
import com.github.renatolsjf.chassi.context.data.LoggingAttribute;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class Context {

    private static ThreadLocal<Context> tlContext = new ThreadLocal<>();

    public static Context forRequest() {
        Context c = tlContext.get();
        if (c == null) {
            throw new InvalidContextStateException("No context available for current request");
        } else {
            return c;
        }
    }

    public static Context initialize(String transactionId, String extTransactionid) {
        Context c = tlContext.get();
        if (c != null) {
            throw new InvalidContextStateException("A context already exists for current request");
        } else {

            if (Chassi.getInstance().getConfig().forbidUnauthorizedContextCreation()) {
                Class<?> callingClass = StackWalker.getInstance(
                        StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass();
                if (!(callingClass.isAnnotationPresent(ContextCreator.class))) {
                    throw new InvalidContextStateException("Caller is not authorized to initialize the context");
                }
            }

            c = new Context(transactionId, extTransactionid);
            tlContext.set(c);
            return c;

        }
    }

    public static void clear() {
        tlContext.remove();
    }

    //--------------

    private String transactionId = UUID.randomUUID().toString();
    private String extTransactionid;
    private String action;
    private Map<String, String> requestContext = new HashMap<>();

    private ApplicationLogger logger = new ApplicationLogger(this.createFixedLogginAttributes());

    private long requestStartingTime = System.currentTimeMillis();
    private Map<String, Long> operationTimeCounter = new HashMap<>();

    private Context(String transactionId) {
        if (transactionId != null && !(transactionId.isBlank())) {
            this.transactionId = transactionId;
        }
    }
    private Context(String transactionId, String extTransactionid) {
        this(transactionId);
        this.extTransactionid = extTransactionid;
    }

    public String getTransactionId() { return this.transactionId; }
    public String getExtTransactionid() { return this.extTransactionid; }
    public Long getElapsedTime() { return System.currentTimeMillis() - this.requestStartingTime; }

    public Context withExtTransactionId(String extTransactionid) {
        if (Chassi.getInstance().getConfig().allowContextExternalTransactionUpdate()) {
            this.extTransactionid = extTransactionid;
        }
        return this;
    }

    public Context withAction(String action) {
        this.action = action;
        return this;
    }

    public Context withRequestContextEntry(String key, String value) {
        this.requestContext.put(key, value);
        return this;
    }

    public Map<String, String> getRequestContext() {
        return this.requestContext;
    }

    public String getAction() {
        return this.action;
    }

    public Context recordOperationTime(String tag, long operationTime) {
        this.operationTimeCounter.put(tag,
                this.operationTimeCounter.getOrDefault(tag, 0L) + operationTime);
        return this;
    }

    public ApplicationLogger createLogger() {
        if (Chassi.getInstance().getConfig().useCallingClassNameForLogging()) {
            return new ApplicationLogger(StackWalker.getInstance(
                    StackWalker.Option.RETAIN_CLASS_REFERENCE).getCallerClass(),
                    this.createFixedLogginAttributes());
        } else {
            return this.logger;
        }
    }

    public ApplicationLogger createLogger(Class<?> clazz) {
        return new ApplicationLogger(clazz, this.createFixedLogginAttributes());
    }

    private Map<String, LoggingAttribute> createFixedLogginAttributes() {

        Map<String, LoggingAttribute> loggingAttributes = new HashMap<>();
        boolean overrideDefaultAttributes = Chassi.getInstance().getConfig().allowDefaultLoggingAttributesOverride();

        if (!overrideDefaultAttributes) {
            this.requestContext.entrySet().forEach(e -> loggingAttributes.put(e.getKey(), () -> e.getValue()));
        }

        loggingAttributes.put("transactionId", () -> this.transactionId);
        loggingAttributes.put("extTransactionId", () -> this.extTransactionid);
        loggingAttributes.put("action", () -> this.action);
        loggingAttributes.put("elapsedTime", () -> this.getElapsedTime().toString());
        loggingAttributes.put("operationTimes", () -> {

            Map<String, Long> operationTimes = new HashMap<>();
            operationTimes.put("total", this.getElapsedTime());
            AtomicLong taggedValues = new AtomicLong();
            this.operationTimeCounter.forEach((key, value) -> {
                operationTimes.put(key, value);
                taggedValues.addAndGet(value);
            });
            operationTimes.put("internal", this.getElapsedTime() - taggedValues.get());

            return operationTimes.toString();

        });

        if (overrideDefaultAttributes) {
            this.requestContext.entrySet().forEach(e -> loggingAttributes.put(e.getKey(), () -> e.getValue()));
        }

        return loggingAttributes;

    }

}