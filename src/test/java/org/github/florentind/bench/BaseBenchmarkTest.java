package org.github.florentind.bench;

import org.junit.jupiter.api.BeforeEach;
import org.neo4j.graphalgo.api.CSRGraph;
import org.neo4j.graphalgo.beta.generator.PropertyProducer;
import org.neo4j.graphalgo.beta.generator.RandomGraphGenerator;
import org.neo4j.graphalgo.beta.generator.RandomGraphGeneratorBuilder;
import org.neo4j.graphalgo.beta.generator.RelationshipDistribution;
import org.neo4j.graphalgo.config.RandomGraphGeneratorConfig;
import org.neo4j.graphalgo.core.Aggregation;
import org.neo4j.graphalgo.core.GdsEdition;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;

public abstract class BaseBenchmarkTest {

    abstract long nodeCount();

    abstract long avgDegree();

    PropertyProducer relationshipPropertyProducer() {
        return null;
    }

    CSRGraph graph;

    @BeforeEach
    void setup() {
        GdsEdition.instance().setToEnterpriseEdition();
        RandomGraphGeneratorBuilder builder = RandomGraphGenerator.builder()
                .nodeCount(nodeCount())
                .averageDegree(avgDegree())
                .seed(42L)
                .aggregation(Aggregation.MAX)
                .allocationTracker(AllocationTracker.empty())
                .allowSelfLoops(RandomGraphGeneratorConfig.AllowSelfLoops.NO)
                .relationshipDistribution(RelationshipDistribution.POWER_LAW);

        if (relationshipPropertyProducer() != null) {
            builder.relationshipPropertyProducer(relationshipPropertyProducer());
        }

        graph = builder.build().generate();
    }
}
