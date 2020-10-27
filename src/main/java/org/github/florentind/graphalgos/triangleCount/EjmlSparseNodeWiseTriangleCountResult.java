package org.github.florentind.graphalgos.triangleCount;

import java.util.Arrays;

public class EjmlSparseNodeWiseTriangleCountResult extends EjmlNodeWiseTriangleCountResult {
    private final long[] indices;

    public EjmlSparseNodeWiseTriangleCountResult(long[] indices, double[] result) {
        super(result);
        this.indices = indices;
    }

    @Override
    public long get(int nodeId) {
        int index = Arrays.binarySearch(indices, nodeId);

        if (index >= 0) {
            double count = result[index];
            assert (count % 1) == 0;
            return (long) count;
        }
        else {
            return 0;
        }
    }
}
