package org.github.florentind.graphalgos.triangleCount;

import com.github.fabianmurariu.unsafe.*;

import java.nio.Buffer;

public class TriangleCountNative {
    // TODO translate ejml version to native version
    // e.g. sandia and nodeWise with mask would be most important

    public static long computeTotalSandia(Buffer matrix, int concurrency) {
        GRBCORE.setGlobalInt(GRBCORE.GxB_NTHREADS, concurrency);

        long status;
        Buffer L = getLowerTriangle(matrix);
        long nodeCount = GRBCORE.nrows(matrix);
        Buffer C = GRBCORE.createMatrix(GRAPHBLAS.doubleType(), nodeCount, nodeCount);

        Buffer semiRing = GRBCORE.createSemiring(GRBMONOID.plusMonoidDouble(), GRAPHBLAS.landBinaryOpDouble());
        status = GRBOPSMAT.mxm(C, L, null, semiRing, L, L, null);
        assert status == GRBCORE.GrB_SUCCESS;


        double globalCount = GRBALG.matrixReduceAllDouble(0.0, null, GRBMONOID.plusMonoidDouble(), C, null); // CommonOps_DSCC.reduceScalar(C, Double::sum);
        // assert count is a whole number
        assert (globalCount % 1) == 0;

        GRBCORE.freeMatrix(L);
        GRBCORE.freeMatrix(C);
        GRBCORE.freeSemiring(semiRing);

        return (long) globalCount;
    }

    private static Buffer getLowerTriangle(Buffer matrix) {
        return getTriangle(matrix, true);
    }

    private static Buffer getUpperTriangle(Buffer matrix) {
        return getTriangle(matrix, false);
    }

    private static Buffer getTriangle(Buffer matrix, boolean lower) {
        long nodeCount = GRBCORE.nrows(matrix);
        Buffer L = GRBCORE.createMatrix(GRAPHBLAS.doubleType(), nodeCount, nodeCount);

        Buffer selectOp = lower ? GRAPHBLAS.selectOpTRIL() : GRAPHBLAS.selectOpTRIU();

        long status = GRBOPSMAT.select(L, null, null, selectOp, matrix, null, null);
        assert status == GRBCORE.GrB_SUCCESS;

        return L;
    }
}
