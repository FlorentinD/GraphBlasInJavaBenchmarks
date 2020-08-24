package org.florentind.core.ejml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.graphalgo.AlgoTestBase;
import org.neo4j.graphalgo.Orientation;
import org.neo4j.graphalgo.StoreLoaderBuilder;
import org.neo4j.graphalgo.api.Graph;
import org.neo4j.graphalgo.api.RelationshipIntersect;
import org.neo4j.graphalgo.core.huge.HugeGraph;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

import java.util.Arrays;
import java.util.PrimitiveIterator;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.neo4j.graphalgo.compat.GraphDatabaseApiProxy.applyInTransaction;

final class EjmlIntersectionTest extends AlgoTestBase {

    private static final int DEGREE = 25;
    public static final RelationshipType TYPE = RelationshipType.withName("TYPE");
    private static RelationshipIntersect INTERSECT;
    private static long START1;
    private static long START2;
    private static long[] TARGETS;

    @BeforeEach
    void setup() {
        Random random = new Random(0L);
        long[] neoStarts = new long[2];
        long[] neoTargets = applyInTransaction(db, tx -> {
            Node start1 = tx.createNode();
            Node start2 = tx.createNode();
            Node start3 = tx.createNode();
            neoStarts[0] = start1.getId();
            neoStarts[1] = start2.getId();
            start1.createRelationshipTo(start2, TYPE);
            long[] targets = new long[DEGREE];
            int some = 0;
            for (int i = 0; i < DEGREE; i++) {
                Node target = tx.createNode();
                start1.createRelationshipTo(target, TYPE);
                start3.createRelationshipTo(target, TYPE);
                if (random.nextBoolean()) {
                    start2.createRelationshipTo(target, TYPE);
                    targets[some++] = target.getId();
                }
            }
            return Arrays.copyOf(targets, some);
        });

        Graph graph = new StoreLoaderBuilder()
            .api(db)
            .globalOrientation(Orientation.UNDIRECTED)
            .build()
            .graph();

        graph = EjmlGraph.create((HugeGraph) graph);

        INTERSECT = graph.intersection();
        START1 = graph.toMappedNodeId(neoStarts[0]);
        START2 = graph.toMappedNodeId(neoStarts[1]);
        TARGETS = Arrays.stream(neoTargets).map(graph::toMappedNodeId).toArray();
        Arrays.sort(TARGETS);
    }

    @Test
    void intersectWithTargets() {
        PrimitiveIterator.OfLong targets = Arrays.stream(TARGETS).iterator();
        INTERSECT.intersectAll(START1, (a, b, c) -> {
            assertEquals(START1, a);
            assertEquals(START2, b);
            assertEquals(targets.nextLong(), c);
        });
    }
}
