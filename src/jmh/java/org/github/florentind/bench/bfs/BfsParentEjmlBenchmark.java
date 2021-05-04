package org.github.florentind.bench.bfs;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.github.florentind.graphalgos.bfs.BfsEjml;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

public class BfsParentEjmlBenchmark extends BfsBaseBenchmark {
    @Override
    protected void benchmarkFunc(Integer concurrency) {
        new BfsEjml().computeDenseSparse(graph.matrix(), BfsEjml.BfsVariation.PARENTS, startNode, MAX_ITERATIONS);
    }
    public static void main(String[] args) {
        new BfsParentEjmlBenchmark().run();
    }
}
