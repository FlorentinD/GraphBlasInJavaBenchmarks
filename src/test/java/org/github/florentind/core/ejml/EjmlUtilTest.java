package org.github.florentind.core.ejml;

import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.junit.jupiter.api.Test;
import org.neo4j.graphalgo.beta.generator.PropertyProducer;
import org.neo4j.graphalgo.beta.generator.RandomGraphGenerator;
import org.neo4j.graphalgo.beta.generator.RelationshipDistribution;
import org.neo4j.graphalgo.core.Aggregation;
import org.neo4j.graphalgo.core.utils.AtomicDoubleArray;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class EjmlUtilTest {

    @Test
    void normalize() {
        var matrix = RandomMatrices_DSCC.generateUniform(10, 10, 5, 0, 5, new Random(42));

        EjmlUtil.normalizeColumnWise(matrix);

        var actualColumSums = CommonOps_DSCC.reduceColumnWise(matrix, 0, Double::sum, null).data;

        for (double actualColumSum : actualColumSums) {
            assertTrue((Math.round(actualColumSum * 100) - 100 == 0) || actualColumSum == 0, "value was " + actualColumSum);
        }
    }

    @Test
    void normalizeGraph() {
        var graph = RandomGraphGenerator
                .builder()
                .relationshipPropertyProducer(PropertyProducer.random("weight", 0, 5))
                .nodeCount(10)
                .averageDegree(5)
                .relationshipDistribution(RelationshipDistribution.POWER_LAW)
                .aggregation(Aggregation.SINGLE)
                .build()
                .generate();

        var ejmlGraph = EjmlGraph.create(graph);

        EjmlUtil.normalizeOutgoingWeights(ejmlGraph);

        AtomicDoubleArray actualTotals = new AtomicDoubleArray((int) ejmlGraph.nodeCount());
        ejmlGraph.forEachNode(id -> {
            ejmlGraph.forEachRelationship(id, 0, (src, trg, weight) -> {
                actualTotals.add(Math.toIntExact(id), weight);
                return true;
            });
            return true;
        });

        for (int i = 0; i < actualTotals.length(); i++) {
            double total = actualTotals.get(i);
            assertTrue((Math.round(total * 100) - 100 == 0) || total == 0, "value was " + total);
        }
    }
}
