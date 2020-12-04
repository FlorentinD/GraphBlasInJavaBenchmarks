package org.github.florentind.bench.ejmlOps.mask;

import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.Buffer;

import static com.github.fabianmurariu.unsafe.GRAPHBLAS.doubleType;
import static com.github.fabianmurariu.unsafe.GRAPHBLAS.timesBinaryOpDouble;
import static com.github.fabianmurariu.unsafe.GRBCORE.*;
import static com.github.fabianmurariu.unsafe.GRBMONOID.plusMonoidDouble;
import static com.github.fabianmurariu.unsafe.GRBOPSMAT.mxm;
import static org.github.florentind.core.grapblas_native.NativeHelper.checkStatusCode;

public class MxMWithMaskNativeBenchmark extends MxMWithMaskBaseBenchmark {

    Buffer semiring;
    Buffer nativeMatrix;
    Buffer nativeMask;
    Buffer nativeResult;
    Buffer descriptor;

    @Override
    @Setup
    public void setup() throws Throwable {
        super.setup();

        initNonBlocking();
        setGlobalInt(GxB_NTHREADS, 1);
        nativeMatrix = ToNativeMatrixConverter.convert(matrix);
        nativeMask = ToNativeMatrixConverter.convert(maskMatrix);
        nativeResult = createMatrix(doubleType(), matrix.numRows, matrix.numCols);
        semiring = createSemiring(plusMonoidDouble(), timesBinaryOpDouble());
        descriptor = createDescriptor();
        setDescriptorValue(descriptor, GxB_AxB_METHOD, GxB_AxB_GUSTAVSON);
        if (negatedMask) setDescriptorValue(descriptor, GrB_MASK, GrB_COMP);
        if (structuralMask) setDescriptorValue(descriptor, GrB_MASK, GrB_STRUCTURE);
    }

    @Benchmark
    public void mxmNativeWithMask(Blackhole bh) {
        checkStatusCode(mxm(nativeResult, nativeMask, null, semiring, nativeMatrix, nativeMatrix, descriptor));
        bh.consume(matrixWait(nativeResult));
    }

    @TearDown
    public void tearDown() {
        freeMatrix(nativeResult);
        freeMatrix(nativeMatrix);
        freeMatrix(nativeMask);
        freeDescriptor(descriptor);
        freeSemiring(semiring);
        checkStatusCode(grbFinalize());
    }
}
