package org.github.florentind.core.grapblas_native;

import com.github.fabianmurariu.unsafe.GRAPHBLAS;
import com.github.fabianmurariu.unsafe.GRBCORE;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.DVectorSparse;
import org.github.florentind.core.ejml.EjmlRelationships;
import org.neo4j.graphalgo.api.Graph;

import java.nio.Buffer;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * converting a DMatrixSparseCSC into a GraphBLAS matrix (returning a buffer)
 */
public class ToNativeVectorConverter {

    /**
     * @param vector Input vector
     * @return Pointer to the GraphBLAS vector object
     */
    public static Buffer convert(DVectorSparse vector) {
        int nzCount = vector.nz_length();

        long[] indices = new long[nzCount];
        double[] values = new double[nzCount];

        int index = 0;

        var iterator = vector.createIterator();

        Buffer resultMatrix = GRBCORE.createVector(GRAPHBLAS.doubleType(), vector.size());

        while (iterator.hasNext()) {
            var tuple = iterator.next();
            indices[index] = tuple.index;
            values[index] = tuple.value;
            index++;
        }

        long statusCode = GRAPHBLAS.buildVectorFromTuplesDouble(resultMatrix, indices, values, nzCount, GRAPHBLAS.firstBinaryOpDouble());
        assert statusCode == GRBCORE.GrB_SUCCESS : "Status code was: " + statusCode + " Did you call GrB.init()?";

        return resultMatrix;
    }
}
