package org.github.florentind.graphalgos.triangleCount;

import java.util.Arrays;

public class NativeNodeWiseTriangleCountResult implements TriangleCountResult {
    protected long[] result;

    public NativeNodeWiseTriangleCountResult(long[] result) {
        this.result = result;
    }

    public long get(int nodeId) {
        return result[nodeId];
    }

    public long totalCount() {
        return Arrays.stream(result).sum() / 3;
    }
}
