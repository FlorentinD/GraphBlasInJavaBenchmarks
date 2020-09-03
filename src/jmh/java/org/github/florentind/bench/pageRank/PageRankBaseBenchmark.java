package org.github.florentind.bench.pageRank;


import org.github.florentind.bench.EjmlGraphBaseBenchmark;
import org.neo4j.graphalgo.api.CSRGraph;
import org.neo4j.graphalgo.beta.generator.RandomGraphGenerator;
import org.neo4j.graphalgo.beta.generator.RelationshipDistribution;
import org.neo4j.graphalgo.config.RandomGraphGeneratorConfig;
import org.neo4j.graphalgo.core.Aggregation;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.openjdk.jmh.annotations.Param;

// TODO: converter from EJML-Graph to JNI-Graph
public class PageRankBaseBenchmark extends EjmlGraphBaseBenchmark {
    @Param({"3000000"})
    int nodeCount;

    @Param({"4"})
    int avgDegree;


    // TODO: scale maxIterations (! need to make sure none terminates earlier .. f.i. via a high tolerance)
    @Param({"10"})
    protected int maxIterations;

    @Param({"0.85"})
    protected float dampingFactor;

    @Param({"1e-7"})
    protected float tolerance;



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
}
