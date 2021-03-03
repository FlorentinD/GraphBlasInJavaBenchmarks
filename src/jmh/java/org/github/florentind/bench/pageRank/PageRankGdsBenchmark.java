package org.github.florentind.bench.pageRank;


import org.github.florentind.graphalgos.pageRank.PageRankEjml;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.neo4j.graphalgo.core.utils.progress.EmptyProgressEventTracker;
import org.neo4j.graphalgo.pagerank.ImmutablePageRankStatsConfig;
import org.neo4j.graphalgo.pagerank.PageRank;
import org.neo4j.graphalgo.pagerank.PageRankBaseConfig;
import org.neo4j.graphalgo.pagerank.PageRankFactory;
import org.neo4j.logging.NullLog;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;


public class PageRankGdsBenchmark extends PageRankBaseBenchmark {

    @Param({"1", "8"})
    private int concurrency;

    private PageRankBaseConfig unweightedConfig;

    @Override
    public void setup() {
        super.setup();

        unweightedConfig = ImmutablePageRankStatsConfig
                .builder()
                .maxIterations(maxIterations)
                .dampingFactor(dampingFactor)
                .tolerance(tolerance)
                .concurrency(concurrency)
                .build();
    }

    @Override
    protected void benchmarkFunc() {
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
