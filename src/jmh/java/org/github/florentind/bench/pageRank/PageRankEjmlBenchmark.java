package org.github.florentind.bench.pageRank;


import org.ejml.sparse.csc.graphAlgos.PageRank_DSCC;
import org.openjdk.jmh.infra.Blackhole;

public class PageRankEjmlBenchmark extends PageRankBaseBenchmark {

    // TODO add weighted version of PageRank (e.g. use a relationship property)

    @org.openjdk.jmh.annotations.Benchmark
    public void computeEjml(Blackhole bh) {
        bh.consume(new PageRank_DSCC().compute(matrix, dampingFactor, tolerance, maxIterations));
    }
}
