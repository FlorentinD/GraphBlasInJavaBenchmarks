package org.github.florentind.bench;

import org.github.florentind.core.ejml.EjmlGraph;
import org.github.florentind.graphalgos.triangleCount.TriangleCountEjml;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.graphalgo.Orientation;
import org.neo4j.graphalgo.beta.generator.RandomGraphGenerator;
import org.neo4j.graphalgo.beta.generator.RelationshipDistribution;
import org.neo4j.graphalgo.config.RandomGraphGeneratorConfig;
import org.neo4j.graphalgo.core.Aggregation;
import org.neo4j.graphalgo.core.GdsEdition;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.neo4j.graphalgo.core.utils.paged.HugeAtomicLongArray;
import org.neo4j.graphalgo.triangle.ImmutableTriangleCountBaseConfig;
import org.neo4j.graphalgo.triangle.IntersectingTriangleCountFactory;
import org.neo4j.logging.NullLog;

import java.util.Arrays;

public class TriangleCountBenchmarkTest extends BaseBenchmarkTest {

    @Override
    long nodeCount() {
        return 8;
    }

    @Override
    long avgDegree() {
        return 4;
    }

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
                .allocationTracker(AllocationTracker.EMPTY)
                .allowSelfLoops(RandomGraphGeneratorConfig.AllowSelfLoops.NO)
                .relationshipDistribution(RelationshipDistribution.POWER_LAW)
                .orientation(Orientation.UNDIRECTED)
                .build().generate();
    }

    @Test
    public void testGds() {
        var config = ImmutableTriangleCountBaseConfig
                .builder()
                .concurrency(1)
                .build();

        var result = new IntersectingTriangleCountFactory<>()
                .build(graph, config, AllocationTracker.EMPTY, NullLog.getInstance())
                .compute();


        HugeAtomicLongArray hugeArray = result.localTriangles();
        System.out.println("result.globalTriangles() = " + result.globalTriangles());
    }

    @Test
    public void testEjml() {
        var ejmlGraph = EjmlGraph.create(graph);
        //var matrix = CommonOps_DSCC.transpose(ejmlGraph.matrix(), null, null);
        // ! no need to transpose as symmetric anyway

        var result = TriangleCountEjml.computeNodeWise(ejmlGraph.matrix(), true);
        //var globalCount = TriangleCountEjml.computeTotalSandia(ejmlGraph.matrix());

        System.out.println("globalTriangles:" + result.totalCount());

        //System.out.println("globalCount = " + globalCount);
    }
}
