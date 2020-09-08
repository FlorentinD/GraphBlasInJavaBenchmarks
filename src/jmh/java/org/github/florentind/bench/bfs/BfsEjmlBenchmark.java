package org.github.florentind.bench.bfs;

import org.github.florentind.graphalgos.bfs.BfsEjml;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.infra.Blackhole;

public class BfsEjmlBenchmark extends BfsBaseBenchmark {

    @Param({"1"})
    private int concurrency;


// FIXME enable when bfs bug is fixed ( and result is comparable)

//    @org.openjdk.jmh.annotations.Benchmark
//    public void gdsBfsTraverse(Blackhole bh) {
//        // Not really level/parent but just returns a list of visited nodes
//        Traverse algorithm = Traverse.bfs(graph, startNode, (s, t, w) -> Traverse.ExitPredicate.Result.FOLLOW, Traverse.DEFAULT_AGGREGATOR);
//
//        bh.consume(algorithm.compute());
//    }

    @Benchmark
    public void ejmlSparseBfsBoolean(Blackhole bh) {
        int[] startNodes = {startNode};
        bh.consume(new BfsEjml().computeSparse(matrix, BfsEjml.BfsVariation.BOOLEAN, startNodes, maxIterations));
    }

    @Benchmark
    public void ejmlSparseBfsLevel(Blackhole bh) {
        int[] startNodes = {startNode};
        bh.consume(new BfsEjml().computeSparse(matrix, BfsEjml.BfsVariation.LEVEL, startNodes, maxIterations));
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void ejmlSparseBfsParent(Blackhole bh) {
        int[] startNodes = {startNode};
        bh.consume(new BfsEjml().computeSparse(matrix, BfsEjml.BfsVariation.PARENTS, startNodes, maxIterations));
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void ejmlDenseBfsBoolean(Blackhole bh) {
        bh.consume(new BfsEjml().computeDense(matrix, BfsEjml.BfsVariation.BOOLEAN, startNode, maxIterations));
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void ejmlDenseBfsLevel(Blackhole bh) {
        bh.consume(new BfsEjml().computeDense(matrix, BfsEjml.BfsVariation.LEVEL, startNode, maxIterations));
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void ejmlDenseBfsParent(Blackhole bh) {
        bh.consume(new BfsEjml().computeDense(matrix, BfsEjml.BfsVariation.PARENTS, startNode, maxIterations));
    }

    // TODO add MSBFS benchmark
}
