package org.github.florentind.graphalgos.triangleCount;

import java.util.Arrays;

public class NativeSparseNodeWiseTriangleCountResult extends NativeNodeWiseTriangleCountResult {
    private final long[] indices;

    public NativeSparseNodeWiseTriangleCountResult(long[] indices, long[] result) {
        super(result);
        this.indices = indices;
    }

    @Override
    public long get(int nodeId) {
        int index = Arrays.binarySearch(indices, nodeId);

        if (index >= 0) {
            return result[index];
        }
        else {
            return 0;
        }
    }
}
