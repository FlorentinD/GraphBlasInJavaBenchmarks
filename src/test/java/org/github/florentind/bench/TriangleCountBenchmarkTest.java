package org.github.florentind.bench;

import org.github.florentind.core.ejml.EjmlGraph;
import org.github.florentind.core.jgrapht.JGraphTConverter;
import org.github.florentind.graphalgos.triangleCount.NativeNodeWiseTriangleCountResult;
import org.github.florentind.graphalgos.triangleCount.TriangleCountEjml;
import org.github.florentind.graphalgos.triangleCount.TriangleCountResult;
import org.jgrapht.GraphMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.neo4j.graphalgo.Orientation;
import org.neo4j.graphalgo.beta.pregel.Pregel;
import org.neo4j.graphalgo.beta.pregel.triangleCount.ImmutableTriangleCountPregelConfig;
import org.neo4j.graphalgo.beta.pregel.triangleCount.TriangleCountPregel;
import org.neo4j.graphalgo.core.concurrency.Pools;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.neo4j.graphalgo.core.utils.paged.HugeAtomicLongArray;
import org.neo4j.graphalgo.triangle.ImmutableTriangleCountBaseConfig;
import org.neo4j.graphalgo.triangle.IntersectingTriangleCount;
import org.neo4j.graphalgo.triangle.IntersectingTriangleCountFactory;
import org.neo4j.logging.NullLog;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * based on these test with a nodeCount of , I chose the following version:
 * - nodeWise -> useMask = true (up to 5x faster for 300k graph)
 * - global -> Sandia (instead of Cohen) .. Cohen seems to be faster for smaller graphs though ..
 */
public class TriangleCountBenchmarkTest extends BaseBenchmarkTest {

    private static final int CONCURRENCY = 8;

    @Override
    long nodeCount() {
        return 30_000;
    }

    @Override
    long avgDegree() {
        return 4;
    }

    @Override
    Orientation orientation() {
        return Orientation.UNDIRECTED;
    }

    EjmlGraph ejmlGraph;

    TriangleCountResult expected;

    // !! graph must be symmetric to properly work (and not have self-loops)
    @Override
    @BeforeEach
    void setup() {
        super.setup();

        ejmlGraph = EjmlGraph.create(graph);

        expected = TriangleCountEjml.computeNodeWise(ejmlGraph.matrix(), true);
//        System.out.println(expected.totalCount());
    }

    @Test
    public void testGds() {
        var config = ImmutableTriangleCountBaseConfig
                .builder()
                .concurrency(CONCURRENCY)
                .build();

        var result = new IntersectingTriangleCountFactory<>()
                .build(ejmlGraph, config, AllocationTracker.empty(), NullLog.getInstance())
                .compute();


        long globalTriangles = result.globalTriangles();

        assertNodeWiseCount(expected, result);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true})
    public void testNodeWiseEjml(boolean useLowerTriangle) {
        // ! no need to transpose as symmetric anyway
        var result = TriangleCountEjml.computeNodeWise(ejmlGraph.matrix(), useLowerTriangle);

        assertNodeWiseCount(expected, result);
    }

    @Test
    public void testEjmlGlobalSandia() {
        // ! no need to transpose as symmetric anyway
        assertEquals(expected.totalCount(), TriangleCountEjml.computeTotalSandia(ejmlGraph.matrix()));
    }

    @Test
    public void testEjmlGlobalCohen() {
        // ! no need to transpose as symmetric anyway
        // without a mask, this quickly OOMs
        assertEquals(expected.totalCount(), TriangleCountEjml.computeTotalCohen(ejmlGraph.matrix(), true));
    }

    @Test
    public void testJGraphTGlobal() {
        var jGraph = JGraphTConverter.convert(ejmlGraph);

        assertEquals(expected.totalCount(), GraphMetrics.getNumberOfTriangles(jGraph));
    }

    @Test
    public void testPregel() {
        var triangleCountJob = Pregel.create(
                graph,
                ImmutableTriangleCountPregelConfig.builder()
                        .concurrency(CONCURRENCY)
                        .build(),
                new TriangleCountPregel(),
                Pools.DEFAULT,
                AllocationTracker.empty()
        );

        var result = new NativeNodeWiseTriangleCountResult(
                triangleCountJob.run().nodeValues().longProperties(TriangleCountPregel.TRIANGLE_COUNT).toArray()
        );
        assertNodeWiseCount(expected, result);
    }

    void assertNodeWiseCount(TriangleCountResult expected, TriangleCountResult actual) {
        assertEquals(expected.totalCount(), actual.totalCount());

        ejmlGraph.forEachNode(id -> {
            assertEquals(expected.get((int) id), actual.get((int) id));
            return true;
        });
    }

    void assertNodeWiseCount(TriangleCountResult expected, IntersectingTriangleCount.TriangleCountResult actual) {
        assertEquals(expected.totalCount(), actual.globalTriangles());

        HugeAtomicLongArray localTriangles = actual.localTriangles();
        ejmlGraph.forEachNode(id -> {
            assertEquals(expected.get((int) id), localTriangles.get(id));
            return true;
        });
    }
}
