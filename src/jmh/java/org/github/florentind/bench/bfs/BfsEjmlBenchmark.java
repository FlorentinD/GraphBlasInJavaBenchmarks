package org.github.florentind.bench.bfs;

import org.ejml.data.DMatrixSparseCSC;
import org.github.florentind.graphalgos.bfs.BfsEjml;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

public class BfsEjmlBenchmark extends BfsBaseBenchmark {

    @Param({"1"})
    private int concurrency;

    private DMatrixSparseCSC transposedMatrix;

    @Override
    @Setup
    public void setup() {
        super.setup();
        transposedMatrix = graph.matrix();
    }

    @Benchmark
    public void ejmlSparseBfsLevel(Blackhole bh) {
        bh.consume(new BfsEjml().computeSparse(transposedMatrix, BfsEjml.BfsVariation.LEVEL, startNode, maxIterations));
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void ejmlSparseBfsParent(Blackhole bh) {
        bh.consume(new BfsEjml().computeSparse(transposedMatrix, BfsEjml.BfsVariation.PARENTS, startNode, maxIterations));
    }

    @Benchmark
    public void ejmlDenseSparseBfsLevel(Blackhole bh) {
        bh.consume(new BfsEjml().computeDenseSparse(transposedMatrix, BfsEjml.BfsVariation.LEVEL, startNode, maxIterations));
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void ejmlDenseSparseBfsParent(Blackhole bh) {
        bh.consume(new BfsEjml().computeDenseSparse(transposedMatrix, BfsEjml.BfsVariation.PARENTS, startNode, maxIterations));
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void ejmlDenseBfsLevel(Blackhole bh) {
        bh.consume(new BfsEjml().computeDense(transposedMatrix, BfsEjml.BfsVariation.LEVEL, startNode, maxIterations));
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void ejmlDenseBfsParent(Blackhole bh) {
        bh.consume(new BfsEjml().computeDense(transposedMatrix, BfsEjml.BfsVariation.PARENTS, startNode, maxIterations));
    }

    // TODO add MSBFS benchmark
}
