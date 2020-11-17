package org.github.florentind.bench.matrixOps;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.DVectorSparse;
import org.ejml.ops.DSemiRings;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.ejml.sparse.csc.mult.MatrixSparseVectorMultWithSemiRing_DSCC;
import org.ejml.sparse.csc.mult.MatrixVectorMult_DSCC;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VxMBenchmarksTest {
    protected DMatrixSparseCSC matrix;

    private int dimension = 100_000;

    private int avgDegree = 4;

    private String degreeDistribution = "UNIFORM";

    @BeforeEach
    public void setup() {
        matrix = RandomMatrices_DSCC.generateUniform(dimension, dimension, avgDegree, 1, 2,new Random(42));
    }

    @ParameterizedTest
    @ValueSource(floats = {0.01f, 0.2f, 0.8f})
    public void testVxM(float density) {
        double[] denseInputVector = new double[matrix.numRows];
        double[] denseOutput = new double[matrix.numRows];
        // fast init and actual values are not relevant for the benchmark

        int nonZeroElements = Math.round(density * matrix.numCols);

        var rand = new Random(99);

        for (int i = 0; i < nonZeroElements; i++) {
            int index = rand.nextInt(denseInputVector.length);
            while(denseInputVector[index] != 0) {
                index = rand.nextInt(denseInputVector.length);
            }
            denseInputVector[index] = rand.nextDouble() + 0.1;
        }

        DVectorSparse sparseVector = new DVectorSparse(denseInputVector, 0);
        DMatrixSparseCSC sparse1DimMatrix = CommonOps_DSCC.transpose(sparseVector.oneDimMatrix, null, null);

        assertEquals(nonZeroElements, sparse1DimMatrix.nz_length);
        assertEquals(nonZeroElements, sparseVector.nz_length());

        MatrixVectorMult_DSCC.mult(denseInputVector, 0, matrix, denseOutput, 0);
        var sparseVectorOutput = MatrixSparseVectorMultWithSemiRing_DSCC.mult(sparseVector, matrix, null, DSemiRings.PLUS_TIMES);
        var sparseMatrixOutput = CommonOps_DSCC.mult(sparse1DimMatrix, matrix, null);

        for (int i = 0; i < denseOutput.length; i++) {
            assertEquals(denseOutput[i], sparseMatrixOutput.get(0, i, 0));
            assertEquals(denseOutput[i], sparseVectorOutput.get(i, 0));
        }
    }
}
