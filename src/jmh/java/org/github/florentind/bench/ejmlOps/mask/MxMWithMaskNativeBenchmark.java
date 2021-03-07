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

public class MxMWithMaskNativeBenchmark extends SimpleMatrixOpsWithMaskBaseBenchmark {

    Buffer semiring;
    Buffer nativeMatrix;
    Buffer nativeResult;
    Buffer descriptor;

    @Override
    public void setup(String dataset) {
        super.setup(dataset);

        initNonBlocking();
        setGlobalInt(GxB_NTHREADS, 1);
        nativeMatrix = ToNativeMatrixConverter.convert(matrix);
        nativeResult = createMatrix(doubleType(), matrix.numRows, matrix.numCols);
        semiring = createSemiring(plusMonoidDouble(), timesBinaryOpDouble());
    }

    @Override
    protected void beforeEach() {
        super.beforeEach();
        descriptor = createDescriptor();
        setDescriptorValue(descriptor, GxB_AxB_METHOD, GxB_AxB_GUSTAVSON);
    }

    @Override
    protected void afterEach() {
        super.afterEach();
        freeDescriptor(descriptor);
    }

    @TearDown
    public void tearDown() {
        freeMatrix(nativeResult);
        freeMatrix(nativeMatrix);
        freeSemiring(semiring);
        checkStatusCode(grbFinalize());
    }

    @Override
    protected void benchmarkFunc(Integer concurrency, Boolean structural, Boolean negated) {
        if (negated) setDescriptorValue(descriptor, GrB_MASK, GrB_COMP);
        if (structural) setDescriptorValue(descriptor, GrB_MASK, GrB_STRUCTURE);
        mxm(nativeResult, nativeMatrix, null, semiring, nativeMatrix, nativeMatrix, descriptor);
        matrixWait(nativeResult);
    }

    public static void main(String[] args) {
        new MxMWithMaskNativeBenchmark().run();
    }
}
