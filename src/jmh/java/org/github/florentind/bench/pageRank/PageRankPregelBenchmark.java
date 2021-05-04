package org.github.florentind.bench.pageRank;


import org.neo4j.graphalgo.beta.pregel.Partitioning;
import org.neo4j.graphalgo.beta.pregel.Pregel;
import org.neo4j.graphalgo.beta.pregel.pr.ImmutablePageRankPregelConfig;
import org.neo4j.graphalgo.beta.pregel.pr.PageRankPregel;
import org.neo4j.graphalgo.core.concurrency.Pools;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;

import java.util.List;

public class PageRankPregelBenchmark extends PageRankBaseBenchmark {

    @Override
    protected List<Integer> concurrencies() {
        return List.of(1, 8);
    }

    private PageRankPregel.PageRankPregelConfig config;

    private Pregel<PageRankPregel.PageRankPregelConfig> pregel;

    @Override
    public void setup(String dataset) {
        super.setup(dataset);
    }

    @Override
    protected void benchmarkFunc(Integer concurrency) {
        config = ImmutablePageRankPregelConfig.builder()
                .maxIterations(maxIterations)
                .dampingFactor(dampingFactor)
                .partitioning(Partitioning.DEGREE)
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

        pregel.run();
    }

    public static void main(String[] args) {
        new PageRankPregelBenchmark().run();
    }
}
