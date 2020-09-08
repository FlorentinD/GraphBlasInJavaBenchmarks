package org.github.florentind.core.grapblas_native;

import com.github.fabianmurariu.unsafe.GRAPHBLAS;
import org.ejml.data.DMatrixSparseCSC;

import java.nio.Buffer;

import com.github.fabianmurariu.unsafe.GRBCORE;

/**
 * converting a DMatrixSparseCSC into a GraphBLAS matrix (returning a buffer)
 */
public class EjmlToNativeMatrixConverter {

    /**
     *
     * @param matrix Input matrix
     * @return Pointer to the GraphBLAS matrix object
     */
    public static Buffer convert(DMatrixSparseCSC matrix) {
        int nodeCount = matrix.numCols;
        assert matrix.numRows == nodeCount : "adjacency matrix has to be a square matrix";

        int relCount = matrix.nz_length;

        long[] colIds = new long[relCount];
        long[] rowIds = new long[relCount];
        double[] values = new double[relCount];

        int index = 0;

        var iterator = matrix.createCoordinateIterator();

        Buffer resultMatrix = GRBCORE.createMatrix(GRAPHBLAS.doubleType(), nodeCount, nodeCount);

        while(iterator.hasNext()) {
            var triple = iterator.next();
            colIds[index] = triple.col;
            rowIds[index] = triple.row;
            values[index] = triple.value;

            index++;
        }

        long statusCode = GRAPHBLAS.buildMatrixFromTuplesDouble(resultMatrix, rowIds, colIds, values, relCount, GRAPHBLAS.firstBinaryOpDouble());
        assert statusCode == GRBCORE.GrB_SUCCESS : "Status code was: " + statusCode;

        // as ejml also uses CSC
        GRBCORE.makeCSC(resultMatrix);

        return resultMatrix;
    }
}
