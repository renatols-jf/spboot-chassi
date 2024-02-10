package com.github.renatolsjf.chassi.monitoring;

import com.github.renatolsjf.chassi.context.Context;
import com.github.renatolsjf.chassi.monitoring.request.RequestData;
import com.github.renatolsjf.chassi.monitoring.request.internal.ContextBasedRequestData;
import com.github.renatolsjf.chassi.monitoring.request.rest.RestBasedRequestData;

import java.util.*;

public abstract class ApplicationHealthEngine {

    private static final int MAX_SNAPSHOT_TIME = 30 * 60 * 1000; // 30 minutos
    private static final int MIN_SNAPSHOT_TIME = 45 * 1000; // 45 segundos

    private static List<RequestData> requests = new ArrayList<>();
    private static List<RequestData> snapshot = Collections.synchronizedList(new ArrayList<>());
    private static long currentSnapshotTime = System.currentTimeMillis();

    private ApplicationHealthEngine() {}

    public static void addContextBasedRequestData(Context c) {
        /*if (c.getAction() == null) return;
        requests.add(new ContextBasedRequestData(c));*/
    }

    public static void addContextBasedRequestData(Context c, boolean clientError, boolean serverError) {
        /*if (c.getAction() == null) return;
        requests.add(new ContextBasedRequestData(c, clientError, serverError));*/
    }

    public static void addRestBasedRequestData(String group, String service, String operation,
                                               long duration, int status) {
        //requests.add(new RestBasedRequestData(group, service, operation, duration, status));
    }

    public static void addRestBasedRequestDataWithConnectionError(String group, String service,
                                                                  String operation, long duration) {
        //requests.add(new RestBasedRequestData(group, service, operation, duration));
    }

    public static List<RequestData> getSnapshot() {
        if (System.currentTimeMillis() > currentSnapshotTime + MIN_SNAPSHOT_TIME) {
            takeSnapshot();
        }
        return snapshot;

    }

    public static void clear() {
        if (System.currentTimeMillis() > currentSnapshotTime + MAX_SNAPSHOT_TIME) {
            takeSnapshot();
        }
    }

    private static void takeSnapshot() {
        snapshot.clear();
        snapshot.addAll(requests);
        requests.clear();
        currentSnapshotTime = System.currentTimeMillis();
    }

}
