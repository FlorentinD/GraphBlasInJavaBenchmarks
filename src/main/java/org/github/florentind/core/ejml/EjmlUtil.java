package org.github.florentind.core.ejml;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.ops.DBinaryOperator;
import org.ejml.ops.DMonoid;
import org.ejml.ops.DSemiRing;
import org.ejml.sparse.csc.CommonOps_DSCC;

public class EjmlUtil {
    public static final DBinaryOperator PAIR_OP = (x, y) -> 1;
    public static final DMonoid FIRST_MONOID = new DMonoid(0, (a, b) -> a);
    public static final DSemiRing FIRST_PAIR = new DSemiRing(FIRST_MONOID, PAIR_OP);

    /**
     * sum of outgoing edge weights equals 1 (or 0 if no edges at all)
     *
     * used by weighted pageRank (preprocessing step for pregel variant)
     */
    public static EjmlGraph normalizeOutgoingWeights(EjmlGraph graph) {
        normalizeColumnWise(graph.matrix());
        return graph;
    }

    /**
     * Matrix changes, such that the sum per column = 1
     * @param matrix
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
}
