package org.github.florentind.bench.weightedPageRank;


import org.github.florentind.bench.EjmlGraphBaseBenchmark;
import org.neo4j.graphalgo.api.CSRGraph;
import org.neo4j.graphalgo.beta.generator.PropertyProducer;
import org.neo4j.graphalgo.beta.generator.RandomGraphGenerator;
import org.neo4j.graphalgo.beta.generator.RelationshipDistribution;
import org.neo4j.graphalgo.config.RandomGraphGeneratorConfig;
import org.neo4j.graphalgo.core.Aggregation;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.openjdk.jmh.annotations.Param;

public class WeightedPageRankBaseBenchmark extends EjmlGraphBaseBenchmark {
    protected static final String REL_PROPERTY_NAME = "weight";
    @Param({"3000000"})
    int nodeCount;

    @Param({"4"})
    int avgDegree;


    // TODO: scale maxIterations (! need to make sure none terminates earlier .. f.i. via a high tolerance)
    @Param({"20"})
    protected int maxIterations;

    @Param({"0.85"})
    protected float dampingFactor;

    @Param({"1e-32"})
    protected float tolerance;

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
                .relationshipPropertyProducer(PropertyProducer.random(REL_PROPERTY_NAME, 0, 1.0))
                .build().generate();

    }
}
