package com.github.renatolsjf.chassi.monitoring.request.internal;

import com.github.renatolsjf.chassi.monitoring.request.MultiRequestData;
import com.github.renatolsjf.chassi.monitoring.request.RequestData;
import com.github.renatolsjf.chassi.rendering.Media;

import java.util.List;
import java.util.Map;

public class MetricMultiRequestData extends MultiRequestData {

    private String prefix;
    private Map<String, String> tags;

    public MetricMultiRequestData(List<RequestData> requests, String name, String prefix) {
        this(requests, name, prefix, null);
    }

    public MetricMultiRequestData(List<RequestData> requests, String name, String prefix,
                                  Map<String, String> tags) {
        super(requests, name);
        this.prefix = prefix;
        this.tags = tags;
    }

    @Override
    public Media render(Media media) {

        String tags = this.getTags();

        media
                .print(prefix + "_heatlh{operation=\""+ this.getName() + "\"" + tags + "}",
                        this.healthPercentage())
                .print(prefix + "_request_number{operation=\""+ this.getName() + "\"" + tags + "}",
                        this.getRpm())
                .print(prefix + "_request_average_time{operation=\""+ this.getName() + "\"" + tags + "}",
                        this.averageRequestDuration())
                .print(prefix + "_request_max10_time{operation=\""+ this.getName() + "\"" + tags + "}",
                        this.max10RequestDuration())
                .print(prefix + "_request_max1_time{operation=\""+ this.getName() + "\"" + tags + "}",
                        this.max1RequestDuration());

        for(Map.Entry<String, Integer> e: this.getResult().entrySet()) {
            media.print(prefix + "_result{operation=\""+ this.getName() + "\"," +
                            "outcome=\""+ e.getKey() +"\"" + tags + "}", e.getValue());
        }

        return media;
    }

    private String getTags() {

        if (this.tags == null || this.tags.isEmpty()) {
            return "";
        }

        StringBuilder sb= new StringBuilder(",");
        for (Map.Entry<String, String> e: this.tags.entrySet()) {
            sb.append(e.getKey()).append("=\"").append(e.getValue()).append("\",");
        }
        return sb.toString();

    }

}
