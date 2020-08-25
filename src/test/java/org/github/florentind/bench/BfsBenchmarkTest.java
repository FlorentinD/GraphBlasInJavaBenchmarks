package org.github.florentind.bench;

import org.junit.jupiter.api.Test;
import org.neo4j.graphalgo.beta.pregel.Pregel;
import org.neo4j.graphalgo.beta.pregel.bfs.BFSLevelPregel;
import org.neo4j.graphalgo.beta.pregel.bfs.BFSPregelConfig;
import org.neo4j.graphalgo.beta.pregel.bfs.ImmutableBFSPregelConfig;
import org.neo4j.graphalgo.core.GdsEdition;
import org.neo4j.graphalgo.core.GraphDimensions;
import org.neo4j.graphalgo.core.ImmutableGraphDimensions;
import org.neo4j.graphalgo.core.concurrency.ParallelUtil;
import org.neo4j.graphalgo.core.concurrency.Pools;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;

public class BfsBenchmarkTest extends BaseBenchmarkTest {
    private static final int NODE_COUNT = 300_000;
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
    void testPregel() {
        GraphDimensions dim = ImmutableGraphDimensions.builder().nodeCount(NODE_COUNT).maxRelCount(4 * NODE_COUNT).build();
        System.out.println("memoryEstimation = " + Pregel.memoryEstimation().estimate(dim, 4).render());

        // TODO increase test heap space?
        GdsEdition.instance().setToEnterpriseEdition();
        int concurrency = 16;
        int startNode = 0;
        BFSPregelConfig config = ImmutableBFSPregelConfig.builder()
                .maxIterations(MAX_ITERATIONS)
                .startNode(startNode)
                .concurrency(concurrency)
                .build();

        int batchSize = (int) ParallelUtil.adjustedBatchSize(graph.nodeCount(), config.concurrency());

        Pregel<BFSPregelConfig> bfsLevelJob = Pregel.create(
                graph,
                config,
                new BFSLevelPregel(),
                batchSize,
                Pools.DEFAULT,
                AllocationTracker.EMPTY
        );

        var result = bfsLevelJob.run();
        System.out.println("result.ranIterations() = " + result.ranIterations());
    }
}
