package org.github.florentind.bench.triangleCount;

import org.github.florentind.bench.pageRank.PageRankPregelBenchmark;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.neo4j.graphalgo.core.utils.progress.EmptyProgressEventTracker;
import org.neo4j.graphalgo.triangle.ImmutableTriangleCountBaseConfig;
import org.neo4j.graphalgo.triangle.IntersectingTriangleCountFactory;
import org.neo4j.logging.NullLog;

import java.util.List;

public class TriangleCountGdsBenchmark extends TriangleCountBaseBenchmark {

    @Override
    protected List<Integer> concurrencies() {
        return List.of(1, 8);
    }

    @Override
    protected void benchmarkFunc(Integer concurrency) {
        var config = ImmutableTriangleCountBaseConfig
                .builder()
                .concurrency(concurrency)
                .build();

        new IntersectingTriangleCountFactory<>()
                .build(graph, config, AllocationTracker.empty(), NullLog.getInstance(), EmptyProgressEventTracker.INSTANCE)
                .compute();
    }

    public static void main(String[] args) {
        new TriangleCountGdsBenchmark().run();
    }
}
