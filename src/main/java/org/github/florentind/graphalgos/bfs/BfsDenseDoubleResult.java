package org.github.florentind.graphalgos.bfs;

public class BfsDenseDoubleResult implements BfsResult {
    private final double[] result;
    private final double notFoundValue;
    private final int iterations;

    public BfsDenseDoubleResult(double[] result, int iterations, double notFoundValue) {
        this.result = result;
        this.iterations = iterations;
        this.notFoundValue = notFoundValue;
    }

    @Override
    public int iterations() {
        return this.iterations;
    }

    @Override
    public int nodesVisited() {
        int visited = 0;

        for (double v : result) {
            if (v != notFoundValue) visited++;
        }

        return visited;
    }

    @Override
    public double get(int nodeId) {
        return result[nodeId];
    }

    public double[] result() {
        return this.result;
    }
}
