package org.github.florentind.core.ejml;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;

public class EjmlUtil {

    /**
     * sum of outgoing edge weights equals 1 (or 0 if no edges at all)
     *
     * used by weighted pageRank (preprocessing step for pregel variant)
     */
    public static EjmlGraph normalizeOutgoingWeights(EjmlGraph graph) {
        DMatrixSparseCSC adjMatrixTransposed = graph.matrix();
        double[] weightSums = CommonOps_DSCC.reduceColumnWise(adjMatrixTransposed, 0, Double::sum, null).data;

        // extended apply .. using (using column + value per entry)
        for (int col = 0; col < adjMatrixTransposed.numCols; col++) {
            int start = adjMatrixTransposed.col_idx[col];
            int end = adjMatrixTransposed.col_idx[col + 1];

            double sum = weightSums[col];

            for (int i = start; i < end; i++) {
                adjMatrixTransposed.nz_values[i] /= sum;
            }
        }

        return graph;
    }
}
