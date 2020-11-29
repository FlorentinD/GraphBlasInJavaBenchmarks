package org.github.florentind.bench.weightedPageRank;


import org.github.florentind.core.ejml.EjmlUtil;
import org.neo4j.graphalgo.beta.pregel.Pregel;
import org.neo4j.graphalgo.beta.pregel.pr.ImmutablePageRankPregelConfig;
import org.neo4j.graphalgo.beta.pregel.pr.PageRankPregel;
import org.neo4j.graphalgo.core.concurrency.Pools;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

public class WeightedPageRankPregelBenchmark extends WeightedPageRankBaseBenchmark {

    @Param({"1", "8"})
    private int concurrency;

    // skipping 5M version (takes too long)
    @Param({"0.1M;20", "0.5M;20", "1M;20", "1M;5", "1M;10", "1M;15"})
    protected String nodeCountIterationCombinations;

    private PageRankPregel.PageRankPregelConfig config;

    private Pregel<PageRankPregel.PageRankPregelConfig> pregel;

    @Override
    @Setup
    public void setup() {
        super.setup();

        config = ImmutablePageRankPregelConfig.builder()
                .maxIterations(maxIterations)
                .dampingFactor(dampingFactor)
                .concurrency(concurrency)
                .relationshipWeightProperty(REL_PROPERTY_NAME)
                .build();

        // as Pregel implementation has no good way to normalize the weights
        EjmlUtil.normalizeOutgoingWeights(graph);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void pregel(Blackhole bh) {
        // init Pregel structures beforehand
        pregel = Pregel.create(
                graph,
                config,
                new PageRankPregel(),
                Pools.DEFAULT,
                AllocationTracker.empty()
        );

        bh.consume(pregel.run());
    }
}
