package org.github.florentind.bench.pageRank;


import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.neo4j.graphalgo.pagerank.ImmutablePageRankStatsConfig;
import org.neo4j.graphalgo.pagerank.PageRank;
import org.neo4j.graphalgo.pagerank.PageRankBaseConfig;
import org.neo4j.graphalgo.pagerank.PageRankFactory;
import org.neo4j.logging.NullLog;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.infra.Blackhole;

public class PageRankGdsBenchmark extends PageRankBaseBenchmark {

    @Param({"1", "8"})
    private int concurrency;

    private PageRankBaseConfig unweightedConfig;

    @Override
    public void setup() {
        super.setup();

        unweightedConfig = ImmutablePageRankStatsConfig
                .builder()
                .maxIterations(maxIterations)
                .dampingFactor(dampingFactor)
                .tolerance(tolerance)
                .concurrency(concurrency)
                .build();
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void gds(Blackhole bh) {
        PageRank algorithm = new PageRankFactory<>().build(
                graph,
                unweightedConfig,
                AllocationTracker.empty(),
                NullLog.getInstance()
        );

        bh.consume(algorithm.compute());
    }
}
