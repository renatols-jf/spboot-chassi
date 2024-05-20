package io.github.renatolsjf.chassis.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CaseString {

    public enum CaseType {
        KEBAB,
        SNAKE,
        CAMEL,
        PASCAL
    }

    private List<CaseBuffer> buffers = new ArrayList<>();

    private CaseString() {
        this.buffers.add(new KebabCaseBuffer());
        this.buffers.add(new SnakeCaseBuffer());
        this.buffers.add(new CamelCaseBuffer());
        this.buffers.add(new PascalCaseBuffer());
    }

    //TODO add support for objects in general other than map
    public <V> V getMapValue(Map<String, V> map) {
        return this.buffers.stream()
                .map(cs -> map.get(cs.toString()))
                .filter(s -> s!= null)
                .findFirst()
                .orElse(null);
    }

    public boolean matches (String s) {
        return this.getMatchingValues().contains(s);
    }

    public List<String> getMatchingValues() {
        return this.buffers.stream()
                .map(cs -> cs.toString())
                .distinct()
                .collect(Collectors.toList());
    }

    public String getValue(CaseType type) {
        return this.buffers.stream()
                .filter(cs -> cs.isOfCaseType(type))
                .map(cs -> cs.toString())
                .findFirst()
                .orElse(null);
    }

    public static CaseString parse(String s) {

        if (s == null) {
            throw new NullPointerException();
        }

        CaseString cs = new CaseString();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            cs.buffers.forEach(b -> b.next(c));
        }

        return cs;

    }

    public static String getValue(CaseType caseType, String s) {
        CaseString cs = parse(s);
        return cs.getValue(caseType);
    }

}

abstract class CaseBuffer {

    protected StringBuffer valueBuffer = new StringBuffer();
    protected List<Character> supportedDelimiters = new ArrayList<>();
    protected boolean lastCharWasDelimiter = false;
    protected CaseString.CaseType type;

    CaseBuffer(CaseString.CaseType type) {
        this.type = type;
        this.supportedDelimiters.add('-');
        this.supportedDelimiters.add('_');
    }

    void next(char c) {
        if (this.supportedDelimiters.contains(c)) {
            if (this.valueBuffer.isEmpty() || this.lastCharWasDelimiter) {
                return;
            } else {
                this.lastCharWasDelimiter = true;
            }
        } else if(Character.isUpperCase(c)) {
            this.appendUpperCase(c);
            this.lastCharWasDelimiter = false;
        } else {
            this.appendLowerCase(c);
            this.lastCharWasDelimiter = false;
        }
    }

    abstract void appendUpperCase(char c);
    abstract void appendLowerCase(char c);

    String bufferValue() {
        return this.valueBuffer.toString();
    }

    @Override
    public String toString() {
        return this.bufferValue();
    }

    public boolean isOfCaseType(CaseString.CaseType type) {
        return this.type.equals(type);
    }

}

abstract class SeparatorCaseBuffer extends CaseBuffer {

    protected String selectedDelimiter;

    SeparatorCaseBuffer(CaseString.CaseType type, String selectedDelimiter) {
        super(type);
        this.selectedDelimiter = selectedDelimiter;
    }

    @Override
    void appendUpperCase(char c) {
        if(!(this.valueBuffer.isEmpty()) && this.selectedDelimiter != null) {
            this.valueBuffer.append(this.selectedDelimiter);
        }
        this.valueBuffer.append(Character.toLowerCase(c));
    }

    @Override
    void appendLowerCase(char c) {
        if (this.lastCharWasDelimiter && this.selectedDelimiter != null) {
            this.valueBuffer.append(this.selectedDelimiter);
        }
        this.valueBuffer.append(c);
    }

}

class KebabCaseBuffer extends SeparatorCaseBuffer {
    KebabCaseBuffer() {
        super(CaseString.CaseType.KEBAB, "-");
    }
}

class SnakeCaseBuffer extends SeparatorCaseBuffer {
    SnakeCaseBuffer() {
        super(CaseString.CaseType.SNAKE, "_");
    }
}

class CamelCaseBuffer extends CaseBuffer {

    CamelCaseBuffer() {
        super(CaseString.CaseType.CAMEL);
    }

    @Override
    void appendUpperCase(char c) {
        if(!(this.valueBuffer.isEmpty())) {
            this.valueBuffer.append(c);
        } else {
            this.valueBuffer.append(Character.toLowerCase(c));
        }

    }

    @Override
    void appendLowerCase(char c) {
        if (this.lastCharWasDelimiter) {
            this.valueBuffer.append(Character.toUpperCase(c));
        } else {
            this.valueBuffer.append(c);
        }
    }

}

class PascalCaseBuffer extends CaseBuffer {

    PascalCaseBuffer() {
        super(CaseString.CaseType.PASCAL);
    }

    @Override
    void appendUpperCase(char c) {
        this.valueBuffer.append(c);
    }

    @Override
    void appendLowerCase(char c) {
        if (this.lastCharWasDelimiter || this.valueBuffer.isEmpty()) {
            this.valueBuffer.append(Character.toUpperCase(c));
        } else {
            this.valueBuffer.append(c);
        }
    }

}
