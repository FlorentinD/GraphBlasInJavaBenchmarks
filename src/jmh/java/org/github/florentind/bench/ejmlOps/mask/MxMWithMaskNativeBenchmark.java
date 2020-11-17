package org.github.florentind.bench.ejmlOps.mask;

import com.github.fabianmurariu.unsafe.GRAPHBLAS;
import com.github.fabianmurariu.unsafe.GRBMONOID;
import com.github.fabianmurariu.unsafe.GRBOPSMAT;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.Buffer;

import static com.github.fabianmurariu.unsafe.GRAPHBLAS.*;
import static com.github.fabianmurariu.unsafe.GRBCORE.*;
import static com.github.fabianmurariu.unsafe.GRBMONOID.*;
import static com.github.fabianmurariu.unsafe.GRBOPSMAT.*;
import static org.github.florentind.core.grapblas_native.NativeHelper.checkStatusCode;

public class MxMWithMaskNativeBenchmark extends MxMWithMaskBaseBenchmark {

    Buffer semiring;
    Buffer nativeMatrix;
    Buffer nativeMask;
    Buffer nativeResult;
    Buffer descriptor;

    @Override
    @Setup
    public void setup() {
        super.setup();

        initNonBlocking();
        nativeMatrix = ToNativeMatrixConverter.convert(matrix);
        nativeMask = ToNativeMatrixConverter.convert(maskMatrix);
        nativeResult = createMatrix(doubleType(), matrix.numRows, matrix.numCols);
        semiring = createSemiring(plusMonoidDouble(), timesBinaryOpDouble());
        descriptor = createDescriptor();
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
