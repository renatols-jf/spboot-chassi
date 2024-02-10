package com.github.renatolsjf.chassi.monitoring.request.internal;

import com.github.renatolsjf.chassi.monitoring.request.MultiRequestData;
import com.github.renatolsjf.chassi.monitoring.request.RequestData;
import com.github.renatolsjf.chassi.rendering.Media;
import com.github.renatolsjf.chassi.rendering.Renderable;

import java.util.*;

public class HealthMultiRequestData extends MultiRequestData {

    public static final int PRINT_NONE = 0;
    public static final int PRINT_STATUS = 1;
    public static final int PRINT_LOAD = 2;
    public static final int PRINT_RESULT = 4;
    public static final int PRINT_ALL = 7;

    private String nodeName;
    private String childNodeName;
    private String parentNodeName;
    private int printConfig;

    public HealthMultiRequestData(String name, String nodeName,
                                  String childNodeName, String parentNodeName, int printConfig) {
        super(name);
        this.nodeName = nodeName;
        this.childNodeName = childNodeName;
        this.parentNodeName = parentNodeName;
        this.printConfig = printConfig;
    }

    public HealthMultiRequestData(List<RequestData> requests, String name, String nodeName,
                                  String childNodeName, String parentNodeName, int printConfig) {
        super(requests, name);
        this.nodeName = nodeName;
        this.childNodeName = childNodeName;
        this.parentNodeName = parentNodeName;
        this.printConfig = printConfig;
    }


    @Override
    public Media render(Media media) {

        Renderable r = m -> {
            m = m.print(this.nodeName, this.getName());

            if ((this.printConfig & PRINT_STATUS) > 0) {
                m = m.print("status", Media.ofRenderable(m2 ->
                        m2.print("description", this.getHealthDescription())
                                .print("percentage", this.healthPercentage())).render());
            }

            if ((this.printConfig & PRINT_LOAD) > 0) {
                m = m.print("load", Media.ofRenderable(
                        m2 -> m2.print("requestNumber", this.getRpm())
                                .print("requestTime", Media.ofRenderable(m3 ->
                                        m3.print("average", this.averageRequestDuration())
                                                .print("max10", this.max10RequestDuration())
                                                .print("max1", this.max1RequestDuration())
                                ).render())).render());
            }

            if ((this.printConfig & PRINT_RESULT) > 0) {
                m = m.print("result", this.getResult());
            }

            if (this.childNodeName != null) {
                m = m.forkCollection(this.childNodeName,(Collection) this.requests);
            }

            return m;
        };

        if (this.parentNodeName != null) {
            return  media.forkRenderable(this.parentNodeName, r);
        } else {
            return r.render(media);
        }

    }
}
