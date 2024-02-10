package com.github.renatolsjf.chassi.monitoring;

import com.github.renatolsjf.chassi.monitoring.request.MultiRequestData;
import com.github.renatolsjf.chassi.monitoring.request.RequestData;
import com.github.renatolsjf.chassi.monitoring.request.internal.EmptyContextBasedRequestData;
import com.github.renatolsjf.chassi.monitoring.request.internal.HealthMultiRequestData;
import com.github.renatolsjf.chassi.rendering.Media;
import com.github.renatolsjf.chassi.rendering.Renderable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ApplicationHealth implements Renderable {

    private List<RequestData> requests = new ArrayList<>();

    public ApplicationHealth(List<String> internalActions, List<String> restOperations) {

        List<RequestData> requestList = ApplicationHealthEngine.getSnapshot();

        List<RequestData> rd = new ArrayList<>();
        internalActions.forEach(s -> {
            List<RequestData> requestsForAction = requestList.stream().filter(
                    r -> r.belongsTo(s)).collect(Collectors.toList());

            if (requestsForAction.isEmpty()) {
                requestsForAction.add(new EmptyContextBasedRequestData());
            }

            rd.add(new HealthMultiRequestData(requestsForAction, s.toLowerCase(),
                    "operation", null, null,
                    HealthMultiRequestData.PRINT_ALL));
        });

        MultiRequestData rIntegrations = new HealthMultiRequestData(null, null,
                "rest", "integrations",
                HealthMultiRequestData.PRINT_NONE);

        Map<String, MultiRequestData> restRequests = new HashMap<>();
        restOperations.forEach(op -> {

            String[] tags = op.split("\\.");
            List<RequestData> requestsForAction = requestList.stream().filter(
                    r -> r.belongsTo(tags)).collect(Collectors.toList());

            MultiRequestData rdGroup = restRequests.get(tags[0]);
            if (rdGroup == null) {
                rdGroup = new HealthMultiRequestData(tags[0], "group", "services",
                        null, HealthMultiRequestData.PRINT_STATUS
                        | HealthMultiRequestData.PRINT_LOAD);
                restRequests.put(tags[0], rdGroup);
                rIntegrations.add(rdGroup);
            }

            MultiRequestData rdService = restRequests.get(tags[0] + "." + tags[1]);
            if (rdService == null) {
                rdService = new HealthMultiRequestData(tags[1], "service", "operations",
                        null, HealthMultiRequestData.PRINT_STATUS
                        | HealthMultiRequestData.PRINT_LOAD);
                restRequests.put(tags[0] + "." + tags[1], rdService);
                rdGroup.add(rdService);
            }

            MultiRequestData rdOperation = new HealthMultiRequestData(requestsForAction, tags[2],
                    "operation", null, null,
                    HealthMultiRequestData.PRINT_ALL);
            rdService.add(rdOperation);

        });

        MultiRequestData appRequestData = new HealthMultiRequestData(rd, null, null,
                "operations", "application",
                HealthMultiRequestData.PRINT_ALL);

        this.requests.add(appRequestData);
        this.requests.add(rIntegrations);
    }

    @Override
    public Media render(Media media) {
        for (RequestData rd: this.requests) {
            media = rd.render(media);
        }
        return media;
    }

}
