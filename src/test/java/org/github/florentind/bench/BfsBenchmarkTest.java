package org.github.florentind.bench;

import com.github.fabianmurariu.unsafe.GRBCORE;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.github.florentind.core.ejml.EjmlGraph;
import org.github.florentind.core.grapblas_native.EjmlToNativeMatrixConverter;
import org.github.florentind.graphalgos.bfs.BfsEjml;
import org.github.florentind.graphalgos.bfs.BfsNative;
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

import java.nio.Buffer;

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
    void pregelEqualsEjmlEqualsJniResult() {
        int startNode = 0;
        // TODO: see why PARENTS variation is not equal
        BfsEjml.BfsVariation variation = BfsEjml.BfsVariation.LEVEL;
        EjmlGraph ejmlGraph = EjmlGraph.create(graph);
        assertResultEquals(
                getEjmlResult(ejmlGraph, variation, true, startNode),
                getPregelResult(ejmlGraph, variation, startNode),
                variation
        );

        assertResultEquals(
                getJniResult(ejmlGraph, variation, startNode),
                getPregelResult(ejmlGraph, variation, startNode),
                variation
        );
    }

    private void assertResultEquals(BfsResult graphblasResult, Pregel.PregelResult pregelResult, BfsEjml.BfsVariation variation) {
        assertEquals(graphblasResult.iterations(), pregelResult.ranIterations() - 1);
        String propertyKey = (variation == BfsEjml.BfsVariation.LEVEL) ? BFSLevelPregel.LEVEL : BFSParentPregel.PARENT;
        HugeLongArray pregelResultValues = pregelResult.nodeValues().longProperties(propertyKey);

        for (int i = 0; i < NODE_COUNT; i++) {
            double graphblasValue = graphblasResult.get(i);
            switch (variation) {
                case PARENTS:
                    // -1  for parent: ids have an offset of 1 for ejml as 0 would be false in the bool semi-ring
                case LEVEL:
                    // -1 as for level: starts at 1 instead of 0 (and not found in pregel == -1)
                    graphblasValue -= 1;
                    break;
                default:
                    throw new IllegalStateException("Not implemented: " + variation.name());
            }

            assertEquals(graphblasValue, Double.valueOf(pregelResultValues.get(i)));
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
                AllocationTracker.empty()
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

    private BfsResult getJniResult(EjmlGraph ejmlGraph, BfsEjml.BfsVariation variation, int startNode) {
        GRBCORE.initNonBlocking();

        assert variation == BfsEjml.BfsVariation.LEVEL : "Not implemented ..";

        var unTransposedMatrix = CommonOps_DSCC.transpose(ejmlGraph.matrix(), null, null);

        Buffer jniMatrix = EjmlToNativeMatrixConverter.convert(unTransposedMatrix);

        var result = new BfsNative().computeLevel(jniMatrix, startNode, MAX_ITERATIONS, 1);
        GRBCORE.freeMatrix(jniMatrix);

        return result;
    };
}
