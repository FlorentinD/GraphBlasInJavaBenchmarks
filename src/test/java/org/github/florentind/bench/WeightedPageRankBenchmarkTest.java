package org.github.florentind.bench;

import com.github.fabianmurariu.unsafe.GRBCORE;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.github.florentind.core.ejml.EjmlGraph;
import org.github.florentind.core.ejml.EjmlUtil;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.github.florentind.core.jgrapht.JGraphTConverter;
import org.github.florentind.graphalgos.pageRank.PageRankEjml;
import org.github.florentind.graphalgos.pageRank.PageRankNative;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.neo4j.graphalgo.beta.generator.PropertyProducer;
import org.neo4j.graphalgo.beta.pregel.Pregel;
import org.neo4j.graphalgo.beta.pregel.pr.ImmutablePageRankPregelConfig;
import org.neo4j.graphalgo.beta.pregel.pr.PageRankPregel;
import org.neo4j.graphalgo.core.concurrency.Pools;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.neo4j.graphalgo.core.utils.progress.EmptyProgressEventTracker;
import org.neo4j.graphalgo.pagerank.ImmutablePageRankStreamConfig;
import org.neo4j.graphalgo.pagerank.PageRank;
import org.neo4j.graphalgo.pagerank.PageRankFactory;
import org.neo4j.graphalgo.pagerank.PageRankStreamConfig;
import org.neo4j.logging.NullLog;

import java.nio.Buffer;

import static org.github.florentind.graphalgos.pageRank.ResultUtil.normalize;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class WeightedPageRankBenchmarkTest extends BaseBenchmarkTest {
    private static final int NODE_COUNT = 500_000;
    private static final int MAX_ITERATIONS = 13;
    private static final double DAMPING_FACTOR = 0.85;
    private static final double TOLERANCE = 1e-32;
    private static final int CONCURRENCY = 8;

    @Override
    long nodeCount() {
        return NODE_COUNT;
    }

    @Override
    long avgDegree() {
        return 4;
    }

    @Override
    PropertyProducer relationshipPropertyProducer() {
        return PropertyProducer.random("weight", 0, 1);
    }

    Triple<String, Integer, double[]> goldStandard;

    @BeforeEach
    public void setup() {
        super.setup();

        goldStandard = getGdsResult();

        assertEquals(MAX_ITERATIONS, goldStandard.getMiddle());
    }

    @Test
    void testJGraphT() {
        var jGraph = JGraphTConverter.convert(graph);
        var pageRanks = new org.jgrapht.alg.scoring.PageRank(jGraph, DAMPING_FACTOR, MAX_ITERATIONS, TOLERANCE).getScores();
        double[] jGraphTResult = pageRanks.values().stream().mapToDouble(v -> (Double) v).toArray();

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

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void testNative(boolean by_col) {
        GRBCORE.initNonBlocking();

        Buffer nativeMatrix = ToNativeMatrixConverter.convert(graph, by_col);
        var nativeResult = PageRankNative.computeWeighted(nativeMatrix, DAMPING_FACTOR, TOLERANCE, MAX_ITERATIONS, CONCURRENCY);

        GRBCORE.grbFinalize();

        assertEquals(goldStandard.getMiddle(), nativeResult.iterations());
        assertArrayEquals(goldStandard.getRight(), normalize(nativeResult.result()), 1e-2);
    }

    Triple<String, Integer, double[]> getEjmlResult() {
        EjmlGraph ejmlGraph = EjmlGraph.create(graph);
        var adjacencyMatrix = CommonOps_DSCC.transpose(ejmlGraph.matrix(), null, null);

        var result = new PageRankEjml().computeWeighted(adjacencyMatrix, DAMPING_FACTOR, TOLERANCE, MAX_ITERATIONS);

        return new ImmutableTriple<>("ejml", result.iterations(), normalize(result.result()));
    }

    Triple<String, Integer, double[]> getGdsResult() {
        PageRankStreamConfig weightedConfig = ImmutablePageRankStreamConfig.builder()
                .maxIterations(MAX_ITERATIONS)
                .dampingFactor(DAMPING_FACTOR)
                .tolerance(TOLERANCE)
                .concurrency(CONCURRENCY)
                .relationshipWeightProperty("weight")
                .build();

        PageRank pageRank = new PageRankFactory<>().build(
                graph,
                weightedConfig,
                AllocationTracker.empty(),
                NullLog.getInstance(),
                EmptyProgressEventTracker.INSTANCE
        );


        double[] normalizedResult = normalize(pageRank.compute().result().array().toArray());
        return new ImmutableTriple<>("gdsUnweighted", pageRank.iterations(), normalizedResult);
    }

    Triple<String, Integer, double[]> getPregelResult() {
        // normalize relationshipWeights (needed for pregel version)
        var normalizedGraph = EjmlUtil.normalizeOutgoingWeights(EjmlGraph.create(graph));


        var config = ImmutablePageRankPregelConfig.builder()
                .maxIterations(MAX_ITERATIONS)
                .dampingFactor(DAMPING_FACTOR)
                .relationshipWeightProperty("whatever")
                .concurrency(CONCURRENCY)
                .build();

        PageRankPregel computation = new PageRankPregel();
        var pageRankJob = Pregel.create(
                normalizedGraph,
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
