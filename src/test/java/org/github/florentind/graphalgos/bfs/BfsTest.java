

package org.github.florentind.graphalgos.bfs;

import com.github.fabianmurariu.unsafe.GRBCORE;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.Buffer;
import java.util.stream.Stream;

import static org.github.florentind.core.grapblas_native.NativeHelper.checkStatusCode;
import static org.github.florentind.graphalgos.bfs.BfsEjml.BfsVariation;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@SuppressWarnings({"UnusedMethod"})
public class BfsTest {
    private static final int EXPECTED_ITERATIONS = 3;
    private static final int START_NODE = 0;
    static DMatrixSparseCSC inputMatrixTransposed;

    BfsEjml bfs = new BfsEjml();
    private static final int MAX_ITERATIONS = 20;

    private static Stream<Arguments> bfsVariantSource() {

        return Stream.of(
                Arguments.of(BfsVariation.LEVEL, new double[]{1, 2, 3, 2, 3, 4, 3}),
                Arguments.of(BfsVariation.PARENTS, new double[]{1, 1, 4, 1, 2, 3, 2})
        );
    }

    @BeforeAll
    public static void setUp() {
        // based on example in http://mit.bme.hu/~szarnyas/grb/graphblas-introduction.pdf
        DMatrixSparseCSC inputMatrix = new DMatrixSparseCSC(7, 7, 12);
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
        inputMatrix.sortIndices(null);
        inputMatrixTransposed = CommonOps_DSCC.transpose(inputMatrix, null, null);
    }

    @ParameterizedTest
    @MethodSource("bfsVariantSource")
    public void testSparseEjmlVariations(BfsVariation variation, double[] expected) {
        BfsSparseResult result = bfs.computeSparse(inputMatrixTransposed, variation, START_NODE, MAX_ITERATIONS);

        assertBfsResult(expected, result);
        assertEquals(EXPECTED_ITERATIONS, result.iterations());
    }

    @ParameterizedTest
    @MethodSource("bfsVariantSource")
    public void testDenseEjmlVariations(BfsVariation variation, double[] expected) {
        var result = bfs.computeDense(CommonOps_DSCC.transpose(inputMatrixTransposed, null, null), variation, START_NODE, MAX_ITERATIONS);

        assertBfsResult(expected, result);
        assertEquals(EXPECTED_ITERATIONS, result.iterations());
    }

    @ParameterizedTest
    @MethodSource("bfsVariantSource")
    public void testSparseDenseEjmlVariations(BfsVariation variation, double[] expected) {
        var result = bfs.computeDenseSparse(inputMatrixTransposed, variation, START_NODE, MAX_ITERATIONS);

        assertBfsResult(expected, result);
        assertEquals(EXPECTED_ITERATIONS, result.iterations());
    }

    @Test
    public void testEmptyResult() {
        DMatrixSparseCSC emptyMatrix = new DMatrixSparseCSC(2, 2);
        emptyMatrix.sortIndices(null);
        BfsResult denseIt = bfs.computeDense(emptyMatrix, BfsVariation.LEVEL, 0, 5);

        BfsResult sparseIt = bfs.computeSparse(emptyMatrix, BfsVariation.LEVEL, START_NODE, 5);

        assertEquals(denseIt.iterations(), sparseIt.iterations());
        assertEquals(denseIt.nodesVisited(), sparseIt.nodesVisited());
    }

    @ParameterizedTest
    @MethodSource("bfsVariantSource")
    public void testNativeBfs(BfsVariation variation, double[] expected) {
        GRBCORE.initNonBlocking();

        Buffer nativeMatrix = ToNativeMatrixConverter.convert(inputMatrixTransposed);

        BfsResult result = null;

        switch (variation) {
            case LEVEL:
                result = new BfsNative().computeLevel(nativeMatrix, 0, 6, 1);
                break;
            case PARENTS:
                result = new BfsNative().computeParent(nativeMatrix, 0, 6, 1);
                break;
            default:
                fail();
        }

        checkStatusCode(GRBCORE.freeMatrix(nativeMatrix));
        checkStatusCode(GRBCORE.grbFinalize());

        // only level variant implemented atm
        assertBfsResult(expected, result);
        // as the result is set at the beginning of each iteration
        assertEquals(EXPECTED_ITERATIONS, result.iterations());
    }

    private void assertBfsResult(double[] expected, BfsResult result) {
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], result.get(i), "Different result at " + i);
        }
    }
}
