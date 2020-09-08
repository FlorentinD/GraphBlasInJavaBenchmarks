package org.github.florentind.bench.pageRank;


import org.github.florentind.graphalgos.pageRank.PageRankEjml;
import org.openjdk.jmh.infra.Blackhole;

public class PageRankEjmlBenchmark extends PageRankBaseBenchmark {

    // TODO add weighted version of PageRank (e.g. use a relationship property)

    @org.openjdk.jmh.annotations.Benchmark
    public void ejml(Blackhole bh) {
        bh.consume(new PageRankEjml().compute(matrix, dampingFactor, tolerance, maxIterations));
    }
}
