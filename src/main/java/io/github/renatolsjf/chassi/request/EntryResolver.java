package io.github.renatolsjf.chassi.request;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EntryResolver {

    public static final String HTTP_HEADER = "X-CONTEXT-ENTRIES";

    private final String stringRepresentation;
    private final Map<String, String> mapRepresentation;

    public EntryResolver(String stringRepresentation) {
        String s;
        Map<String, String> m;
        try {
            if (stringRepresentation == null || stringRepresentation.isBlank()) {
                m = Collections.emptyMap();
                s = "";
            } else {
                s = stringRepresentation;
                m = this.mapFromString(s);
            }
        } catch (Exception e) {
            m = Collections.emptyMap();
            s = "";
        }
        this.stringRepresentation = s;
        this.mapRepresentation = m;
    }

    public EntryResolver(Map<String, String> mapRepresentation) {
        String s;
        Map<String, String> m;
        try {
            if (mapRepresentation == null) {
                m = Collections.emptyMap();
                s = "";
            } else {
                m = mapRepresentation;
                s = this.stringFormMap(m);
            }
        } catch (Exception e) {
            m = Collections.emptyMap();
            s = "";
        }
        this.stringRepresentation = s;
        this.mapRepresentation = m;
    }

    public String getStringRepresentation() {
        return this.stringRepresentation;
    }

    public Map<String, String> getMapRepresentation() {
        return this.mapRepresentation;
    }

    private Map<String, String> mapFromString(String s) {
        Map<String, String> m = new HashMap<>();
        String[] entries = s.split(";");
        for (String entry: entries) {
            String[] pair = entry.split(":");
            m.put(pair[0], pair[1]);
        }
        return m;
    }

    private String stringFormMap(Map<String, String> m) {
        StringBuilder sb = new StringBuilder("");
        m.entrySet().forEach(e -> sb.append(e.getKey()).append(":").append(e.getValue()).append(";"));
        return sb.toString();
    }

}
