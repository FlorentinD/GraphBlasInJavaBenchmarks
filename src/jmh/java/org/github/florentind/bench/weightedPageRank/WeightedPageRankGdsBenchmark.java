package org.github.florentind.bench.weightedPageRank;


import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.neo4j.graphalgo.core.utils.progress.EmptyProgressEventTracker;
import org.neo4j.graphalgo.pagerank.ImmutablePageRankStatsConfig;
import org.neo4j.graphalgo.pagerank.PageRank;
import org.neo4j.graphalgo.pagerank.PageRankBaseConfig;
import org.neo4j.graphalgo.pagerank.PageRankFactory;
import org.neo4j.logging.NullLog;

import java.util.List;

public class WeightedPageRankGdsBenchmark extends WeightedPageRankBaseBenchmark {


    @Override
    protected List<Integer> concurrencies() {
        return List.of(1, 8);
    }

    private PageRankBaseConfig weightedConfig;

    @Override
    public void setup(String dataset) {
        super.setup(dataset);
    }

    @Override
    protected void benchmarkFunc(Integer concurrency) {
        weightedConfig = ImmutablePageRankStatsConfig
                .builder()
                .maxIterations(maxIterations)
                .dampingFactor(dampingFactor)
                .tolerance(tolerance)
                .concurrency(concurrency)
                .relationshipWeightProperty(REL_PROPERTY_NAME)
                .build();

        PageRank algorithm = new PageRankFactory<>().build(
                graph,
                weightedConfig,
                AllocationTracker.empty(),
                NullLog.getInstance(),
                EmptyProgressEventTracker.INSTANCE
        );

        algorithm.compute();
    }
}
