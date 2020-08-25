package org.github.florentind.bench.bfs;


import org.ejml.sparse.csc.graphAlgos.Bfs_DSCC;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.infra.Blackhole;

public class BfsEjmlBenchmark extends BfsBaseBenchmark {

    @Param({"1"})
    private int concurrency;


// FIXME enable when bfs bug is fixed

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
        bh.consume(new Bfs_DSCC().computeSparse(matrix, Bfs_DSCC.BfsVariation.BOOLEAN, startNodes, maxIterations));
    }

    @Benchmark
    public void ejmlSparseBfsLevel(Blackhole bh) {
        int[] startNodes = {startNode};
        bh.consume(new Bfs_DSCC().computeSparse(matrix, Bfs_DSCC.BfsVariation.LEVEL, startNodes, maxIterations));
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void ejmlSparseBfsParent(Blackhole bh) {
        int[] startNodes = {startNode};
        bh.consume(new Bfs_DSCC().computeSparse(matrix, Bfs_DSCC.BfsVariation.PARENTS, startNodes, maxIterations));
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void ejmlDenseBfsBoolean(Blackhole bh) {
        bh.consume(new Bfs_DSCC().computeDense(matrix, Bfs_DSCC.BfsVariation.BOOLEAN, startNode, maxIterations));
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void ejmlDenseBfsLevel(Blackhole bh) {
        bh.consume(new Bfs_DSCC().computeDense(matrix, Bfs_DSCC.BfsVariation.LEVEL, startNode, maxIterations));
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void ejmlDenseBfsParent(Blackhole bh) {
        bh.consume(new Bfs_DSCC().computeDense(matrix, Bfs_DSCC.BfsVariation.PARENTS, startNode, maxIterations));
    }

    // TODO add MSBFS benchmark
}
