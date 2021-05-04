package org.github.florentind.bench.triangleCount;

import org.neo4j.graphalgo.beta.pregel.Partitioning;
import org.neo4j.graphalgo.beta.pregel.Pregel;
import org.neo4j.graphalgo.beta.pregel.triangleCount.ImmutableTriangleCountPregelConfig;
import org.neo4j.graphalgo.beta.pregel.triangleCount.TriangleCountPregel;
import org.neo4j.graphalgo.core.concurrency.Pools;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;

import java.util.List;

public class TriangleCountVertexWisePregelBenchmark extends TriangleCountBaseBenchmark {

    @Override
    protected List<Integer> concurrencies() {
        return List.of(1, 8);
    }

    @Override
    protected void benchmarkFunc(Integer concurrency) {
        var triangleCountJob = Pregel.create(
                graph,
                ImmutableTriangleCountPregelConfig.builder()
                        .concurrency(concurrency)
                        .partitioning(Partitioning.DEGREE)
                        .build(),
                new TriangleCountPregel(),
                Pools.DEFAULT,
                AllocationTracker.empty()
        );

        triangleCountJob.run();
    }

    public static void main(String[] args) {
        new TriangleCountVertexWisePregelBenchmark().run();
    }
}
