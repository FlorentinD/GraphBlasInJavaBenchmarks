package org.github.florentind.bench.weightedPageRank;


import org.github.florentind.bench.pageRank.PageRankBaseBenchmark;
import org.neo4j.graphalgo.api.CSRGraph;
import org.neo4j.graphalgo.beta.generator.PropertyProducer;
import org.neo4j.graphalgo.beta.generator.RandomGraphGenerator;
import org.neo4j.graphalgo.beta.generator.RelationshipDistribution;
import org.neo4j.graphalgo.config.RandomGraphGeneratorConfig;
import org.neo4j.graphalgo.core.Aggregation;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;

public class WeightedPageRankBaseBenchmark extends PageRankBaseBenchmark {
    protected static final String REL_PROPERTY_NAME = "weight";

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
