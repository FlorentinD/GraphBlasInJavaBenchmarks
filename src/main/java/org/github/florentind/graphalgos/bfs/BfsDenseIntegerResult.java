package org.github.florentind.graphalgos.bfs;

public class BfsDenseIntegerResult implements BfsResult {
    private final int[] values;
    private final int iterations;
    private final int notFoundValue;

    public BfsDenseIntegerResult(int[] values, int iterations, int notFoundValue) {
        this.values = values;
        this.iterations = iterations;
        // for now only LEVEL variant exists
        this.notFoundValue = notFoundValue;
    }

    @Override
    public int iterations() {
        return this.iterations;
    }

    @Override
    public int nodesVisited() {
        // works as the result vector is sparse
        return this.values.length;
    }

    @Override
    public double get(int nodeId) {
        return values[nodeId];
    }
}
