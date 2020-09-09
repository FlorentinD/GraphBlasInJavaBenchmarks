package org.github.florentind.graphalgos.triangleCount;

import org.ejml.data.DMatrixSparseCSC;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TriangleCountTest {
    private static final int EXPECTED_TRIANGLE_COUNT = 5;
    DMatrixSparseCSC inputMatrix;

    @BeforeEach
    public void setUp() {
        // based on example in http://mit.bme.hu/~szarnyas/grb/graphblas-introduction.pdf (undirected)
        inputMatrix = new DMatrixSparseCSC(7, 7, 24);
        inputMatrix.set(0, 1, 1);
        inputMatrix.set(0, 3, 1);
        inputMatrix.set(1, 3, 1);
        inputMatrix.set(1, 4, 1);
        inputMatrix.set(1, 6, 1);
        inputMatrix.set(2, 3, 1);
        inputMatrix.set(2, 5, 1);
        inputMatrix.set(2, 6, 1);
        inputMatrix.set(3, 5, 1);
        inputMatrix.set(3, 6, 1);
        inputMatrix.set(4, 5, 1);
        inputMatrix.set(4, 6, 1);

        // making matrix symmetric
        inputMatrix.copy().createCoordinateIterator().forEachRemaining(v -> inputMatrix.set(v.col, v.row, v.value));
    }


    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void triangleCountTotal(boolean useMask) {
        double cohenResult = TriangleCountEjml.computeTotalCohen(inputMatrix, useMask);
        double sandiaResult = TriangleCountEjml.computeTotalSandia(inputMatrix);

        assertEquals(EXPECTED_TRIANGLE_COUNT, cohenResult);
        assertEquals(cohenResult, sandiaResult);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    public void triangleCountNodeWise(boolean useLowerTriangle) {
        // FIX version with lower triangle
        double[] result = TriangleCountEjml.computeNodeWise(inputMatrix, useLowerTriangle).result;

        double[] expected = {1, 3, 2, 4, 1, 1, 3};

        assertArrayEquals(expected, result);
        assertEquals(EXPECTED_TRIANGLE_COUNT, Arrays.stream(result).sum() / 3);
    }
}
