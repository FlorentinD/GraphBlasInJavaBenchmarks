package org.github.florentind.graphalgos.triangleCount;

import com.github.fabianmurariu.unsafe.*;

import java.nio.Buffer;

import static org.github.florentind.core.grapblas_native.NativeHelper.checkStatusCode;

public class TriangleCountNative {

    public static long computeTotalSandia(Buffer matrix, int concurrency) {
        GRBCORE.setGlobalInt(GRBCORE.GxB_NTHREADS, concurrency);

        Buffer L = getLowerTriangle(matrix);
        long nodeCount = GRBCORE.nrows(matrix);
        Buffer C = GRBCORE.createMatrix(GRAPHBLAS.doubleType(), nodeCount, nodeCount);

        Buffer semiRing = GRBCORE.createSemiring(GRBMONOID.plusMonoidDouble(), GRAPHBLAS.landBinaryOpDouble());
        checkStatusCode(GRBOPSMAT.mxm(C, L, null, semiRing, L, L, null));


        double globalCount = GRBALG.matrixReduceAllDouble(0.0, null, GRBMONOID.plusMonoidDouble(), C, null); // CommonOps_DSCC.reduceScalar(C, Double::sum);
        // assert count is a whole number
        assert (globalCount % 1) == 0;

        GRBCORE.freeMatrix(L);
        GRBCORE.freeMatrix(C);
        GRBCORE.freeSemiring(semiRing);

        return (long) globalCount;
    }

    public static NodeWiseTriangleCountResult computeNodeWise(Buffer matrix, int concurrency) {
        GRBCORE.setGlobalInt(GRBCORE.GxB_NTHREADS, concurrency);

        Buffer desc = GRBCORE.createDescriptor();
        checkStatusCode(GRBCORE.setDescriptorValue(desc, GRBCORE.GrB_OUTP, GRBCORE.GrB_REPLACE));

        Buffer L = getLowerTriangle(matrix);
        Buffer plusAndSemiring = GRBCORE.createSemiring(GRBMONOID.plusMonoidDouble(), GRAPHBLAS.landBinaryOpDouble());
        checkStatusCode(GRBOPSMAT.mxm(L, matrix, null, plusAndSemiring, matrix, L, desc));

        long nodeCount = GRBCORE.nrows(matrix);
        Buffer nativeResult = GRBCORE.createVector(GRAPHBLAS.longType(), nodeCount);

        checkStatusCode(GRBOPSMAT.matrixReduceMonoid(nativeResult, null, null, GRBMONOID.plusMonoidLong(), L, null));

        int resultSize = Math.toIntExact(GRBCORE.nvalsVector(nativeResult));
        double[] resultValues = new double[resultSize];
        long[] indices = new long[resultSize];

        GRAPHBLAS.extractVectorTuplesDouble(nativeResult, resultValues, indices);

        GRBCORE.freeSemiring(plusAndSemiring);
        GRBCORE.freeDescriptor(desc);
        GRBCORE.freeVector(nativeResult);
        GRBCORE.freeMatrix(L);

        if (resultSize == nodeCount) {
            return new NodeWiseTriangleCountResult(resultValues);
        } else {
            return new SparseNodeWiseTriangleCountResult(indices, resultValues);
        }
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

        checkStatusCode(GRBOPSMAT.select(L, null, null, selectOp, matrix, null, null));

        return L;
    }
}
