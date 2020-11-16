package org.github.florentind.bench.bfs;


import org.github.florentind.bench.EjmlGraphBaseBenchmark;
import org.neo4j.graphalgo.Orientation;
import org.neo4j.graphalgo.api.CSRGraph;
import org.neo4j.graphalgo.beta.generator.RandomGraphGenerator;
import org.neo4j.graphalgo.beta.generator.RelationshipDistribution;
import org.neo4j.graphalgo.config.RandomGraphGeneratorConfig;
import org.neo4j.graphalgo.core.Aggregation;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;

public class BfsBaseBenchmark extends EjmlGraphBaseBenchmark {
    @Param({"100000", "500000", "1000000",  "5000000"})
    int nodeCount;

    @Param({"4"})
    int avgDegree;

    // node in the middle (id-wise)
    int startNode;

    // TODO: retrieve actually run iterations (for eval explanation)
    @Param({"100"})
    protected int maxIterations;

    @Param({"POWER_LAW", "UNIFORM"})
    protected String degreeDistribution;

    @Override
    @Setup
    public void setup() {
        super.setup();
        startNode = nodeCount / 2;
    }


    @Override
    protected CSRGraph getCSRGraph() {
        return RandomGraphGenerator.builder()
                .nodeCount(nodeCount)
                .averageDegree(avgDegree)
                .seed(42L)
                .aggregation(Aggregation.MAX)
                .orientation(Orientation.UNDIRECTED)
                .relationshipDistribution(RelationshipDistribution.valueOf(degreeDistribution))
                .allocationTracker(AllocationTracker.empty())
                .allowSelfLoops(RandomGraphGeneratorConfig.AllowSelfLoops.NO)
                .relationshipDistribution(RelationshipDistribution.POWER_LAW)
                .build().generate();

    }
}
