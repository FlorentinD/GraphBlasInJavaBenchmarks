package org.github.florentind.bench;

import org.junit.jupiter.api.Test;
import org.neo4j.graphalgo.beta.pregel.Pregel;
import org.neo4j.graphalgo.beta.pregel.pr.ImmutablePageRankPregelConfig;
import org.neo4j.graphalgo.beta.pregel.pr.PageRankPregel;
import org.neo4j.graphalgo.core.GdsEdition;
import org.neo4j.graphalgo.core.concurrency.ParallelUtil;
import org.neo4j.graphalgo.core.concurrency.Pools;
import org.neo4j.graphalgo.core.utils.ProgressLogger;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.neo4j.graphalgo.pagerank.ImmutablePageRankStreamConfig;
import org.neo4j.graphalgo.pagerank.PageRank;
import org.neo4j.graphalgo.pagerank.PageRankAlgorithmType;
import org.neo4j.graphalgo.pagerank.PageRankStreamConfig;

import java.util.stream.LongStream;

public class PageRankBenchmarkTest extends BaseBenchmarkTest {
    private static final int NODE_COUNT = 300_000;
    private static final int MAX_ITERATIONS = 5;

    @Override
    long nodeCount() {
        return NODE_COUNT;
    }

    @Override
    long avgDegree() {
        return 4;
    }

    // TODO:   check equal result (on generated graph!?)

    @Test
    void testPregel() {
        // TODO increase test heap space?
        GdsEdition.instance().setToEnterpriseEdition();
        int concurrency = 1;

        var config = ImmutablePageRankPregelConfig.builder()
                .maxIterations(MAX_ITERATIONS)
                .concurrency(concurrency)
                .build();

        var pageRankJob = Pregel.create(
                graph,
                config,
                new PageRankPregel(),
                Pools.DEFAULT,
                AllocationTracker.EMPTY
        );

        var result = pageRankJob.run();
        System.out.println("result.ranIterations() = " + result.ranIterations());
    }

    @Test
    void testGdsUnweightedPageRank() {
        PageRankStreamConfig config = ImmutablePageRankStreamConfig.builder()
                .maxIterations(40).build();

        PageRank pageRank = PageRankAlgorithmType.NON_WEIGHTED
                .create(graph, config, LongStream.empty(), ProgressLogger.NULL_LOGGER)
                .compute();

        // TODO assert against ejml variation (ranIterations and result)
    }
}
