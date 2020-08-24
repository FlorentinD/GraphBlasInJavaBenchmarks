package org.florentind.bench.bfs;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.neo4j.graphalgo.api.CSRGraph;
import org.neo4j.graphalgo.api.Graph;
import org.neo4j.graphalgo.beta.generator.RandomGraphGenerator;
import org.neo4j.graphalgo.beta.generator.RelationshipDistribution;
import org.neo4j.graphalgo.beta.pregel.Pregel;
import org.neo4j.graphalgo.beta.pregel.bfs.BFSLevelPregel;
import org.neo4j.graphalgo.beta.pregel.bfs.BFSPregelConfig;
import org.neo4j.graphalgo.beta.pregel.bfs.ImmutableBFSPregelConfig;
import org.neo4j.graphalgo.config.RandomGraphGeneratorConfig;
import org.neo4j.graphalgo.core.Aggregation;
import org.neo4j.graphalgo.core.GdsEdition;
import org.neo4j.graphalgo.core.GraphDimensions;
import org.neo4j.graphalgo.core.ImmutableGraphDimensions;
import org.neo4j.graphalgo.core.concurrency.ParallelUtil;
import org.neo4j.graphalgo.core.concurrency.Pools;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;

public class BfsBenchmarkTest {
    private static final int NODE_COUNT = 3_000_000;
    private static final int AVG_DEGREE = 4;
    private static final int MAX_ITERATIONS = 100;

    // TODO:   check equal result (on generated graph!?)

    CSRGraph getGraph(long nodeCount, long avgDegree) {
        return RandomGraphGenerator.builder()
                .nodeCount(nodeCount)
                .averageDegree(avgDegree)
                .seed(42L)
                .aggregation(Aggregation.MAX)
                .allocationTracker(AllocationTracker.EMPTY)
                .allowSelfLoops(RandomGraphGeneratorConfig.AllowSelfLoops.YES)
                .relationshipDistribution(RelationshipDistribution.POWER_LAW)
                .build().generate();
    }

    @Test
    void pregelMemEst() {
        // FIXME this is not an actual test...
        GraphDimensions dim = ImmutableGraphDimensions.builder().nodeCount(NODE_COUNT).maxRelCount(4 * NODE_COUNT).build();

        System.out.println("memoryEstimation = " + Pregel.memoryEstimation().estimate(dim, 4).render());
    }

    @Disabled
    void testPregel() {
        // TODO increase test heap space?
        Graph graph = getGraph(NODE_COUNT, AVG_DEGREE);
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

        bfsLevelJob.run();
    }

}
