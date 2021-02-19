package org.github.florentind.bench.graphImpl;


import org.neo4j.graphalgo.api.CSRGraph;
import org.neo4j.graphalgo.beta.generator.RandomGraphGenerator;
import org.neo4j.graphalgo.beta.generator.RelationshipDistribution;
import org.neo4j.graphalgo.config.RandomGraphGeneratorConfig;
import org.neo4j.graphalgo.core.Aggregation;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.neo4j.graphalgo.core.utils.progress.EmptyProgressEventTracker;
import org.neo4j.graphalgo.triangle.ImmutableTriangleCountStatsConfig;
import org.neo4j.graphalgo.triangle.IntersectingTriangleCount;
import org.neo4j.graphalgo.triangle.IntersectingTriangleCountFactory;
import org.neo4j.graphalgo.triangle.TriangleCountBaseConfig;
import org.neo4j.logging.NullLog;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

public class TriangleCountGraphImplBenchmark extends GraphImplBaseBenchmark {

    private TriangleCountBaseConfig config;

    @Param({"4"})
    private int concurrency;

    @Param({"3000000"})
    private int nodeCount;

    @Param({"4"})
    private int avgDegree;

    @Override
    CSRGraph getCSRGraph() {
        return RandomGraphGenerator.builder()
                .nodeCount(nodeCount)
                .averageDegree(avgDegree)
                .relationshipDistribution(RelationshipDistribution.POWER_LAW)
                .aggregation(Aggregation.SINGLE)
                .seed(42)
                .allowSelfLoops(RandomGraphGeneratorConfig.AllowSelfLoops.NO).build().generate();
    }

    @Override
    @Setup
    public void setup() {
        super.setup();

        config = ImmutableTriangleCountStatsConfig
                .builder()
                .concurrency(concurrency)
                .build();
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void compute(Blackhole bh) {
        IntersectingTriangleCount algorithm = new IntersectingTriangleCountFactory<>().build(
                graph,
                config,
                AllocationTracker.empty(),
                NullLog.getInstance(),
                EmptyProgressEventTracker.INSTANCE
        );

        bh.consume(algorithm.compute());
    }

}
