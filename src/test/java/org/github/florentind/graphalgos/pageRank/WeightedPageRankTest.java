package org.github.florentind.graphalgos.pageRank;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.github.florentind.graphalgos.pageRank.ResultUtil.normalize;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings({"UnusedMethod"})
public class WeightedPageRankTest {
    private static final double DAMPING_FACTOR = PageRankGraphalyticsEjml.DEFAULT_DAMPING_FACTOR;
    private static final float TOLERANCE = PageRankGraphalyticsEjml.DEFAULT_TOLERANCE;
    private static final int MAX_ITERATIONS = PageRankGraphalyticsEjml.DEFAULT_MAX_ITERATIONS;

    private static Stream<Arguments> weightedGraphs() {
        DMatrixSparseCSC equallyWeighted = new DMatrixSparseCSC(10, 10, 9);
        equallyWeighted.set(1, 2, 1);
        equallyWeighted.set(2, 1, 1);
        equallyWeighted.set(3, 0, 0.5);
        equallyWeighted.set(3, 1, 0.5);
        equallyWeighted.set(4, 1, 1 / 3.0);
        equallyWeighted.set(4, 3, 1 / 3.0);
        equallyWeighted.set(4, 5, 1 / 3.0);
        equallyWeighted.set(5, 1, 0.5);
        equallyWeighted.set(5, 4, 0.5);

        DMatrixSparseCSC weighted = new DMatrixSparseCSC(10, 10, 9);
        weighted.set(1, 2, 1);
        weighted.set(2, 1, 1);
        weighted.set(3, 0, 0.3);
        weighted.set(3, 1, 0.7);
        weighted.set(4, 1, 0.9);
        weighted.set(4, 3, 0.05);
        weighted.set(4, 5, 0.05);
        weighted.set(5, 1, 0.9);
        weighted.set(5, 4, 0.1);

        DMatrixSparseCSC nonNormalizedMatrix = weighted.copy();
        CommonOps_DSCC.apply(nonNormalizedMatrix, (a) -> a * 2);

        double[] scoresForWeighted = {
                0.03445316351988414,
                0.4016725956992126,
                0.3685557854413577,
                0.02845462368074553,
                0.029615183932254104,
                0.02845462368074553,
                0.02719850601145006,
                0.02719850601145006,
                0.02719850601145006,
                0.02719850601145006
        };

        return Stream.of(
                Arguments.of("equally weighted edges", equallyWeighted, new double[]{
                        0.046528597509547166,
                        0.36731632503597234,
                        0.3409378593057864,
                        0.04190325202551531,
                        0.046528597509547166,
                        0.04190325202551531,
                        0.028720529147028995,
                        0.028720529147028995,
                        0.028720529147028995,
                        0.028720529147028995}
                ),
                Arguments.of("non equally weighted", weighted, scoresForWeighted),
                Arguments.of("non normalized", nonNormalizedMatrix, scoresForWeighted)
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("weightedGraphs")
    public void pageRankGraphAlyticsEjml(String desc, DMatrixSparseCSC adjMatrix, double[] expectedScores) {
        PageRankResult result = new PageRankGraphalyticsEjml().computeWeighted(
                adjMatrix,
                DAMPING_FACTOR,
                TOLERANCE,
                MAX_ITERATIONS
        );


        assertEquals(20, result.iterations());
        // other tolerance as maxIterations reached and not tolerance
        assertArrayEquals(expectedScores, result.result(), 1e-2f);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("weightedGraphs")
    public void pageRankEjml(String desc, DMatrixSparseCSC adjMatrix, double[] expectedScores) {
        PageRankResult result = new PageRankEjml().computeWeighted(
                adjMatrix,
                DAMPING_FACTOR,
                TOLERANCE,
                MAX_ITERATIONS
        );


        assertEquals(20, result.iterations());
        // other tolerance as maxIterations reached and not tolerance
        assertArrayEquals(expectedScores, normalize(result.result()), 1e-2f);
    }
}
