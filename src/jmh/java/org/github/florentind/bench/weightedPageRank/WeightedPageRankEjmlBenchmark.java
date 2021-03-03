package org.github.florentind.bench.weightedPageRank;


import org.ejml.data.DMatrixSparseCSC;
import org.github.florentind.graphalgos.pageRank.PageRankEjml;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

public class WeightedPageRankEjmlBenchmark extends WeightedPageRankBaseBenchmark {

    DMatrixSparseCSC matrix;

    @Override
    public void setup() {
        super.setup();

        matrix = getAdjacencyMatrix();
    }

    @Override
    protected void benchmarkFunc() {
        new PageRankEjml().computeWeighted(matrix, dampingFactor, tolerance, maxIterations);
    }
}
