package org.github.florentind.bench;

import com.github.fabianmurariu.unsafe.GRBCORE;
import org.github.florentind.core.ejml.EjmlGraph;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.github.florentind.core.jgrapht.JGraphTConverter;
import org.github.florentind.graphalgos.triangleCount.TriangleCountEjml;
import org.github.florentind.graphalgos.triangleCount.TriangleCountNative;
import org.jgrapht.Graph;
import org.jgrapht.GraphMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.neo4j.graphalgo.Orientation;
import org.neo4j.graphalgo.beta.generator.RandomGraphGenerator;
import org.neo4j.graphalgo.beta.generator.RelationshipDistribution;
import org.neo4j.graphalgo.config.RandomGraphGeneratorConfig;
import org.neo4j.graphalgo.core.Aggregation;
import org.neo4j.graphalgo.core.GdsEdition;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.neo4j.graphalgo.triangle.ImmutableTriangleCountBaseConfig;
import org.neo4j.graphalgo.triangle.IntersectingTriangleCountFactory;
import org.neo4j.logging.NullLog;

import java.nio.Buffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * based on these test with a nodeCount of , I chose the following version:
 * - nodeWise -> useMask = true (up to 5x faster for 300k graph)
 * - global -> Sandia (instead of Cohen) .. Cohen seems to be faster for smaller graphs though ..
 */
public class TriangleCountBenchmarkTest extends BaseBenchmarkTest {

    @Override
    long nodeCount() {
        return 10_000;
    }

    @Override
    long avgDegree() {
        return 4;
    }

    EjmlGraph ejmlGraph;

    private static long expectedGlobalTriangles = 16182;

    // !! graph must be symmetric to properly work (and not have self-loops)
    @Override
    @BeforeEach
    void setup() {
        GdsEdition.instance().setToEnterpriseEdition();
        graph = RandomGraphGenerator.builder()
                .nodeCount(nodeCount())
                .averageDegree(avgDegree())
                .seed(42L)
                .aggregation(Aggregation.MAX)
                .allocationTracker(AllocationTracker.empty())
                .allowSelfLoops(RandomGraphGeneratorConfig.AllowSelfLoops.NO)
                .relationshipDistribution(RelationshipDistribution.POWER_LAW)
                .orientation(Orientation.UNDIRECTED)
                .build().generate();


        ejmlGraph = EjmlGraph.create(graph);
    }

    @Test
    public void testGds() {
        var config = ImmutableTriangleCountBaseConfig
                .builder()
                .concurrency(1)
                .build();

        var result = new IntersectingTriangleCountFactory<>()
                .build(ejmlGraph, config, AllocationTracker.empty(), NullLog.getInstance())
                .compute();


        long globalTriangles = result.globalTriangles();
        System.out.println("result.globalTriangles() = " + globalTriangles);

        assertEquals(expectedGlobalTriangles, globalTriangles);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void testNodeWiseEjml(boolean useLowerTriangle) {
        // ! no need to transpose as symmetric anyway
        var result = TriangleCountEjml.computeNodeWise(ejmlGraph.matrix(), useLowerTriangle);

        long globalTriangleCount = result.totalCount();
        assertEquals(expectedGlobalTriangles, globalTriangleCount);
    }

    @Test
    public void testEjmlGlobalSandia() {
        // ! no need to transpose as symmetric anyway
        long globalTriangleCount = TriangleCountEjml.computeTotalSandia(ejmlGraph.matrix());
        assertEquals(expectedGlobalTriangles, globalTriangleCount);
    }

    @Test
    public void testEjmlGlobalCohen() {
        // ! no need to transpose as symmetric anyway
        // without a mask, this quickly OOMs
        long globalTriangleCount = TriangleCountEjml.computeTotalCohen(ejmlGraph.matrix(), true);
        assertEquals(expectedGlobalTriangles, globalTriangleCount);
    }

    @Test
    public void testNativeSandia() {
        GRBCORE.initNonBlocking();

        Buffer jniMatrix = ToNativeMatrixConverter.convert(ejmlGraph);

        long actual = TriangleCountNative.computeTotalSandia(jniMatrix);

        GRBCORE.freeMatrix(jniMatrix);
        GRBCORE.grbFinalize();

        assertEquals(expectedGlobalTriangles, actual);
    }

    @Disabled
    @Test
    public void testJGraphTGlobal() {
        // TODO this finds way too many triangles ..
        Graph jGraph = JGraphTConverter.convert(ejmlGraph);

        assertEquals(expectedGlobalTriangles, GraphMetrics.getNumberOfTriangles(jGraph));
    }
}
