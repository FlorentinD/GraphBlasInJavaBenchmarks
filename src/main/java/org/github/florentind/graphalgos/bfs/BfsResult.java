package org.github.florentind.graphalgos.bfs;

public interface BfsResult {
    int iterations();

    int nodesVisited();

    double get(int nodeId);
}
