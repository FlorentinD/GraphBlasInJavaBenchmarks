package org.github.florentind.graphalgos.bfs;

import org.ejml.data.DMatrixSparseCSC;

public class BfsSparseResult implements BfsResult {
    private final DMatrixSparseCSC result;
    private final double notFoundValue;
    private final int iterations;

    public BfsSparseResult(DMatrixSparseCSC result, int iterations, double fallBackValue) {
        this.result = result;
        this.iterations = iterations;
        notFoundValue = fallBackValue;
    }

    @Override
    public int iterations() {
        return this.iterations;
    }

    @Override
    public int nodesVisited() {
        return this.result.getNonZeroLength();
    }

    @Override
    public double get(int nodeId) {
        return result.get(0, nodeId, notFoundValue);
    }

    @Override
    public boolean visited(int nodeId) {
        return result.isAssigned(0, nodeId);
    }

    public DMatrixSparseCSC result() {
        return this.result;
    }
}
