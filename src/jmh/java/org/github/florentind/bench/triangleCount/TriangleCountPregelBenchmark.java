package org.github.florentind.bench.triangleCount;

import org.neo4j.graphalgo.beta.pregel.Pregel;
import org.neo4j.graphalgo.beta.pregel.triangleCount.ImmutableTriangleCountPregelConfig;
import org.neo4j.graphalgo.beta.pregel.triangleCount.TriangleCountPregel;
import org.neo4j.graphalgo.core.concurrency.Pools;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.infra.Blackhole;

public class TriangleCountPregelBenchmark extends TriangleCountBaseBenchmark {
    @Param({"1", "8"})
    private int concurrency;

    @Benchmark
    public void pregelNodeWise(Blackhole bh) {
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
