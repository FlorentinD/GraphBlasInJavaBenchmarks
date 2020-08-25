package org.github.florentind.bench;

import org.junit.jupiter.api.Test;
import org.neo4j.graphalgo.beta.pregel.Pregel;
import org.neo4j.graphalgo.beta.pregel.cc.ConnectedComponentsPregel;
import org.neo4j.graphalgo.beta.pregel.cc.ImmutableConnectedComponentsConfig;
import org.neo4j.graphalgo.core.GdsEdition;
import org.neo4j.graphalgo.core.concurrency.ParallelUtil;
import org.neo4j.graphalgo.core.concurrency.Pools;
import org.neo4j.graphalgo.core.utils.ProgressLogger;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.neo4j.graphalgo.core.utils.paged.dss.DisjointSetStruct;
import org.neo4j.graphalgo.wcc.ImmutableWccStreamConfig;
import org.neo4j.graphalgo.wcc.Wcc;
import org.neo4j.graphalgo.wcc.WccStreamConfig;

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

        var connectedComponentsJob = Pregel.create(
                graph,
                config,
                new ConnectedComponentsPregel(),
                batchSize,
                Pools.DEFAULT,
                AllocationTracker.EMPTY
        );

        var result = connectedComponentsJob.run();
        System.out.println("result.ranIterations() = " + result.ranIterations());
    }

    @Test
    void unweightedWccGds() {
        WccStreamConfig config = ImmutableWccStreamConfig.builder().build();

        DisjointSetStruct result = new Wcc(
                graph,
                Pools.DEFAULT,
                ParallelUtil.DEFAULT_BATCH_SIZE,
                config,
                ProgressLogger.NULL_LOGGER,
                AllocationTracker.EMPTY
        ).compute();

        System.out.println(result.setIdOf(NODE_COUNT - 1));
        // TODO assert against ejml variation (ranIterations and result)
    }
}
