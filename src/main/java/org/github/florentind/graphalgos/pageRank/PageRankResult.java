package org.github.florentind.graphalgos.pageRank;

public class PageRankResult {
    private final double[] result;
    private final int iterations;

    public PageRankResult(double[] result, int iterations) {
        this.result = result;
        this.iterations = iterations;
    }

    public double[] result() {
        return result;
    }

    public int iterations() {
        return iterations;
    }
}
