package org.github.florentind.bench.triangleCount;

import org.neo4j.graphalgo.beta.pregel.Pregel;
import org.neo4j.graphalgo.beta.pregel.triangleCount.ImmutableTriangleCountPregelConfig;
import org.neo4j.graphalgo.beta.pregel.triangleCount.TriangleCountPregel;
import org.neo4j.graphalgo.core.concurrency.Pools;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.neo4j.graphalgo.triangle.ImmutableTriangleCountBaseConfig;
import org.neo4j.graphalgo.triangle.IntersectingTriangleCountFactory;
import org.neo4j.logging.NullLog;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.infra.Blackhole;

public class TriangleCountGdsBenchmark extends TriangleCountBaseBenchmark {
    @Param({"1", "8"})
    private int concurrency;


    @Benchmark
    public void gds(Blackhole bh) {
        var config = ImmutableTriangleCountBaseConfig
                .builder()
                .concurrency(concurrency)
                .build();

        bh.consume(new IntersectingTriangleCountFactory<>()
                .build(graph, config, AllocationTracker.empty(), NullLog.getInstance())
                .compute());
    }

    @Benchmark
    public void pregel(Blackhole bh) {
        var triangleCountJob = Pregel.create(
                graph,
                ImmutableTriangleCountPregelConfig.builder()
                        .concurrency(concurrency)
                        .build(),
                new TriangleCountPregel(),
                Pools.DEFAULT,
                AllocationTracker.empty()
        );

        bh.consume(triangleCountJob.run());
    }
}