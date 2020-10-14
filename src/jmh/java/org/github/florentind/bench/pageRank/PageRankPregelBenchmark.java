package org.github.florentind.bench.pageRank;


import org.neo4j.graphalgo.beta.pregel.Pregel;
import org.neo4j.graphalgo.beta.pregel.pr.ImmutablePageRankPregelConfig;
import org.neo4j.graphalgo.beta.pregel.pr.PageRankPregel;
import org.neo4j.graphalgo.core.concurrency.Pools;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.infra.Blackhole;

public class PageRankPregelBenchmark extends PageRankBaseBenchmark {

    @Param({"1", "8"})
    private int concurrency;

    // as Pregel is to slow for 3 million nodes
    @Param({"300000"})
    int nodeCount;

    private PageRankPregel.PageRankPregelConfig config;

    private Pregel<PageRankPregel.PageRankPregelConfig> pregel;

    @Override
    public void setup() {
        super.setup();

        config = ImmutablePageRankPregelConfig.builder()
                .maxIterations(maxIterations)
                .dampingFactor(dampingFactor)
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
}