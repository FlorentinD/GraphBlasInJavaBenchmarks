package org.github.florentind.bench.triangleCount;

import org.github.florentind.bench.EjmlGraphBaseBenchmark;
import org.neo4j.graphalgo.Orientation;
import org.neo4j.graphalgo.api.CSRGraph;
import org.neo4j.graphalgo.beta.generator.RandomGraphGenerator;
import org.neo4j.graphalgo.beta.generator.RelationshipDistribution;
import org.neo4j.graphalgo.config.RandomGraphGeneratorConfig;
import org.neo4j.graphalgo.core.Aggregation;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.openjdk.jmh.annotations.Param;

public class TriangleCountBaseBenchmark extends EjmlGraphBaseBenchmark {
    // TODO smaller nodeCounts
    //      Pregel?

    // "1000", "10000", "100000", "1000000"
    @Param({"10000", "100000", "1000000"})
    int nodeCount;

    @Param({"4"})
    int avgDegree;

    // !! undirected and no self-loops
    @Override
    protected CSRGraph getCSRGraph() {
        return RandomGraphGenerator.builder()
                .nodeCount(nodeCount)
                .averageDegree(avgDegree)
                .seed(42L)
                .aggregation(Aggregation.MAX)
                .allocationTracker(AllocationTracker.empty())
                .allowSelfLoops(RandomGraphGeneratorConfig.AllowSelfLoops.NO)
                .relationshipDistribution(RelationshipDistribution.POWER_LAW)
                .orientation(Orientation.UNDIRECTED)
                .build().generate();

    }


}
