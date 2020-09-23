package org.github.florentind.graphalgos.triangleCount;

import java.util.Arrays;

public class SparseNodeWiseTriangleCountResult extends NodeWiseTriangleCountResult {
    private final long[] indices;

    public SparseNodeWiseTriangleCountResult(long[] indices, double[] result) {
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

    public long totalCount() {
        double total = Arrays.stream(result).sum() / 3;
        assert (total % 1) == 0;
        return (long) total;
    }
}
