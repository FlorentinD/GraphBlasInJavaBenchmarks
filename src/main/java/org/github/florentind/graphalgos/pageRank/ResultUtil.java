package org.github.florentind.graphalgos.pageRank;

import java.util.Arrays;

public class ResultUtil {
    public static double[] normalize(double[] result) {
        var sum = Arrays.stream(result).sum();
        return Arrays.stream(result).map(x -> x / sum).toArray();
    }
}
