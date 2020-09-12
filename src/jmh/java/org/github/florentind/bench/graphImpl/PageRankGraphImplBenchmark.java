package org.github.florentind.bench.graphImpl;


import org.neo4j.graphalgo.Orientation;
import org.neo4j.graphalgo.api.CSRGraph;
import org.neo4j.graphalgo.beta.generator.PropertyProducer;
import org.neo4j.graphalgo.beta.generator.RandomGraphGenerator;
import org.neo4j.graphalgo.beta.generator.RelationshipDistribution;
import org.neo4j.graphalgo.config.RandomGraphGeneratorConfig;
import org.neo4j.graphalgo.core.Aggregation;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.neo4j.graphalgo.pagerank.ImmutablePageRankStatsConfig;
import org.neo4j.graphalgo.pagerank.PageRank;
import org.neo4j.graphalgo.pagerank.PageRankBaseConfig;
import org.neo4j.graphalgo.pagerank.PageRankFactory;
import org.neo4j.logging.NullLog;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Optional;


public class PageRankGraphImplBenchmark extends GraphImplBaseBenchmark {

    private PageRankBaseConfig unweightedConfig;
    private PageRankBaseConfig weightedConfig;

    private static String RELATIONSHIP_PROPERTY = "weight";

    @Param({"4"})
    private int concurrency;

    @Param({"3000000"})
    private int nodeCount;

    @Param({"4"})
    private int avgDegree;

    @Override
    CSRGraph getCSRGraph() {
        RandomGraphGenerator generator = new RandomGraphGenerator(
            nodeCount,
            avgDegree,
            RelationshipDistribution.POWER_LAW,
            42L,
            Optional.empty(),
            Optional.of(new PropertyProducer.Random(RELATIONSHIP_PROPERTY, -3, 3)),
            Aggregation.MAX,
            Orientation.NATURAL,
            RandomGraphGeneratorConfig.AllowSelfLoops.YES,
            AllocationTracker.empty()
        );
        return generator.generate();
    }

    @Setup(Level.Invocation)
    public void setup() {
        super.setup();

        unweightedConfig =  ImmutablePageRankStatsConfig
            .builder()
            .concurrency(concurrency)
            .build();

        weightedConfig = ImmutablePageRankStatsConfig
            .builder()
            .from(unweightedConfig)
            .relationshipWeightProperty(RELATIONSHIP_PROPERTY)
            .build();
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void unweighted(Blackhole bh) {
        PageRank algorithm = new PageRankFactory<>().build(
            graph,
            unweightedConfig,
            AllocationTracker.empty(),
            NullLog.getInstance()
        );

        bh.consume(algorithm.compute());
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void weighted(Blackhole bh) {
        PageRank algorithm = new PageRankFactory<>().build(
            graph,
            weightedConfig,
            AllocationTracker.empty(),
            NullLog.getInstance()
        );

        bh.consume(algorithm.compute());
    }
}
