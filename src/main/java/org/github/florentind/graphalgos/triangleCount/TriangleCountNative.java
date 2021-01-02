package org.github.florentind.graphalgos.triangleCount;

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
        Buffer C = createMatrix(longType(), nodeCount, nodeCount);

        Buffer plusPairSemiring = createSemiring(plusMonoidLong(), pairBinaryOpLong());
        checkStatusCode(mxm(C, L, null, plusPairSemiring, L, L, null));


        long globalCount = matrixReduceAllLong(0, null, plusMonoidLong(), C, null);

        freeMatrix(L);
        freeMatrix(C);
        freeSemiring(plusPairSemiring);

        return globalCount;
    }

    public static TriangleCountResult computeNodeWise(Buffer matrix, int concurrency) {
        setGlobalInt(GxB_NTHREADS, concurrency);

        Buffer desc = createDescriptor();
        checkStatusCode(setDescriptorValue(desc, GrB_OUTP, GrB_REPLACE));

        Buffer L = getLowerTriangle(matrix);
        Buffer plusPairSemiring = createSemiring(plusMonoidLong(), pairBinaryOpLong());
        checkStatusCode(mxm(L, matrix, null, plusPairSemiring, matrix, L, desc));

        long nodeCount = nrows(matrix);
        Buffer nativeResult = createVector(longType(), nodeCount);

        checkStatusCode(matrixReduceMonoid(nativeResult, null, null, plusMonoidLong(), L, null));

        int resultSize = Math.toIntExact(nvalsVector(nativeResult));
        long[] resultValues = new long[resultSize];
        long[] indices = new long[resultSize];

        vectorWait(nativeResult);
        extractVectorTuplesLong(nativeResult, resultValues, indices);

        freeSemiring(plusPairSemiring);
        freeDescriptor(desc);
        freeVector(nativeResult);
        freeMatrix(L);

        if (resultSize == nodeCount) {
            return new NativeNodeWiseTriangleCountResult(resultValues);
        } else {
            return new NativeSparseNodeWiseTriangleCountResult(indices, resultValues);
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
        // needs to be of type long, as its reused to store the triangle counts
        Buffer L = createMatrix(longType(), nodeCount, nodeCount);

        Buffer selectOp = lower ? selectOpTRIL() : selectOpTRIU();

        checkStatusCode(select(L, null, null, selectOp, matrix, null, null));

        return L;
    }
}
