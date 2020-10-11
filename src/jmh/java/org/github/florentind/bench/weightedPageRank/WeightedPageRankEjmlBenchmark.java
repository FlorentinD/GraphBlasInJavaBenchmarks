package org.github.florentind.bench.weightedPageRank;


import org.github.florentind.graphalgos.pageRank.PageRankEjml;
import org.openjdk.jmh.infra.Blackhole;

public class WeightedPageRankEjmlBenchmark extends WeightedPageRankBaseBenchmark {
    @org.openjdk.jmh.annotations.Benchmark
    public void ejml(Blackhole bh) {
        bh.consume(new PageRankEjml().computeWeighted(matrix, dampingFactor, tolerance, maxIterations));
    }
}
