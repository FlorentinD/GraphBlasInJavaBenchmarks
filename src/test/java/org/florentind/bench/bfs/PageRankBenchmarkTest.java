package org.florentind.bench.bfs;

import org.junit.jupiter.api.Test;
import org.neo4j.graphalgo.beta.pregel.Pregel;
import org.neo4j.graphalgo.beta.pregel.pr.ImmutablePageRankPregelConfig;
import org.neo4j.graphalgo.beta.pregel.pr.PageRankPregel;
import org.neo4j.graphalgo.core.GdsEdition;
import org.neo4j.graphalgo.core.concurrency.ParallelUtil;
import org.neo4j.graphalgo.core.concurrency.Pools;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;

public class PageRankBenchmarkTest extends BaseBenchmarkTest {
    private static final int NODE_COUNT = 30_000;
    private static final int MAX_ITERATIONS = 30;

    @Override
    long nodeCount() {
        return NODE_COUNT;
    }

    @Override
    long avgDegree() {
        return 4;
    }

    // TODO:   check equal result (on generated graph!?)

    @Test
    void testPregel() {
        // TODO increase test heap space?
        GdsEdition.instance().setToEnterpriseEdition();
        int concurrency = 16;

        var config = ImmutablePageRankPregelConfig.builder()
                .maxIterations(MAX_ITERATIONS)
                .concurrency(concurrency)
                .build();

        int batchSize = (int) ParallelUtil.adjustedBatchSize(graph.nodeCount(), config.concurrency());

        var bfsLevelJob = Pregel.create(
                graph,
                config,
                new PageRankPregel(),
                batchSize,
                Pools.DEFAULT,
                AllocationTracker.EMPTY
        );

        var result = bfsLevelJob.run();
        System.out.println("result.ranIterations() = " + result.ranIterations());
    }
}
