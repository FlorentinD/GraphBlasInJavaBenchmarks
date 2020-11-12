package org.github.florentind.core.grapblas_native;

import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;

import static com.github.fabianmurariu.unsafe.GRAPHBLAS.*;
import static com.github.fabianmurariu.unsafe.GRBCORE.size;

public class NativeVectorToString {
    public static String doubleVectorToString(Buffer result) {
        List<String> values = new ArrayList<>();

        for (int i = 0; i < size(result); i++) {
            double[] value = getVectorElementDouble(result, i);
            if (value.length != 0) {
                values.add(Double.toString(value[0]));
            } else {
                values.add("?");
            }
        }

        return String.join(",", values);
    }

    public static String booleanVectorToString(Buffer result) {
        List<String> values = new ArrayList<>();

        for (int i = 0; i < size(result); i++) {
            boolean[] value = getVectorElementBoolean(result, i);
            if (value.length != 0) {
                values.add(Boolean.toString(value[0]));
            } else {
                values.add("?");
            }
        }

        return String.join(",", values);
    }

    public static String integerVectorToString(Buffer result) {
        List<String> values = new ArrayList<>();

        for (int i = 0; i < size(result); i++) {
            int[] value = getVectorElementInt(result, i);
            if (value.length != 0) {
                values.add(Integer.toString(value[0]));
            } else {
                values.add("?");
            }
        }

        return String.join(",", values);
    }

    public static String longVectorToString(Buffer result) {
        List<String> values = new ArrayList<>();

        for (int i = 0; i < size(result); i++) {
            long[] value = getVectorElementLong(result, i);
            if (value.length != 0) {
                values.add(Long.toString(value[0]));
            } else {
                values.add("?");
            }
        }

        return String.join(",", values);
    }
}
