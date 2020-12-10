package org.github.florentind.graphalgos.triangleCount;

import org.ejml.data.DMatrixSparseCSC;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TriangleCountTest {
    private static final int EXPECTED_TRIANGLE_COUNT = 5;
    private static final double[] EXPECTED_NODEWISE_TC = {1, 3, 2, 4, 1, 1, 3, 0};
    DMatrixSparseCSC inputMatrix;

    @BeforeEach
    public void setUp() {
        // based on example in http://mit.bme.hu/~szarnyas/grb/graphblas-introduction.pdf (undirected)
        inputMatrix = new DMatrixSparseCSC(8, 8, 24);
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
        var result = TriangleCountEjml.computeNodeWise(inputMatrix, useLowerTriangle);

        for (int i = 0; i < EXPECTED_NODEWISE_TC.length; i++) {
            assertEquals(EXPECTED_NODEWISE_TC[i], result.get(i));
        }
        assertEquals(EXPECTED_TRIANGLE_COUNT, result.totalCount());
    }
}
