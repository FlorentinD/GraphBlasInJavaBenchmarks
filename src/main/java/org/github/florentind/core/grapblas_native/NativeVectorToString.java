package org.github.florentind.core.grapblas_native;

import com.github.fabianmurariu.unsafe.GRAPHBLAS;

import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;

public class NativeVectorToString {
    public static String doubleVectorToString(Buffer result, int nodeCount) {
        List<String> values = new ArrayList<>();

        for (int i = 0; i < nodeCount; i++) {
            double[] value = GRAPHBLAS.getVectorElementDouble(result, i);
            if (value.length != 0) {
                values.add(Double.toString(value[0]));
            } else {
                values.add("?");
            }
        }

        return String.join(",", values);
    }

    public static String booleanVectorToString(Buffer result, int nodeCount) {
        List<String> values = new ArrayList<>();

        for (int i = 0; i < nodeCount; i++) {
            boolean[] value = GRAPHBLAS.getVectorElementBoolean(result, i);
            if (value.length != 0) {
                values.add(Boolean.toString(value[0]));
            } else {
                values.add("?");
            }
        }

        return String.join(",", values);
    }

    public static String integerVectorToString(Buffer result, int nodeCount) {
        List<String> values = new ArrayList<>();

        for (int i = 0; i < nodeCount; i++) {
            int[] value = GRAPHBLAS.getVectorElementInt(result, i);
            if (value.length != 0) {
                values.add(Integer.toString(value[0]));
            } else {
                values.add("?");
            }
        }

        return String.join(",", values);
    }
}
