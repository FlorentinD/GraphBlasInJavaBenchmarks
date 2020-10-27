package org.github.florentind.graphalgos.triangleCount;

public interface TriangleCountResult {
    long get(int nodeId);

    long totalCount();
}
