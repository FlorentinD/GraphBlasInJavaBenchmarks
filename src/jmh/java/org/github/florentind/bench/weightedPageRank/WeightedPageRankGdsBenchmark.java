package org.github.florentind.bench.weightedPageRank;


import org.neo4j.graphalgo.core.utils.ProgressLogger;
import org.neo4j.graphalgo.pagerank.ImmutablePageRankStatsConfig;
import org.neo4j.graphalgo.pagerank.PageRank;
import org.neo4j.graphalgo.pagerank.PageRankAlgorithmType;
import org.neo4j.graphalgo.pagerank.PageRankBaseConfig;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.infra.Blackhole;

import java.util.stream.LongStream;

public class WeightedPageRankGdsBenchmark extends WeightedPageRankBaseBenchmark {

    @Param({"1", "8"})
    private int concurrency;

    private PageRankBaseConfig weightedConfig;

    @Override
    public void setup() {
        super.setup();
        weightedConfig = ImmutablePageRankStatsConfig
                .builder()
                .maxIterations(maxIterations)
                .dampingFactor(dampingFactor)
                .tolerance(tolerance)
                .concurrency(concurrency)
                .build();
    }


    @org.openjdk.jmh.annotations.Benchmark
    public void gds(Blackhole bh) {
        PageRank algorithm = PageRankAlgorithmType.WEIGHTED
                .create(graph, weightedConfig, LongStream.empty(), ProgressLogger.NULL_LOGGER);

        bh.consume(algorithm.compute());
    }
}
