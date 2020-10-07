package org.github.florentind.bench;

import com.github.fabianmurariu.unsafe.GRBCORE;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.github.florentind.core.ejml.EjmlGraph;
import org.github.florentind.core.grapblas_native.NativeHelper;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.github.florentind.graphalgos.bfs.BfsDenseDoubleResult;
import org.github.florentind.graphalgos.bfs.BfsEjml;
import org.github.florentind.graphalgos.bfs.BfsNative;
import org.github.florentind.graphalgos.bfs.BfsResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.graphalgo.api.Graph;
import org.neo4j.graphalgo.beta.pregel.Pregel;
import org.neo4j.graphalgo.beta.pregel.PregelComputation;
import org.neo4j.graphalgo.beta.pregel.bfs.BFSLevelPregel;
import org.neo4j.graphalgo.beta.pregel.bfs.BFSParentPregel;
import org.neo4j.graphalgo.beta.pregel.bfs.BFSPregelConfig;
import org.neo4j.graphalgo.beta.pregel.bfs.ImmutableBFSPregelConfig;
import org.neo4j.graphalgo.core.concurrency.Pools;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.neo4j.graphalgo.core.utils.paged.HugeLongArray;

import java.nio.Buffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BfsLevelBenchmarkTest extends BaseBenchmarkTest {
    private static final int NODE_COUNT = 30_000;
    private static final int MAX_ITERATIONS = 100;
    private static final int CONCURRENCY = 16;
    // TODO: see why PARENTS variation is not equal
    private static final BfsEjml.BfsVariation VARIATION = BfsEjml.BfsVariation.LEVEL;
    private static final int START_NODE = 0;


    @Override
    long nodeCount() {
        return NODE_COUNT;
    }

    @Override
    long avgDegree() {
        return 4;
    }

    BfsResult goldStandard;
    EjmlGraph ejmlGraph;

    @Override
    @BeforeEach
    void setup() {
        super.setup();
        ejmlGraph = EjmlGraph.create(graph);
        goldStandard = getJniResult(ejmlGraph, VARIATION, START_NODE);
    }

    @Test
    void pregelEqualsJni() {
        assertResultEquals(getPregelResult(ejmlGraph, VARIATION, START_NODE));
    }

    @Test
    void ejmlSparseEqualsJni() {
        assertResultEquals(getEjmlSparseResult(ejmlGraph, VARIATION, START_NODE));
    }

    @Test
    void ejmlDenseEqualsJni() {
        assertResultEquals(getEjmlDenseResult(ejmlGraph, VARIATION, START_NODE));
    }

    @Test
    void ejmlDenseSparseEqualsJni() {
        assertResultEquals(getEjmlDenseSparseResult(ejmlGraph, VARIATION, START_NODE));
    }


    private void assertResultEquals(BfsResult actual) {
        assertEquals(goldStandard.iterations(), actual.iterations(), "Iterations do not equal");
        assertEquals(goldStandard.nodesVisited(), actual.nodesVisited(), "Number of nodes visited does not equal");

        for (int i = 0; i < NODE_COUNT; i++) {
            assertEquals(goldStandard.get(i), actual.get(i), "Different for node " + i);
        }
    }


    private BfsResult getPregelResult(Graph graph, BfsEjml.BfsVariation variation, int startNode) {
        // MAX_ITERATIONS + 1 as the init iterations also counts
        BFSPregelConfig config = ImmutableBFSPregelConfig.builder()
                .maxIterations(MAX_ITERATIONS)
                .startNode(startNode)
                .concurrency(CONCURRENCY)
                .build();

        PregelComputation<BFSPregelConfig> computation;
        long zeroElement;
        switch (variation) {
            case LEVEL:
                computation = new BFSLevelPregel();
                zeroElement = 0;
                break;
            case PARENTS:
                computation = new BFSParentPregel();
                zeroElement = Long.MAX_VALUE;
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

        Pregel.PregelResult result = bfsLevelJob.run();


        // making the result equal to the graphblas version
        String propertyKey = (variation == BfsEjml.BfsVariation.LEVEL) ? BFSLevelPregel.LEVEL : BFSParentPregel.PARENT;
        HugeLongArray resultValues = result.nodeValues().longProperties(propertyKey);
        double[] resultArray = new double[Math.toIntExact(nodeCount())];
        for (int i = 0; i < NODE_COUNT; i++) {
            double resultValue = resultValues.get(i);
            switch (variation) {
                case PARENTS:
                    // +1  for parent: ids have an offset of 1 for graphblas as 0 would be false in the bool semi-ring
                case LEVEL:
                    // +1 as for level: graphblas versions starts at 1 instead of 0 (and not found in pregel == -1)
                    resultValue += 1;
                    break;
                default:
                    throw new IllegalStateException("Not implemented: " + variation.name());
            }
            resultArray[i] = resultValue;
        }

        return new BfsDenseDoubleResult(resultArray, result.ranIterations(), zeroElement);
    }

    private BfsResult getEjmlSparseResult(EjmlGraph ejmlGraph, BfsEjml.BfsVariation variation, int startNode) {
        var unTransposedMatrix = CommonOps_DSCC.transpose(ejmlGraph.matrix(), null, null);
        return new BfsEjml().computeSparse(unTransposedMatrix, variation, new int[]{startNode}, MAX_ITERATIONS);
    }

    private BfsResult getEjmlDenseResult(EjmlGraph ejmlGraph, BfsEjml.BfsVariation variation, int startNode) {
        var unTransposedMatrix = CommonOps_DSCC.transpose(ejmlGraph.matrix(), null, null);
        return new BfsEjml().computeDense(unTransposedMatrix, variation, startNode, MAX_ITERATIONS);
    }

    private BfsResult getEjmlDenseSparseResult(EjmlGraph ejmlGraph, BfsEjml.BfsVariation variation, int startNode) {
        var unTransposedMatrix = CommonOps_DSCC.transpose(ejmlGraph.matrix(), null, null);
        return new BfsEjml().computeDenseSparse(unTransposedMatrix, variation, startNode, MAX_ITERATIONS);
    }

    private BfsResult getJniResult(EjmlGraph ejmlGraph, BfsEjml.BfsVariation variation, int startNode) {
        GRBCORE.initNonBlocking();

        assert variation == BfsEjml.BfsVariation.LEVEL : "Not implemented ..";

        var unTransposedMatrix = CommonOps_DSCC.transpose(ejmlGraph.matrix(), null, null);

        Buffer jniMatrix = ToNativeMatrixConverter.convert(unTransposedMatrix);

        var result = new BfsNative().computeLevel(jniMatrix, startNode, MAX_ITERATIONS, 1);
        NativeHelper.checkStatusCode(GRBCORE.freeMatrix(jniMatrix));

        return result;
    }
}
