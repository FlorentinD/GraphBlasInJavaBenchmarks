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
import org.neo4j.graphalgo.beta.pregel.bfs.BFSParentPregel;
import org.neo4j.graphalgo.beta.pregel.bfs.BFSPregelConfig;
import org.neo4j.graphalgo.beta.pregel.bfs.ImmutableBFSPregelConfig;
import org.neo4j.graphalgo.core.concurrency.Pools;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.neo4j.graphalgo.core.utils.paged.HugeLongArray;

import java.nio.Buffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BfsParentBenchmarkTest extends BaseBenchmarkTest {
    private static final int NODE_COUNT = 300_000;
    private static final int MAX_ITERATIONS = 100;
    private static final int CONCURRENCY = 1;
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
        goldStandard = getJniResult(ejmlGraph, START_NODE);

        System.out.println("goldStandard.iterations() = " + goldStandard.iterations());
    }

    @Test
    void pregelEqualsJni() {
        assertResultEquals(getPregelResult(ejmlGraph, START_NODE));
    }

    @Test
    void ejmlSparseEqualsJni() {
        assertResultEquals(getEjmlSparseResult(ejmlGraph, START_NODE));
    }

    @Test
    void ejmlDenseEqualsJni() {
        assertResultEquals(getEjmlDenseResult(ejmlGraph, START_NODE));
    }

    @Test
    void ejmlDenseSparseEqualsJni() {
        assertResultEquals(getEjmlDenseSparseResult(ejmlGraph, START_NODE));
    }


    private void assertResultEquals(BfsResult actual) {
        assertEquals(goldStandard.iterations(), actual.iterations(), "Iterations do not equal");
        assertEquals(goldStandard.nodesVisited(), actual.nodesVisited(), "Number of nodes visited does not equal");

        for (int i = 0; i < NODE_COUNT; i++) {
            boolean isVisited = goldStandard.visited(i);
            assertEquals(isVisited, actual.visited(i));

            if (isVisited) {
                assertEquals(goldStandard.get(i), actual.get(i), "Different for node " + i);
            }
        }
    }


    private BfsResult getPregelResult(Graph graph, int startNode) {
        // MAX_ITERATIONS + 1 as the init iterations also counts
        BFSPregelConfig config = ImmutableBFSPregelConfig.builder()
                .maxIterations(MAX_ITERATIONS)
                .startNode(startNode)
                .concurrency(CONCURRENCY)
                .build();

        Pregel<BFSPregelConfig> bfsLevelJob = Pregel.create(
                graph,
                config,
                new BFSParentPregel(),
                Pools.DEFAULT,
                AllocationTracker.empty()
        );

        Pregel.PregelResult result = bfsLevelJob.run();


        // making the result equal to the graphblas version
        HugeLongArray resultValues = result.nodeValues().longProperties(BFSParentPregel.PARENT);
        double[] resultArray = new double[Math.toIntExact(nodeCount())];
        for (int i = 0; i < NODE_COUNT; i++) {
            double resultValue = resultValues.get(i);
            // +1  for parent: ids have an offset of 1 for graphblas as 0 would be false in the bool semi-ring
            resultValue += 1;
            resultArray[i] = resultValue;
        }

        return new BfsDenseDoubleResult(resultArray, result.ranIterations(), Long.MAX_VALUE);
    }

    private BfsResult getEjmlSparseResult(EjmlGraph ejmlGraph, int startNode) {
        var unTransposedMatrix = CommonOps_DSCC.transpose(ejmlGraph.matrix(), null, null);
        unTransposedMatrix.sortIndices(null);
        return new BfsEjml().computeSparse(unTransposedMatrix, BfsEjml.BfsVariation.PARENTS, startNode, MAX_ITERATIONS);
    }

    private BfsResult getEjmlDenseResult(EjmlGraph ejmlGraph, int startNode) {
        var unTransposedMatrix = CommonOps_DSCC.transpose(ejmlGraph.matrix(), null, null);
        return new BfsEjml().computeDense(unTransposedMatrix, BfsEjml.BfsVariation.PARENTS, startNode, MAX_ITERATIONS);
    }

    private BfsResult getEjmlDenseSparseResult(EjmlGraph ejmlGraph, int startNode) {
        var unTransposedMatrix = CommonOps_DSCC.transpose(ejmlGraph.matrix(), null, null);
        return new BfsEjml().computeDenseSparse(unTransposedMatrix, BfsEjml.BfsVariation.PARENTS, startNode, MAX_ITERATIONS);
    }

    private BfsResult getJniResult(EjmlGraph ejmlGraph, int startNode) {
        GRBCORE.initNonBlocking();

        Buffer jniMatrix = ToNativeMatrixConverter.convert(ejmlGraph);

        var result = new BfsNative().computeParent(jniMatrix, startNode, MAX_ITERATIONS, 1);

        NativeHelper.checkStatusCode(GRBCORE.freeMatrix(jniMatrix));

        return result;
    }
}
