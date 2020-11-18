package org.github.florentind.core.ejml;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.ops.*;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

public class EjmlUtil {
    public static final DBinaryOperator PAIR_OP = (x, y) -> 1;
    public static final DBinaryOperator SECOND_OP = (x, y) -> y;
    public static final DMonoid FIRST_MONOID = new DMonoid(0, (a, b) -> a);
    public static final DSemiRing FIRST_PAIR = new DSemiRing(FIRST_MONOID, PAIR_OP);
    public static final DSemiRing OR_PAIR = new DSemiRing(DMonoids.OR, PAIR_OP);

    /**
     * sum of outgoing edge weights equals 1 (or 0 if no edges at all)
     * <p>
     * used by weighted pageRank (preprocessing step for pregel variant)
     */
    public static EjmlGraph normalizeOutgoingWeights(EjmlGraph graph) {
        normalizeColumnWise(graph.matrix());
        return graph;
    }

    /**
     * Matrix changes, such that the sum per column = 1
     *
     */
    public static void normalizeColumnWise(DMatrixSparseCSC matrix) {
        double[] weightSums = CommonOps_DSCC.reduceColumnWise(matrix, 0, Double::sum, null).data;

        // eWiseMult (with implicit array expansion)
        for (int col = 0; col < matrix.numCols; col++) {
            int start = matrix.col_idx[col];
            int end = matrix.col_idx[col + 1];

            double sum = weightSums[col];

            for (int i = start; i < end; i++) {
                matrix.nz_values[i] /= sum;
            }
        }
    }

    public static DMatrixSparseCSC getAdjacencyMatrix(EjmlGraph ejmlGraph) {
        return getAdjacencyMatrix(ejmlGraph, false);
    }

    /**
     * @return untransposed adjacencyMatrix
     */
    public static DMatrixSparseCSC getAdjacencyMatrix(EjmlGraph ejmlGraph, boolean sortIndices) {
        var unTransposedMatrix = CommonOps_DSCC.transpose(ejmlGraph.matrix(), null, null);
        if (sortIndices) {
            unTransposedMatrix.sortIndices(null);
        }
        return unTransposedMatrix;
    }

    public static DMatrixSparseCSC createOrLoadRandomMatrix(int numRows, int numCols, int avgEntries, double minVal, double maxVal, int seed) throws Throwable {
        String tmpMatricesDir = System.getProperty("java.io.tmpdir") + "/randomSparseMatrices";
        String matrixName = String.format("/dscc_rows%dcols%davgEntries%dminVal%smaxVal%sseed%d.bin",
                numRows, numCols, avgEntries, doubleToEscapedString(minVal), doubleToEscapedString(maxVal), seed);
        Path matrixPath = Path.of(tmpMatricesDir + matrixName);

        DMatrixSparseCSC result;

        if (new File(matrixPath.toString()).exists()) {
            result = MatrixIO.loadBin(matrixPath.toString());
        } else {
            result = RandomMatrices_DSCC.generateUniform(numRows, numCols, avgEntries, minVal, maxVal, new Random(seed));

            Files.createDirectories(Path.of(tmpMatricesDir));
            MatrixIO.saveBin(result, matrixPath.toString());
        }

        return result;
    }

    @NotNull
    private static String doubleToEscapedString(double minVal) {
        return String.valueOf(minVal).replace('.', '_');
    }
}
