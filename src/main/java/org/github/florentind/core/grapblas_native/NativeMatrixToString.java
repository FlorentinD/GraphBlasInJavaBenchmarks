package org.github.florentind.core.grapblas_native;

import com.github.fabianmurariu.unsafe.GRAPHBLAS;

import java.nio.Buffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// FIXME: can be replaced when GxB_Matrix_fprint or generell GxB_print is mapped
public class NativeMatrixToString {
    public static String doubleMatrixToString(Buffer result, int nodeCount) {
        List<List<String>> values = new ArrayList<>();
        int maxValueLength = 1;

        for (int row = 0; row < nodeCount; row++) {
            List<String> rowString = new ArrayList<>();
            values.add(rowString);

            for (int col = 0; col < nodeCount; col++) {
                double[] optional_value = GRAPHBLAS.getMatrixElementDouble(result, row, col);
                if (optional_value.length != 0) {
                    String v = Double.toString(optional_value[0]);
                    maxValueLength = Math.max(maxValueLength, v.length());
                    rowString.add(v);
                } else {
                    rowString.add("?");
                }
            }
        }

        int finalMaxValueLength = maxValueLength;
        return values.stream()
                .map(row -> row.stream()
                        .map(v -> String.format("%1$" + finalMaxValueLength + "s", v))
                        .collect(Collectors.joining(",")))
                .collect(Collectors.joining(System.lineSeparator()));
    }
}
