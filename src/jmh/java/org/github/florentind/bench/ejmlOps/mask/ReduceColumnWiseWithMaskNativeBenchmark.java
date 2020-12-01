package org.github.florentind.bench.ejmlOps.mask;

import com.github.fabianmurariu.unsafe.GRBMONOID;
import com.github.fabianmurariu.unsafe.GRBOPSMAT;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.github.florentind.core.grapblas_native.ToNativeVectorConverter;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.Buffer;

import static com.github.fabianmurariu.unsafe.GRAPHBLAS.doubleType;
import static com.github.fabianmurariu.unsafe.GRBCORE.*;
import static org.github.florentind.core.grapblas_native.NativeHelper.checkStatusCode;

public class ReduceColumnWiseWithMaskNativeBenchmark extends ReduceColumnWiseWithMaskBaseBenchmark {

    Buffer nativeMonoid;
    Buffer nativeMatrix;
    Buffer nativeMask;
    Buffer nativeResult;
    Buffer descriptor;

    // there is no dense mask in suitesparse
    @Param({"false"})
    protected boolean denseMask;

    @Override
    @Setup
    public void setup() throws Throwable {
        super.setup();

        initNonBlocking();
        nativeMatrix = ToNativeMatrixConverter.convert(matrix);
        nativeMask = ToNativeVectorConverter.convert(maskVector);
        nativeResult = createVector(doubleType(), matrix.numRows);
        nativeMonoid = GRBMONOID.plusMonoidDouble();
        descriptor = createDescriptor();
        // otherwise would be reduceRowWise
        setDescriptorValue(descriptor, GrB_INP0, GrB_TRAN);
        if (negatedMask) setDescriptorValue(descriptor, GrB_MASK, GrB_COMP);
        if (structuralMask) setDescriptorValue(descriptor, GrB_MASK, GrB_STRUCTURE);
    }

    @Benchmark
    public void reduceColWiseWithMaskNative(Blackhole bh) {
        checkStatusCode(GRBOPSMAT.matrixReduceMonoid(nativeResult, nativeMask, null, nativeMonoid, nativeMatrix, descriptor));
        bh.consume(vectorWait(nativeResult));
    }

    @TearDown
    public void tearDown() {
        freeVector(nativeResult);
        freeMatrix(nativeMatrix);
        freeMatrix(nativeMask);
        freeDescriptor(descriptor);
        freeMonoid(nativeMonoid);
        checkStatusCode(grbFinalize());
    }
}
