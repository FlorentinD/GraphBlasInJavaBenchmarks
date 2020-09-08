package org.github.florentind.bench;

import org.ejml.sparse.csc.CommonOps_DSCC;
import org.github.florentind.core.ejml.EjmlGraph;
import org.github.florentind.graphalgos.bfs.BfsEjml;
import org.github.florentind.graphalgos.bfs.BfsResult;
import org.junit.jupiter.api.Test;
import org.neo4j.graphalgo.api.Graph;
import org.neo4j.graphalgo.beta.pregel.Pregel;
import org.neo4j.graphalgo.beta.pregel.PregelComputation;
import org.neo4j.graphalgo.beta.pregel.bfs.BFSLevelPregel;
import org.neo4j.graphalgo.beta.pregel.bfs.BFSParentPregel;
import org.neo4j.graphalgo.beta.pregel.bfs.BFSPregelConfig;
import org.neo4j.graphalgo.beta.pregel.bfs.ImmutableBFSPregelConfig;
import org.neo4j.graphalgo.core.GraphDimensions;
import org.neo4j.graphalgo.core.ImmutableGraphDimensions;
import org.neo4j.graphalgo.core.concurrency.Pools;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.neo4j.graphalgo.core.utils.paged.HugeLongArray;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BfsBenchmarkTest extends BaseBenchmarkTest {
    private static final int NODE_COUNT = 30000;
    private static final int MAX_ITERATIONS = 100;
    private static final int CONCURRENCY = 16;


    @Override
    long nodeCount() {
        return NODE_COUNT;
    }

    @Override
    long avgDegree() {
        return 4;
    }

    @Test
    void testPregel() {
        GraphDimensions dim = ImmutableGraphDimensions.builder().nodeCount(NODE_COUNT).maxRelCount(4 * NODE_COUNT).build();
        System.out.println("memoryEstimation = " + Pregel.memoryEstimation(new BFSLevelPregel().nodeSchema()).estimate(dim, 4).render());

        var result = getPregelResult(graph, BfsEjml.BfsVariation.LEVEL, 0);
        // ranIteration includes initIteration
        System.out.println("result.ranIterations() = " + result.ranIterations());
    }

    @Test
    void pregelEqualsEjmlResult() {
        int startNode = 0;
        // TODO: see why PARENTS variation is not equal
        BfsEjml.BfsVariation variation = BfsEjml.BfsVariation.LEVEL;
        EjmlGraph ejmlGraph = EjmlGraph.create(graph);
        assertResultEquals(
                getEjmlResult(ejmlGraph, variation, true, startNode),
                getPregelResult(ejmlGraph, variation, startNode),
                variation
        );
    }

    private void assertResultEquals(BfsResult ejmlResult, Pregel.PregelResult pregelResult, BfsEjml.BfsVariation variation) {
        assertEquals(ejmlResult.iterations(), pregelResult.ranIterations() - 1);
        String propertyKey = (variation == BfsEjml.BfsVariation.LEVEL) ? BFSLevelPregel.LEVEL : BFSParentPregel.PARENT;
        HugeLongArray pregelResultValues = pregelResult.nodeValues().longProperties(propertyKey);

        for (int i = 0; i < NODE_COUNT; i++) {
            double ejmlValue = ejmlResult.get(i);
            switch (variation) {
                case PARENTS:
                    // -1  for parent: ids have an offset of 1 for ejml as 0 would be false in the bool semi-ring
                case LEVEL:
                    // -1 as for level: starts at 1 instead of 0 (and not found in pregel == -1)
                    ejmlValue -= 1;
                    break;
                default:
                    throw new IllegalStateException("Not implemented: " + variation.name());
            }

            assertEquals(ejmlValue, Double.valueOf(pregelResultValues.get(i)));
        }

    }


    private Pregel.PregelResult getPregelResult(Graph graph, BfsEjml.BfsVariation variation, int startNode) {
        BFSPregelConfig config = ImmutableBFSPregelConfig.builder()
                .maxIterations(MAX_ITERATIONS)
                .startNode(startNode)
                .concurrency(CONCURRENCY)
                .build();

        PregelComputation<BFSPregelConfig> computation;
        switch (variation) {
            case LEVEL:
                computation = new BFSLevelPregel();
                break;
            case PARENTS:
                computation = new BFSParentPregel();
                break;
            default:
                throw new IllegalStateException("variant not implemented for Pregel" + variation.name());
        }

        Pregel<BFSPregelConfig> bfsLevelJob = Pregel.create(
                graph,
                config,
                computation,
                Pools.DEFAULT,
                AllocationTracker.EMPTY
        );

        return bfsLevelJob.run();
    }

    private BfsResult getEjmlResult(EjmlGraph ejmlGraph, BfsEjml.BfsVariation variation, boolean sparse, int startNode) {
        var unTransposedMatrix = CommonOps_DSCC.transpose(ejmlGraph.matrix(), null, null);
        if (sparse) {
            return new BfsEjml().computeSparse(unTransposedMatrix, variation, new int[]{startNode}, MAX_ITERATIONS);
        } else {
            return new BfsEjml().computeDense(unTransposedMatrix, variation, startNode, MAX_ITERATIONS);
        }
    }
}
