package org.github.florentind.graphalgos.triangleCount;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.masks.DMasks;
import org.ejml.masks.Mask;
import org.ejml.ops.*;
import org.ejml.sparse.csc.CommonOpsWithSemiRing_DSCC;
import org.ejml.sparse.csc.CommonOps_DSCC;

public class TriangleCountEjml {
    static DSemiRing plusAndSemiring = new DSemiRing(DMonoids.PLUS, DMonoids.AND);

    /**
     * based on slides 91-93 in
     * http://mit.bme.hu/~szarnyas/grb/graphblas-introduction.pdf
     *
     * @param matrix Representing an undirected graph
     * @return triangles inside the graph
     */
    public static double computeTotalCohen(DMatrixSparseCSC matrix, boolean useMask) {
        DMatrixSparseCSC L = getLowerTriangle(matrix);
        DMatrixSparseCSC U = CommonOps_DSCC.select(matrix, IBinaryPredicates.higherTriangle, null, null);

        // AND instead of TIMES as actual values do not matter
        DSemiRing semiRing = new DSemiRing(DMonoids.PLUS, DMonoids.AND);

        Mask mask = null;
        if (useMask) {
            mask = DMasks.builder(matrix, true).build();
        }

        DMatrixSparseCSC B = CommonOpsWithSemiRing_DSCC.mult(L, U, null, semiRing, mask, null);
        DMatrixSparseCSC C;
        if (!useMask) {
            // using L for output mem as L is not used afterwards
            // MAX_FIRST semiRing as we dont care for the actual values in the matrix (just take the value from B)
            C = CommonOpsWithSemiRing_DSCC.elementMult(B, matrix, L, DSemiRings.MAX_FIRST, null, null, null, null);
        } else {
            C = B;
        }

        return CommonOps_DSCC.reduceScalar(C, Double::sum) / 2;
    }

    public static double computeTotalSandia(DMatrixSparseCSC matrix) {
        DMatrixSparseCSC L = getLowerTriangle(matrix);

        Mask mask = DMasks.builder(L, true).build();
        DMatrixSparseCSC C = CommonOpsWithSemiRing_DSCC.mult(L, L, null, plusAndSemiring, mask, null);

        return CommonOps_DSCC.reduceScalar(C, Double::sum);
    }

    // Sandia variant (slides 95 - 96)

    // Simple variant


    // nodewise variant
    /**
     * based on slides 104 - 105 in
     * http://mit.bme.hu/~szarnyas/grb/graphblas-introduction.pdf
     *
     * @param matrix
     */
    public static double[] computeNodeWise(DMatrixSparseCSC matrix, boolean useLowerTriangle) {
        Mask mask = DMasks.builder(matrix, true).build();

        double[] result;

        if (useLowerTriangle) {
            // FIXME !!
            DMatrixSparseCSC L = getLowerTriangle(matrix);
            DMatrixSparseCSC tri = CommonOpsWithSemiRing_DSCC.mult(matrix, L, null, plusAndSemiring, mask, null);
            result = CommonOps_DSCC.reduceRowWise(tri, 0, Double::sum, null).data;
        }
        else {
            DMatrixSparseCSC tri = CommonOpsWithSemiRing_DSCC.mult(matrix, matrix, null, plusAndSemiring, mask, null);
            result = CommonOps_DSCC.reduceColumnWise(tri, 0, Double::sum, null).data;

            for (int i = 0; i < result.length; i++) {
                // each triangle gets counted twice .. as not only lower triangle is used
                result[i] /= 2;
            }
        }

        return result;
    }

    private static DMatrixSparseCSC getLowerTriangle(DMatrixSparseCSC matrix) {
        return CommonOps_DSCC.select(matrix, IBinaryPredicates.lowerTriangle, null, null);
    }
}
