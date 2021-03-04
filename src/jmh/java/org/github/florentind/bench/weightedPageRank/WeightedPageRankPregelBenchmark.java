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

import java.util.List;

public class WeightedPageRankPregelBenchmark extends WeightedPageRankBaseBenchmark {


    @Override
    protected List<Integer> concurrencies() {
        return List.of(1,8);
    }

    private PageRankPregel.PageRankPregelConfig config;

    private Pregel<PageRankPregel.PageRankPregelConfig> pregel;

    @Override
    @Setup
    public void setup(String dataset) {
        super.setup(dataset);

        // as Pregel implementation has no good way to normalize the weights
        EjmlUtil.normalizeOutgoingWeights(graph);
    }

    @Override
    protected void benchmarkFunc(Integer concurrency) {
        config = ImmutablePageRankPregelConfig.builder()
                .maxIterations(maxIterations)
                .dampingFactor(dampingFactor)
                .concurrency(concurrency)
                .relationshipWeightProperty(REL_PROPERTY_NAME)
                .build();

        // init Pregel structures beforehand
        pregel = Pregel.create(
                graph,
                config,
                new PageRankPregel(),
                Pools.DEFAULT,
                AllocationTracker.empty()
        );

        pregel.run();
    }
}
