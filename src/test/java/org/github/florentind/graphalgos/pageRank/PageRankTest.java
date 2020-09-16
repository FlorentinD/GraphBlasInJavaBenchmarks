package org.github.florentind.graphalgos.pageRank;

import com.github.fabianmurariu.unsafe.GRBCORE;
import org.ejml.data.DMatrixSparseCSC;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.Buffer;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings({"UnusedMethod"})
public class PageRankTest {
    DMatrixSparseCSC inputMatrix;

    PageRankEjml pageRank = new PageRankEjml();

    @BeforeEach
    public void setUp() {
        inputMatrix = new DMatrixSparseCSC(10, 10, 9);
        inputMatrix.set(1, 2, 1);
        inputMatrix.set(2, 1, 1);
        inputMatrix.set(3, 0, 1);
        inputMatrix.set(3, 1, 1);
        inputMatrix.set(4, 1, 1);
        inputMatrix.set(4, 3, 1);
        inputMatrix.set(5, 1, 1);
        inputMatrix.set(5, 4, 1);
    }

    @Test
    public void pageRankEjml() {
        PageRankResult result = pageRank.compute(
                inputMatrix,
                PageRankEjml.DEFAULT_DAMPING_FACTOR,
                PageRankEjml.DEFAULT_TOLERANCE,
                PageRankEjml.DEFAULT_MAX_ITERATIONS
        );

        double[] expected = {
                0.04881240953046283,
                0.37252731373997194,
                0.34566197629884704,
                0.04658515322643894,
                0.04134455015814745,
                0.029013719409226278,
                0.029013719409226278,
                0.029013719409226278,
                0.029013719409226278,
                0.029013719409226278
        };

        assertEquals(20, result.iterations());
        // other tolerance as maxIterations reached and not tolerance
        assertArrayEquals(expected, result.result(), 1e-2f);
    }

    @Test
    public void pageRankNative() {
        GRBCORE.initNonBlocking();

        Buffer nativeMatrix = ToNativeMatrixConverter.convert(inputMatrix);

        PageRankResult result = PageRankNative.compute(
                nativeMatrix,
                PageRankEjml.DEFAULT_DAMPING_FACTOR,
                PageRankEjml.DEFAULT_TOLERANCE,
                PageRankEjml.DEFAULT_MAX_ITERATIONS,
                1
        );

        double[] expected = {
                0.04881240953046283,
                0.37252731373997194,
                0.34566197629884704,
                0.04658515322643894,
                0.04134455015814745,
                0.029013719409226278,
                0.029013719409226278,
                0.029013719409226278,
                0.029013719409226278,
                0.029013719409226278
        };

        assertEquals(20, result.iterations());
        // other tolerance as maxIterations reached and not tolerance
        assertArrayEquals(expected, result.result(), 1e-2f);

        GRBCORE.grbFinalize();
    }

    // TODO test other pageRank impl. with a graph with only dangling nodes (compute2)
}
