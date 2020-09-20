

package org.github.florentind.graphalgos.bfs;

import com.github.fabianmurariu.unsafe.GRBCORE;
import org.ejml.data.DMatrixSparseCSC;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.Buffer;
import java.util.stream.Stream;

import static org.github.florentind.graphalgos.bfs.BfsEjml.BfsVariation;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings({"UnusedMethod"})
public class BfsTest {
    private static final int EXPECTED_ITERATIONS = 3;
    DMatrixSparseCSC inputMatrix;

    BfsEjml bfs = new BfsEjml();

    private static Stream<Arguments> bfsVariantSource() {

        return Stream.of(
                Arguments.of(BfsVariation.BOOLEAN, new double[]{1, 1, 1, 1, 1, 1, 1}),
                Arguments.of(BfsVariation.LEVEL, new double[]{1, 2, 3, 2, 3, 4, 3}),
                Arguments.of(BfsVariation.PARENTS, new double[]{1, 1, 4, 1, 2, 3, 2})

        );
    }

    @BeforeEach
    public void setUp() {
        // based on example in http://mit.bme.hu/~szarnyas/grb/graphblas-introduction.pdf
        inputMatrix = new DMatrixSparseCSC(7, 7, 12);
        inputMatrix.set(0, 1, 1);
        inputMatrix.set(0, 3, 1);
        inputMatrix.set(1, 4, 1);
        inputMatrix.set(1, 6, 1);
        inputMatrix.set(2, 5, 1);
        inputMatrix.set(3, 0, 0.2);
        inputMatrix.set(3, 2, 0.4);
        inputMatrix.set(4, 5, 1);
        inputMatrix.set(5, 2, 0.5);
        inputMatrix.set(6, 2, 1);
        inputMatrix.set(6, 3, 1);
        inputMatrix.set(6, 4, 1);
    }

    @ParameterizedTest
    @MethodSource("bfsVariantSource")
    public void testSparseEjmlVariations(BfsVariation variation, double[] expected) {
        int[] startNodes = {0};
        int maxIterations = 20;
        BfsEjml.BfsSparseResult result = bfs.computeSparse(inputMatrix, variation, startNodes, maxIterations);

        assertBfsResult(expected, result);
        assertEquals(EXPECTED_ITERATIONS, result.iterations());
    }

    @ParameterizedTest
    @MethodSource("bfsVariantSource")
    public void testDenseEjmlVariations(BfsVariation variation, double[] expected) {
        int startNode = 0;
        int maxIterations = 20;

        var result = bfs.computeDense(inputMatrix, variation, startNode, maxIterations);

        assertBfsResult(expected, result);
        assertEquals(EXPECTED_ITERATIONS, result.iterations());
    }

    @Test
    public void testEmptyResult() {
        BfsResult denseIt = bfs.computeDense(new DMatrixSparseCSC(2, 2), BfsVariation.LEVEL, 0, 5);

        int[] startNodes = {0};
        BfsResult sparseIt = bfs.computeSparse(new DMatrixSparseCSC(2, 2), BfsVariation.LEVEL, startNodes, 5);

        assertEquals(denseIt.iterations(), sparseIt.iterations());
        assertEquals(denseIt.nodesVisited(), sparseIt.nodesVisited());
    }

    @Test
    public void testNativeBfs() {
        GRBCORE.initNonBlocking();

        Buffer nativeMatrix = ToNativeMatrixConverter.convert(inputMatrix);

        var result = new BfsNative().computeLevel(nativeMatrix, 0, 6, 1);

        GRBCORE.freeMatrix(nativeMatrix);
        GRBCORE.grbFinalize();

        // only level variant implemented atm
        assertBfsResult(new double[]{1, 2, 3, 2, 3, 4, 3}, result);
        // as the result is set at the beginning of each iteration
        assertEquals(EXPECTED_ITERATIONS + 1, result.iterations());
    }

    private void assertBfsResult(double[] expected, BfsResult result) {
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], result.get(i), "Different result at " + i);
        }
    }
}
