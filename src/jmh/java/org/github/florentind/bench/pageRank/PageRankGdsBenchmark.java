package org.github.florentind.bench.pageRank;


import org.neo4j.graphalgo.beta.pregel.Pregel;
import org.neo4j.graphalgo.beta.pregel.pr.ImmutablePageRankPregelConfig;
import org.neo4j.graphalgo.beta.pregel.pr.PageRankPregel;
import org.neo4j.graphalgo.core.concurrency.Pools;
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

    private PageRankPregel.PageRankPregelConfig config;

    private Pregel<PageRankPregel.PageRankPregelConfig> pregel;

    private PageRankBaseConfig unweightedConfig;

    // TODO add weighted version of PageRank (e.g. use a relationship property)

    @Override
    public void setup() {
        super.setup();

        config = ImmutablePageRankPregelConfig.builder()
                .maxIterations(maxIterations)
                .dampingFactor(dampingFactor)
                .concurrency(concurrency)
                .build();

        unweightedConfig = ImmutablePageRankStatsConfig
                .builder()
                .concurrency(concurrency)
                .build();

        // init Pregel structures beforehand
        pregel = Pregel.create(
                graph,
                config,
                new PageRankPregel(),
                Pools.DEFAULT,
                AllocationTracker.empty()
        );
    }

    // TODO: add tolerance feature to Pregel (otherwise Pregel has an advantage)
    // TODO: also normalize result as in ejml version done automatically?
    @org.openjdk.jmh.annotations.Benchmark
    public void pregel(Blackhole bh) {
        bh.consume(pregel.run());
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void gdsUnweighted(Blackhole bh) {
        PageRank algorithm = new PageRankFactory<>().build(
                graph,
                unweightedConfig,
                AllocationTracker.empty(),
                NullLog.getInstance()
        );

        bh.consume(algorithm.compute());
    }
}
