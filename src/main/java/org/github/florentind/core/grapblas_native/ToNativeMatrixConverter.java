package org.github.florentind.core.grapblas_native;

import com.github.fabianmurariu.unsafe.GRAPHBLAS;
import com.github.fabianmurariu.unsafe.GRBCORE;
import org.ejml.data.DMatrixSparseCSC;
import org.github.florentind.core.ejml.EjmlRelationships;
import org.neo4j.graphalgo.api.Graph;

import java.nio.Buffer;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * converting a DMatrixSparseCSC into a GraphBLAS matrix (returning a buffer)
 */
public class ToNativeMatrixConverter {

    public static Buffer convert(DMatrixSparseCSC matrix) {
        // as ejml uses CSC
        return convert(matrix, true);
    }

    /**
     * @param matrix Input matrix
     * @param by_col true -> CSC matrix, else CSR matrix
     * @return Pointer to the GraphBLAS matrix object
     */
    public static Buffer convert(DMatrixSparseCSC matrix, boolean by_col) {
        int nodeCount = matrix.numCols;
        assert matrix.numRows == nodeCount : "adjacency matrix has to be a square matrix";

        if (by_col) {
            GRBCORE.setGlobalInt(GRBCORE.GxB_FORMAT, GRBCORE.GxB_BY_COL);
        } else {
            GRBCORE.setGlobalInt(GRBCORE.GxB_FORMAT, GRBCORE.GxB_BY_ROW);
        }

        int relCount = matrix.nz_length;

        long[] colIds = new long[relCount];
        long[] rowIds = new long[relCount];
        double[] values = new double[relCount];

        int index = 0;

        var iterator = matrix.createCoordinateIterator();

        Buffer resultMatrix = GRBCORE.createMatrix(GRAPHBLAS.doubleType(), nodeCount, nodeCount);

        while (iterator.hasNext()) {
            var triple = iterator.next();
            colIds[index] = triple.col;
            rowIds[index] = triple.row;
            values[index] = triple.value;

            index++;
        }

        long statusCode = GRAPHBLAS.buildMatrixFromTuplesDouble(resultMatrix, rowIds, colIds, values, relCount, GRAPHBLAS.firstBinaryOpDouble());
        assert statusCode == GRBCORE.GrB_SUCCESS : "Status code was: " + statusCode + " Did you call GrB.init()?";

        return resultMatrix;
    }

    public static Buffer convert(Graph graph) {
        // as ejml uses CSC
        return convert(graph, true);
    }

    /**
     * @param graph  Input graph
     * @param by_col true -> CSC matrix, else CSR matrix
     * @return Pointer to the GraphBLAS matrix object
     */
    public static Buffer convert(Graph graph, boolean by_col) {
        if (by_col) {
            GRBCORE.setGlobalInt(GRBCORE.GxB_FORMAT, GRBCORE.GxB_BY_COL);
        } else {
            GRBCORE.setGlobalInt(GRBCORE.GxB_FORMAT, GRBCORE.GxB_BY_ROW);
        }

        int relCount = Math.toIntExact(graph.relationshipCount());

        long[] colIds = new long[relCount];
        long[] rowIds = new long[relCount];

        AtomicInteger index = new AtomicInteger();

        int nodeCount = Math.toIntExact(graph.nodeCount());
        Buffer resultMatrix;

        if (graph.hasRelationshipProperty()) {
            double[] values = new double[relCount];
            resultMatrix = GRBCORE.createMatrix(GRAPHBLAS.doubleType(), nodeCount, nodeCount);

            graph.forEachNode(node -> {
                graph.forEachRelationship(node, EjmlRelationships.DEFAULT_RELATIONSHIP_PROPERTY, (src, trg, weight) -> {
                    int indexValue = index.getAndIncrement();
                    colIds[indexValue] = trg;
                    rowIds[indexValue] = src;
                    values[indexValue] = weight;
                    return true;
                });
                return true;
            });

            long statusCode = GRAPHBLAS.buildMatrixFromTuplesDouble(resultMatrix, rowIds, colIds, values, relCount, GRAPHBLAS.firstBinaryOpDouble());
            assert statusCode == GRBCORE.GrB_SUCCESS : "Status code was: " + statusCode + " Did you call GrB.init()?";
        } else {
            // unweighted case -> boolean matrix entries are sufficient
            boolean[] values = new boolean[relCount];
            Arrays.fill(values, true);

            resultMatrix = GRBCORE.createMatrix(GRAPHBLAS.booleanType(), nodeCount, nodeCount);

            graph.forEachNode(node -> {
                graph.forEachRelationship(node, (src, trg) -> {
                    int indexValue = index.getAndIncrement();
                    colIds[indexValue] = trg;
                    rowIds[indexValue] = src;
                    return true;
                });
                return true;
            });

            long statusCode = GRAPHBLAS.buildMatrixFromTuplesBoolean(resultMatrix, rowIds, colIds, values, relCount, GRAPHBLAS.firstBinaryOpDouble());
            assert statusCode == GRBCORE.GrB_SUCCESS : "Status code was: " + statusCode + " Did you call GrB.init()?";
        }

        return resultMatrix;
    }
}
