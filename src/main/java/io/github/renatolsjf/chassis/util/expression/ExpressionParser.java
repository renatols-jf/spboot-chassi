package io.github.renatolsjf.chassis.util.expression;

import io.github.renatolsjf.chassis.util.CaseString;
import io.github.renatolsjf.chassis.util.genesis.ExtractedMember;
import io.github.renatolsjf.chassis.util.genesis.NoAdequateMemberException;
import io.github.renatolsjf.chassis.util.genesis.ObjectExtractor;
import io.github.renatolsjf.chassis.util.genesis.UnableToGetMemberException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ExpressionParser {

    public static Object parse(Object obj) {

        if (obj == null) {
            return null;
        }

        if (obj instanceof Collection<?> c) {
            List<Object> l = new ArrayList<>();
            for (Object o: c) {
                l.add(ExpressionParser.parse(o));
            }
            return l;
        } else if (obj.getClass().isArray()) {
            List<Object> l = new ArrayList<>();
            for (Object o: (Object[]) obj) {
                l.add(ExpressionParser.parse(o));
            }
            return l;
        }

        if (!(obj instanceof String)) {
            return obj;
        }

        String expression = (String) obj;
        if (!expression.startsWith("$")) {
            return expression;
        }

        String[] parts = expression.split("\\.");

        Expression root;
        try {
            Class<Expression> type = (Class<Expression>) Class.forName("io.github.renatolsjf.chassis.util.expression."
                    + CaseString.getValue(CaseString.CaseType.PASCAL, parts[0].substring(1)) + "Expression");
            Constructor<Expression> c = type.getConstructor();
            root = c.newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            return expression;
        }

        Object toReturn = root.value();
        if (parts.length == 1) {
            return toReturn;
        }

        for (String part : Arrays.copyOfRange(parts, 1, parts.length)) {

            ObjectExtractor extractor = new ObjectExtractor<>(toReturn);
            ExtractedMember member = extractor.methodExtractor().getter()
                    .withName(part).mostAdequateOrNull();
            if (member == null) {
                try {
                    extractor.fieldExtractor().withName(part).mostAdequate();
                } catch (NoAdequateMemberException e) {
                    return null;
                }
            }

            try {
                toReturn = member.callOrGet();
            } catch (UnableToGetMemberException e) {
                return null;
            }

            if (toReturn == null) {
                break;
            }

        }

        return toReturn;
    }

}
