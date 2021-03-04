package org.github.florentind.bench.pageRank;


import org.ejml.data.DMatrixSparseCSC;
import org.github.florentind.graphalgos.pageRank.PageRankEjml;

public class PageRankEjmlBenchmark extends PageRankBaseBenchmark {

    DMatrixSparseCSC matrix;

    @Override
    public void setup(String dataset) {
        super.setup(dataset);
        matrix = getAdjacencyMatrix();
    }

    @Override
    protected void benchmarkFunc(Integer concurrency) {
        new PageRankEjml().compute(matrix, dampingFactor, tolerance, maxIterations);
    }

    public static void main(String[] args) {
        new PageRankEjmlBenchmark().run();
    }
}
