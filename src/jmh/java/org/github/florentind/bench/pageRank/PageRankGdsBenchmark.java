package org.github.florentind.bench.pageRank;


import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.neo4j.graphalgo.core.utils.progress.EmptyProgressEventTracker;
import org.neo4j.graphalgo.pagerank.ImmutablePageRankStatsConfig;
import org.neo4j.graphalgo.pagerank.PageRank;
import org.neo4j.graphalgo.pagerank.PageRankBaseConfig;
import org.neo4j.graphalgo.pagerank.PageRankFactory;
import org.neo4j.logging.NullLog;

import java.util.List;


public class PageRankGdsBenchmark extends PageRankBaseBenchmark {

    @Override
    protected List<Integer> concurrencies() {
        return List.of(1, 8);
    }

    private PageRankBaseConfig unweightedConfig;

    @Override
    public void setup(String dataset) {
        super.setup(dataset);
    }

    @Override
    protected void benchmarkFunc(Integer concurrency) {
        unweightedConfig = ImmutablePageRankStatsConfig
                .builder()
                .maxIterations(maxIterations)
                .dampingFactor(dampingFactor)
                .tolerance(tolerance)
                .concurrency(concurrency)
                .build();

        PageRank algorithm = new PageRankFactory<>().build(
                graph,
                unweightedConfig,
                AllocationTracker.empty(),
                NullLog.getInstance(),
                EmptyProgressEventTracker.INSTANCE
        );

        algorithm.compute();
    }

    public static void main(String[] args) {
        new PageRankGdsBenchmark().run();
    }
}
