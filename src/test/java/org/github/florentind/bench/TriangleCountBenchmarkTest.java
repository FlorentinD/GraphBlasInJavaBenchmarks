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
import org.neo4j.graphalgo.triangle.ImmutableTriangleCountBaseConfig;
import org.neo4j.graphalgo.triangle.IntersectingTriangleCountFactory;
import org.neo4j.logging.NullLog;

public class TriangleCountBenchmarkTest extends BaseBenchmarkTest {
// TODO: wait for fixed gds TriangleCount

    @Override
    long nodeCount() {
        return 3000;
    }

    @Override
    long avgDegree() {
        return 4;
    }

    EjmlGraph ejmlGraph;

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


        //ejmlGraph = EjmlGraph.create(graph);

        //ejmlGraph.matrix().createCoordinateIterator().forEachRemaining(v -> System.out.println(v.col + "," +  v.row));

    }

    @Test
    public void testGds() {
        var config = ImmutableTriangleCountBaseConfig
                .builder()
                .concurrency(1)
                .build();

        var result = new IntersectingTriangleCountFactory<>()
                .build(graph, config, AllocationTracker.empty(), NullLog.getInstance())
                .compute();


        System.out.println("result.globalTriangles() = " + result.globalTriangles());
//
//        System.out.println("single stuff:");
//        for (int i = 0; i < nodeCount(); i++) {
//            System.out.print(result.localTriangles().get(i) + ",");
//        }

    }

    @Test
    public void testEjml() {
        ejmlGraph = EjmlGraph.create(graph);
        // ! no need to transpose as symmetric anyway
        //var matrix = CommonOps_DSCC.transpose(ejmlGraph.matrix(), null, null);

        var result = TriangleCountEjml.computeNodeWise(ejmlGraph.matrix(), true);
        //var globalCount = TriangleCountEjml.computeTotalSandia(ejmlGraph.matrix());

        System.out.println("globalTriangles:" + result.totalCount());

        System.out.println("single stuff");
        for (int i = 0; i < nodeCount(); i++) {
            System.out.print(result.get(i) + ",");
        }

        // TODO Triangle 0, 3, 6

        System.out.println();
        System.out.println("0->3:" + ejmlGraph.matrix().isAssigned(0, 3));
        System.out.println("3->6:" + ejmlGraph.matrix().isAssigned(3, 6));
        System.out.println("6->0:" + ejmlGraph.matrix().isAssigned(6, 0));

        //System.out.println("globalCount = " + globalCount);
    }
}
