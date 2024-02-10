package com.github.renatolsjf.chassi.monitoring;

import com.github.renatolsjf.chassi.monitoring.request.RequestData;
import com.github.renatolsjf.chassi.monitoring.request.internal.EmptyContextBasedRequestData;
import com.github.renatolsjf.chassi.monitoring.request.internal.MetricMultiRequestData;
import com.github.renatolsjf.chassi.monitoring.request.rest.EmptyRestBasedRequestData;
import com.github.renatolsjf.chassi.rendering.Media;
import com.github.renatolsjf.chassi.rendering.Renderable;

import java.util.*;
import java.util.stream.Collectors;

public class ApplicationMetrics implements Renderable {

    private List<RequestData> requests = new ArrayList<>();

    public ApplicationMetrics(List<String> internalOperations, List<String> restOperations) {
        List<RequestData> rd = ApplicationHealthEngine.getSnapshot();

        internalOperations.forEach(op -> {

            List<RequestData> requestsForAction = rd.stream().filter(
                    r -> r.belongsTo(op)).collect(Collectors.toList());

            if (requestsForAction.isEmpty()) {
                requestsForAction.add(new EmptyContextBasedRequestData());
            }

            this.requests.add(new MetricMultiRequestData(requestsForAction, op.toLowerCase(),
                    "operation", Map.of("type", "internal")));
        });

        restOperations.forEach(op -> {

            String[] tags = op.split("\\.");
            List<RequestData> requestsForAction = rd.stream().filter(
                    r -> r.belongsTo(tags)).collect(Collectors.toList());

            if (requestsForAction.isEmpty()) {
                requestsForAction.add(new EmptyRestBasedRequestData());
            }

            this.requests.add(new MetricMultiRequestData(requestsForAction, tags[2].toLowerCase(),
                    "integration",
                    Map.of("group", tags[0],
                            "service", tags[1],
                            "type", "rest")));

        });


    }

    @Override
    public Media render(Media media) {
        for (RequestData rd: this.requests) rd.render(media);
        return media;
    }
}


