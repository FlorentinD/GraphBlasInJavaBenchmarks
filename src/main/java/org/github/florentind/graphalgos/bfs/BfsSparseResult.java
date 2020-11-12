package org.github.florentind.graphalgos.bfs;

import org.ejml.data.DVectorSparse;

public class BfsSparseResult implements BfsResult {
    private final DVectorSparse result;
    private final double notFoundValue;
    private final int iterations;

    public BfsSparseResult(DVectorSparse result, int iterations, double fallBackValue) {
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
        return this.result.nz_length();
    }

    @Override
    public double get(int nodeId) {
        // to speed up test
        if (!result.isIndicesSorted()) {
            result.sortIndices();
        }

        return result.get(nodeId, notFoundValue);
    }

    @Override
    public boolean visited(int nodeId) {
        return result.isAssigned(nodeId);
    }
}
