package org.github.florentind.bench.weightedPageRank;


import org.ejml.data.DMatrixSparseCSC;
import org.github.florentind.graphalgos.pageRank.PageRankEjml;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

public class WeightedPageRankEjmlBenchmark extends WeightedPageRankBaseBenchmark {

    DMatrixSparseCSC matrix;

    @Override
    @Setup
    public void setup() {
        super.setup();

        matrix = getAdjacencyMatrix();
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void ejml(Blackhole bh) {
        bh.consume(new PageRankEjml().computeWeighted(matrix, dampingFactor, tolerance, maxIterations));
    }
}
