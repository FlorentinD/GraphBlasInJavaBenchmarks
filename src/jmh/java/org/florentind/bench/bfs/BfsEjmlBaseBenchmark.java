package org.florentind.bench.bfs;


import org.florentind.bench.EjmlGraphBaseBenchmark;
import org.neo4j.graphalgo.api.CSRGraph;
import org.neo4j.graphalgo.beta.generator.RandomGraphGenerator;
import org.neo4j.graphalgo.beta.generator.RelationshipDistribution;
import org.neo4j.graphalgo.config.RandomGraphGeneratorConfig;
import org.neo4j.graphalgo.core.Aggregation;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.openjdk.jmh.annotations.Param;

// TODO: converter from EJML-Graph to JNI-Graph
public class BfsEjmlBaseBenchmark extends EjmlGraphBaseBenchmark {
    @Param({"3000000"})
    int nodeCount;

    @Param({"4"})
    int avgDegree;

    @Param({"0"})
    int startNode;

    // TODO: retrieve actually run iterations
    @Param({"100"})
    int maxIterations;


    @Override
    protected CSRGraph getCSRGraph() {
        return RandomGraphGenerator.builder()
                .nodeCount(nodeCount)
                .averageDegree(avgDegree)
                .seed(42L)
                .aggregation(Aggregation.MAX)
                .allocationTracker(AllocationTracker.EMPTY)
                .allowSelfLoops(RandomGraphGeneratorConfig.AllowSelfLoops.YES)
                .relationshipDistribution(RelationshipDistribution.POWER_LAW)
                .build().generate();

    }

    // TODO add MSBFS benchmark

}
