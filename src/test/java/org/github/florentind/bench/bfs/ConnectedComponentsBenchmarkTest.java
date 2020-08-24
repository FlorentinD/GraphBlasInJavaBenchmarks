package org.github.florentind.bench.bfs;

import org.junit.jupiter.api.Test;
import org.neo4j.graphalgo.beta.pregel.Pregel;
import org.neo4j.graphalgo.beta.pregel.cc.ConnectedComponentsPregel;
import org.neo4j.graphalgo.beta.pregel.cc.ImmutableConnectedComponentsConfig;
import org.neo4j.graphalgo.core.GdsEdition;
import org.neo4j.graphalgo.core.concurrency.ParallelUtil;
import org.neo4j.graphalgo.core.concurrency.Pools;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;

public class ConnectedComponentsBenchmarkTest extends BaseBenchmarkTest {
    private static final int NODE_COUNT = 30_000;
    private static final int MAX_ITERATIONS = 100;

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
    void wccPregel() {
        GdsEdition.instance().setToEnterpriseEdition();
        int concurrency = 1;

        var config = ImmutableConnectedComponentsConfig.builder()
                .maxIterations(MAX_ITERATIONS)
                .concurrency(concurrency)
                .build();

        int batchSize = (int) ParallelUtil.adjustedBatchSize(graph.nodeCount(), config.concurrency());

        var bfsLevelJob = Pregel.create(
                graph,
                config,
                new ConnectedComponentsPregel(),
                batchSize,
                Pools.DEFAULT,
                AllocationTracker.EMPTY
        );

        var result = bfsLevelJob.run();
        System.out.println("result.ranIterations() = " + result.ranIterations());
    }
}
