package org.florentind.bench.pageRank;


import org.florentind.bench.bfs.BfsBaseBenchmark;
import org.neo4j.graphalgo.beta.pregel.Pregel;
import org.neo4j.graphalgo.beta.pregel.pr.ImmutablePageRankPregelConfig;
import org.neo4j.graphalgo.beta.pregel.pr.PageRankPregel;
import org.neo4j.graphalgo.core.concurrency.ParallelUtil;
import org.neo4j.graphalgo.core.concurrency.Pools;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.infra.Blackhole;

public class PageRankGdsBenchmark extends BfsBaseBenchmark {

    @Param({"1", "8"})
    private int concurrency;

    private PageRankPregel.PageRankPregelConfig config;

    private int batchSize;

    private Pregel<PageRankPregel.PageRankPregelConfig> pregel;

    @Override
    public void setup() {
        super.setup();

        config = ImmutablePageRankPregelConfig.builder()
                .maxIterations(maxIterations)
                .concurrency(concurrency)
                .build();

        batchSize = (int) ParallelUtil.adjustedBatchSize(graph.nodeCount(), config.concurrency());

        // init Pregel structures beforehand
        pregel = Pregel.create(
                graph,
                config,
                new PageRankPregel(),
                batchSize,
                Pools.DEFAULT,
                AllocationTracker.EMPTY
        );
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void pregelPageRank(Blackhole bh) {
        bh.consume(pregel.run());
    }
}
