package org.github.florentind.bench.pageRank;


import org.ejml.data.DMatrixSparseCSC;
import org.github.florentind.graphalgos.pageRank.PageRankEjml;

public class PageRankEjmlBenchmark extends PageRankBaseBenchmark {

    DMatrixSparseCSC matrix;

    @Override
    public void setup() {
        super.setup();
        matrix = getAdjacencyMatrix();
    }

    @Override
    protected void benchmarkFunc() {
        new PageRankEjml().compute(matrix, dampingFactor, tolerance, maxIterations);
    }

    public static void main(String[] args) {
        new PageRankEjmlBenchmark().run();
    }
}
