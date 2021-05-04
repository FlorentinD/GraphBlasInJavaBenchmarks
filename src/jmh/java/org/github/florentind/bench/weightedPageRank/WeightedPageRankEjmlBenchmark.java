package org.github.florentind.bench.weightedPageRank;


import org.ejml.data.DMatrixSparseCSC;
import org.github.florentind.graphalgos.pageRank.PageRankEjml;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

public class WeightedPageRankEjmlBenchmark extends WeightedPageRankBaseBenchmark {

    DMatrixSparseCSC matrix;

    @Override
    public void setup(String dataset) {
        super.setup(dataset);

        matrix = getAdjacencyMatrix();
    }

    @Override
    protected void benchmarkFunc(Integer concurrency) {
        new PageRankEjml().computeWeighted(matrix, dampingFactor, tolerance, maxIterations);
    }
}
