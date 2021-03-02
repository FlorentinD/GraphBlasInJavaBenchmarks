package org.github.florentind.bench.pageRank;


import org.ejml.data.DMatrixSparseCSC;
import org.github.florentind.graphalgos.pageRank.PageRankEjml;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

public class PageRankEjmlBenchmark extends PageRankBaseBenchmark {

    DMatrixSparseCSC matrix;

    @Override
    @Setup
    public void setup() {
        super.setup();
        matrix = getAdjacencyMatrix();
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void ejml(Blackhole bh) {
        bh.consume(new PageRankEjml().compute(matrix, dampingFactor, tolerance, maxIterations));
    }
}
