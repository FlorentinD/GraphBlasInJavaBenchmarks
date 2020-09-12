package org.github.florentind.bench;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.github.florentind.core.ejml.EjmlGraph;
import org.github.florentind.graphalgos.pageRank.PageRankEjml;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.neo4j.graphalgo.beta.pregel.Pregel;
import org.neo4j.graphalgo.beta.pregel.pr.ImmutablePageRankPregelConfig;
import org.neo4j.graphalgo.beta.pregel.pr.PageRankPregel;
import org.neo4j.graphalgo.core.concurrency.Pools;
import org.neo4j.graphalgo.core.utils.ProgressLogger;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.neo4j.graphalgo.pagerank.ImmutablePageRankStreamConfig;
import org.neo4j.graphalgo.pagerank.PageRank;
import org.neo4j.graphalgo.pagerank.PageRankAlgorithmType;
import org.neo4j.graphalgo.pagerank.PageRankStreamConfig;

import java.util.Arrays;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PageRankBenchmarkTest extends BaseBenchmarkTest {
    private static final int NODE_COUNT = 300_000;
    private static final int MAX_ITERATIONS = 20;
    private static final double DAMPING_FACTOR = 0.85;
    private static final double TOLERANCE = 1e-7;
    private static final int CONCURRENCY = 1;

    @Override
    long nodeCount() {
        return NODE_COUNT;
    }

    @Override
    long avgDegree() {
        return 4;
    }

    @Disabled
    @Test
    void testPregel() {
        var result = getPregelResult();

//        System.out.println("result.ranIterations() = " + result.getLeft());
//        System.out.println("statistics = " + Arrays.stream(result.getRight()).summaryStatistics());
    }

    @Disabled
    @Test
    void testGdsUnweightedPageRank() {
        var result = getGdsResult();

        System.out.println("iterations = " + result.getMiddle());
        System.out.println("statistics = " + Arrays.stream(result.getRight()).summaryStatistics());
    }

    @Disabled
    @Test
    void testEjml() {
        var result = getEjmlResult();

//        System.out.println("result.iterations() = " + result.getMiddle());
//        System.out.println("statistics = " + Arrays.stream(result.getRight()).summaryStatistics());
    }

    @Test
    void resultEquivalence() {
        var gdsResult = getGdsResult();
        var pregelResult = getPregelResult();
        var ejmlResult = getEjmlResult();

        // iterations ran
        assertEquals(pregelResult.getMiddle(), ejmlResult.getMiddle());
        assertEquals(pregelResult.getMiddle(), gdsResult.getMiddle());

        assertArrayEquals(pregelResult.getRight(), ejmlResult.getRight(), 1e-2);
        assertArrayEquals(gdsResult.getRight(), ejmlResult.getRight(), 1e-2);
    }

    Triple<String, Integer, double[]> getEjmlResult() {
        EjmlGraph ejmlGraph = EjmlGraph.create(graph);
        var adjacencyMatrix = CommonOps_DSCC.transpose(ejmlGraph.matrix(), null, null);

        var result = new PageRankEjml().compute(adjacencyMatrix, DAMPING_FACTOR, TOLERANCE, MAX_ITERATIONS);

        return new ImmutableTriple<>("ejml", result.iterations(), result.result());
    }

    Triple<String, Integer, double[]> getGdsResult() {
        PageRankStreamConfig config = ImmutablePageRankStreamConfig.builder()
                .maxIterations(MAX_ITERATIONS)
                .dampingFactor(DAMPING_FACTOR)
                .tolerance(TOLERANCE)
                .build();

        PageRank pageRank = PageRankAlgorithmType.NON_WEIGHTED
                .create(graph, config, LongStream.empty(), ProgressLogger.NULL_LOGGER)
                .compute();

        double[] normalizedResult = normalizeResult(pageRank.result().array().toArray());
        return new ImmutableTriple<>("gdsUnweighted", pageRank.iterations(), normalizedResult);
    }

    Triple<String, Integer, double[]> getPregelResult() {
        var config = ImmutablePageRankPregelConfig.builder()
                .maxIterations(MAX_ITERATIONS)
                .dampingFactor(DAMPING_FACTOR)
                .concurrency(CONCURRENCY)
                .build();

        PageRankPregel computation = new PageRankPregel();
        var pageRankJob = Pregel.create(
                graph,
                config,
                computation,
                Pools.DEFAULT,
                AllocationTracker.empty()
        );

        var result = pageRankJob.run();
        String propertyName = computation.nodeSchema().elements().get(0).propertyKey();
        double[] normalizedResult = normalizeResult(result.nodeValues().doubleProperties(propertyName).toArray());
        return new ImmutableTriple<>("pregel", result.ranIterations(), normalizedResult);
    }

    double[] normalizeResult(double[] result) {
        var sum = Arrays.stream(result).sum();
        return Arrays.stream(result).map(x -> x / sum).toArray();
    }

}
