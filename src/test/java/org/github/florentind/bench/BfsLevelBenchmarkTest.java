package org.github.florentind.bench;

import com.github.fabianmurariu.unsafe.GRBCORE;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.github.florentind.core.ejml.EjmlGraph;
import org.github.florentind.core.ejml.EjmlUtil;
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
import org.neo4j.graphalgo.beta.pregel.bfs.BFSPregelConfig;
import org.neo4j.graphalgo.beta.pregel.bfs.ImmutableBFSPregelConfig;
import org.neo4j.graphalgo.core.concurrency.Pools;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.neo4j.graphalgo.core.utils.paged.HugeLongArray;

import java.nio.Buffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BfsLevelBenchmarkTest extends BaseBenchmarkTest {
    private static final int NODE_COUNT = 1000_000;
    private static final int MAX_ITERATIONS = 100;
    private static final int CONCURRENCY = 16;
    private static final int START_NODE = NODE_COUNT / 2;

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
        System.out.println("goldStandard.nodesVisited() = " + goldStandard.nodesVisited());
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
            assertEquals(goldStandard.get(i), actual.get(i), "Different for node " + i);
        }
    }


    private BfsResult getPregelResult(Graph graph, int startNode) {
        // MAX_ITERATIONS + 1 as the init iterations also counts
        BFSPregelConfig config = ImmutableBFSPregelConfig.builder()
                .maxIterations(MAX_ITERATIONS)
                .startNode(startNode)
                .concurrency(CONCURRENCY)
                .build();

        PregelComputation<BFSPregelConfig> computation;
        computation = new BFSLevelPregel();

        Pregel<BFSPregelConfig> bfsLevelJob = Pregel.create(
                graph,
                config,
                computation,
                Pools.DEFAULT,
                AllocationTracker.empty()
        );

        Pregel.PregelResult result = bfsLevelJob.run();


        // making the result equal to the graphblas version
        HugeLongArray resultValues = result.nodeValues().longProperties(BFSLevelPregel.LEVEL);
        double[] resultArray = new double[Math.toIntExact(nodeCount())];
        for (int i = 0; i < NODE_COUNT; i++) {
            double resultValue = resultValues.get(i);
            // +1 as for level: graphblas versions starts at 1 instead of 0 (and not found in pregel == -1)
            resultValue += 1;
            resultArray[i] = resultValue;
        }

        return new BfsDenseDoubleResult(resultArray, result.ranIterations(), (long) 0);
    }

    private BfsResult getEjmlSparseResult(EjmlGraph ejmlGraph, int startNode) {
        return new BfsEjml().computeSparse(ejmlGraph.matrix(), BfsEjml.BfsVariation.LEVEL, startNode, MAX_ITERATIONS);
    }

    private BfsResult getEjmlDenseResult(EjmlGraph ejmlGraph, int startNode) {
        return new BfsEjml().computeDense(ejmlGraph.matrix(), BfsEjml.BfsVariation.LEVEL, startNode, MAX_ITERATIONS);
    }

    private BfsResult getEjmlDenseSparseResult(EjmlGraph ejmlGraph, int startNode) {
        return new BfsEjml().computeDenseSparse(ejmlGraph.matrix(), BfsEjml.BfsVariation.LEVEL, startNode, MAX_ITERATIONS);
    }

    private BfsResult getJniResult(EjmlGraph ejmlGraph, int startNode) {
        GRBCORE.initNonBlocking();

        Buffer jniMatrix = ToNativeMatrixConverter.convert(ejmlGraph.matrix());

        var result = new BfsNative().computeLevel(jniMatrix, startNode, MAX_ITERATIONS, CONCURRENCY);
        NativeHelper.checkStatusCode(GRBCORE.freeMatrix(jniMatrix));

        return result;
    }
}
