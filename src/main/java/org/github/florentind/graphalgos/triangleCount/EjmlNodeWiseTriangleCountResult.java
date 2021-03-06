package org.github.florentind.graphalgos.triangleCount;

import java.util.Arrays;

public class EjmlNodeWiseTriangleCountResult implements TriangleCountResult {
    protected double[] result;

    public EjmlNodeWiseTriangleCountResult(double[] result) {
        this.result = result;
    }

    public long get(int nodeId) {
        double count = result[nodeId];
        assert (count % 1) == 0;
        return (long) count;
    }

    public long totalCount() {
        double total = Arrays.stream(result).sum() / 3;
        assert (total % 1) == 0;
        return (long) total;
    }
}
