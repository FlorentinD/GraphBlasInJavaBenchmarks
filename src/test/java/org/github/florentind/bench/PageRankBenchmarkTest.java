package org.github.florentind.bench;

import com.github.fabianmurariu.unsafe.GRBCORE;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.github.florentind.core.ejml.EjmlGraph;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.github.florentind.core.jgrapht.JGraphTConverter;
import org.github.florentind.graphalgos.pageRank.PageRankEjml;
import org.github.florentind.graphalgos.pageRank.PageRankGraphalyticsEjml;
import org.github.florentind.graphalgos.pageRank.PageRankNative;
import org.github.florentind.graphalgos.pageRank.ResultUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.graphalgo.beta.pregel.Pregel;
import org.neo4j.graphalgo.beta.pregel.pr.ImmutablePageRankPregelConfig;
import org.neo4j.graphalgo.beta.pregel.pr.PageRankPregel;
import org.neo4j.graphalgo.core.concurrency.Pools;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.neo4j.graphalgo.pagerank.ImmutablePageRankStreamConfig;
import org.neo4j.graphalgo.pagerank.PageRank;
import org.neo4j.graphalgo.pagerank.PageRankFactory;
import org.neo4j.graphalgo.pagerank.PageRankStreamConfig;
import org.neo4j.logging.NullLog;

import java.nio.Buffer;

import static org.github.florentind.graphalgos.pageRank.ResultUtil.normalize;
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

    Triple<String, Integer, double[]> goldStandard;

    @Override
    @BeforeEach
    public void setup() {
        super.setup();
        goldStandard = getGdsResult();

        assertEquals(goldStandard.getMiddle(), MAX_ITERATIONS);
    }

    @Test
    void testJGraphT() {
        var jGraph = JGraphTConverter.convert(graph);
        var pageRanks = new org.jgrapht.alg.scoring.PageRank(jGraph).getScores();
        double[] jGraphTResult = pageRanks.values().stream().mapToDouble(v -> (Double) v).toArray();

        // jGraphT does not return the actual needed iterations unfortunately
        assertArrayEquals(goldStandard.getRight(), jGraphTResult, 1e-2);
    }

    @Test
    void testEjml() {
        var ejmlResult = getEjmlResult();

        assertEquals(goldStandard.getMiddle(), ejmlResult.getMiddle());
        assertArrayEquals(goldStandard.getRight(), ejmlResult.getRight(), 1e-2);
    }

    @Test
    void testPregel() {
        var pregelResult = getPregelResult();

        assertEquals(goldStandard.getMiddle(), pregelResult.getMiddle());
        assertArrayEquals(goldStandard.getRight(), pregelResult.getRight(), 1e-2);
    }

    @Test
    void testNative() {
        GRBCORE.initNonBlocking();

        Buffer nativeMatrix = ToNativeMatrixConverter.convert(graph, true);
        var nativeResult = PageRankNative.compute(nativeMatrix, DAMPING_FACTOR, TOLERANCE, MAX_ITERATIONS, CONCURRENCY);

        GRBCORE.grbFinalize();

        assertEquals(goldStandard.getMiddle(), nativeResult.iterations());
        assertArrayEquals(goldStandard.getRight(), normalize(nativeResult.result()), 1e-2);
    }

    Triple<String, Integer, double[]> getEjmlResult() {
        EjmlGraph ejmlGraph = EjmlGraph.create(graph);
        var adjacencyMatrix = CommonOps_DSCC.transpose(ejmlGraph.matrix(), null, null);

        var result = new PageRankEjml().compute(adjacencyMatrix, DAMPING_FACTOR, TOLERANCE, MAX_ITERATIONS);

        return new ImmutableTriple<>("ejml", result.iterations(), normalize(result.result()));
    }

    Triple<String, Integer, double[]> getGdsResult() {
        PageRankStreamConfig config = ImmutablePageRankStreamConfig.builder()
                .maxIterations(MAX_ITERATIONS)
                .dampingFactor(DAMPING_FACTOR)
                .tolerance(TOLERANCE)
                .concurrency(CONCURRENCY)
                .build();

        PageRank pageRank = new PageRankFactory<>().build(
                graph,
                config,
                AllocationTracker.empty(),
                NullLog.getInstance()
        );

        pageRank.compute();

        double[] normalizedResult = normalize(pageRank.result().array().toArray());
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
        String propertyName = "pagerank";
        double[] normalizedResult = normalize(result.nodeValues().doubleProperties(propertyName).toArray());
        return new ImmutableTriple<>("pregel", result.ranIterations(), normalizedResult);
    }
}
