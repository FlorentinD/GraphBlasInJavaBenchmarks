package org.github.florentind.graphalgos.triangleCount;

import com.github.fabianmurariu.unsafe.*;

import java.nio.Buffer;

import static com.github.fabianmurariu.unsafe.GRAPHBLAS.*;
import static com.github.fabianmurariu.unsafe.GRBALG.*;
import static com.github.fabianmurariu.unsafe.GRBCORE.*;
import static com.github.fabianmurariu.unsafe.GRBMONOID.*;
import static com.github.fabianmurariu.unsafe.GRBOPSMAT.*;
import static org.github.florentind.core.grapblas_native.NativeHelper.checkStatusCode;

public class TriangleCountNative {

    public static long computeTotalSandia(Buffer matrix, int concurrency) {
        setGlobalInt(GxB_NTHREADS, concurrency);

        Buffer L = getLowerTriangle(matrix);
        long nodeCount = nrows(matrix);
        Buffer C = createMatrix(doubleType(), nodeCount, nodeCount);

        Buffer semiRing = createSemiring(plusMonoidDouble(), firstBinaryOpDouble());
        checkStatusCode(mxm(C, L, null, semiRing, L, L, null));


        double globalCount = matrixReduceAllDouble(0.0, null, plusMonoidDouble(), C, null); // CommonOps_DSCC.reduceScalar(C, Double::sum);
        // assert count is a whole number
        assert (globalCount % 1) == 0;

        freeMatrix(L);
        freeMatrix(C);
        freeSemiring(semiRing);

        return (long) globalCount;
    }

    public static NodeWiseTriangleCountResult computeNodeWise(Buffer matrix, int concurrency) {
        setGlobalInt(GxB_NTHREADS, concurrency);

        Buffer desc = createDescriptor();
        checkStatusCode(setDescriptorValue(desc, GrB_OUTP, GrB_REPLACE));

        Buffer L = getLowerTriangle(matrix);
        Buffer plusAndSemiring = createSemiring(plusMonoidDouble(), landBinaryOpDouble());
        checkStatusCode(mxm(L, matrix, null, plusAndSemiring, matrix, L, desc));

        long nodeCount = nrows(matrix);
        Buffer nativeResult = createVector(longType(), nodeCount);

        checkStatusCode(matrixReduceMonoid(nativeResult, null, null, plusMonoidLong(), L, null));

        int resultSize = Math.toIntExact(nvalsVector(nativeResult));
        double[] resultValues = new double[resultSize];
        long[] indices = new long[resultSize];

        extractVectorTuplesDouble(nativeResult, resultValues, indices);

        freeSemiring(plusAndSemiring);
        freeDescriptor(desc);
        freeVector(nativeResult);
        freeMatrix(L);

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
        long nodeCount = nrows(matrix);
        Buffer L = createMatrix(doubleType(), nodeCount, nodeCount);

        Buffer selectOp = lower ? selectOpTRIL() : selectOpTRIU();

        checkStatusCode(select(L, null, null, selectOp, matrix, null, null));

        return L;
    }
}
