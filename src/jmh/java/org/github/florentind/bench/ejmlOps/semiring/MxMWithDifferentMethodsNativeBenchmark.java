package org.github.florentind.bench.ejmlOps.semiring;

import com.github.fabianmurariu.unsafe.GRAPHBLAS;
import com.github.fabianmurariu.unsafe.GRBCORE;
import com.github.fabianmurariu.unsafe.GRBMONOID;
import com.github.fabianmurariu.unsafe.GRBOPSMAT;
import org.apache.commons.lang3.tuple.Pair;
import org.ejml.ops.DMonoids;
import org.ejml.ops.DSemiRing;
import org.ejml.ops.DSemiRings;
import org.github.florentind.bench.ejmlOps.MatrixOpsBaseBenchmark;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.Buffer;
import java.util.HashMap;

import static com.github.fabianmurariu.unsafe.GRBCORE.GxB_NTHREADS;
import static com.github.fabianmurariu.unsafe.GRBCORE.setGlobalInt;
import static org.github.florentind.core.grapblas_native.NativeHelper.checkStatusCode;

public class MxMWithDifferentMethodsNativeBenchmark extends MatrixOpsBaseBenchmark {
    protected static final HashMap<String, Integer> methods = new HashMap<>() {{
        put(GUSTAVSON, GRBCORE.GxB_AxB_GUSTAVSON);
        put(HEAP, GRBCORE.GxB_AxB_HEAP);
        put(DOT, GRBCORE.GxB_AxB_DOT);
        put(HASH, GRBCORE.GxB_AxB_HASH);
    }};
    private static final String GUSTAVSON = "Gustavson";
    private static final String HEAP = "Heap";
    private static final String DOT = "Dot";
    private static final String HASH = "Hash";


    Buffer nativeMatrix;
    Buffer nativeResult;
    Buffer descriptor;
    Buffer semiring;

    // DOT removed as it was very slow
    @Param({GUSTAVSON, HEAP, HASH})
    protected String method;

    @Param({"500000"})
    protected int dimension;

    @Override
    @Setup
    public void setup() throws Throwable {
        super.setup();

        GRBCORE.initNonBlocking();
        setGlobalInt(GxB_NTHREADS, 1);
        nativeMatrix = ToNativeMatrixConverter.convert(matrix);
        nativeResult = GRBCORE.createMatrix(GRAPHBLAS.doubleType(), matrix.numRows, matrix.numCols);
        semiring = GRBCORE.createSemiring(GRBMONOID.plusMonoidDouble(), GRAPHBLAS.anyBinaryOpDouble());
        descriptor = GRBCORE.createDescriptor();
        GRBCORE.setDescriptorValue(descriptor, GRBCORE.GxB_AxB_METHOD, methods.get(method));
    }

    @Benchmark
    public void mxmNativeMethods(Blackhole bh) {
        checkStatusCode(GRBOPSMAT.mxm(nativeResult, null, null, semiring, nativeMatrix, nativeMatrix, descriptor));
        bh.consume(GRBCORE.matrixWait(nativeResult));
    }

    @TearDown
    public void tearDown() {
        GRBCORE.freeMatrix(nativeResult);
        GRBCORE.freeMatrix(nativeMatrix);
        GRBCORE.freeDescriptor(descriptor);
        GRBCORE.freeSemiring(semiring);
        checkStatusCode(GRBCORE.grbFinalize());
    }
}
