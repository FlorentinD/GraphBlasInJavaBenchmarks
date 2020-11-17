package org.github.florentind.bench.ejmlOps.mult;

import com.github.fabianmurariu.unsafe.GRBMONOID;
import com.github.fabianmurariu.unsafe.GRBOPSMAT;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.Buffer;
import java.util.HashMap;

import static com.github.fabianmurariu.unsafe.GRAPHBLAS.doubleType;
import static com.github.fabianmurariu.unsafe.GRBCORE.*;
import static org.github.florentind.core.grapblas_native.NativeHelper.checkStatusCode;

public class ReduceColumnWiseNativeBenchmark extends MatrixOpsWithMonoidBaseBenchmark {
    protected static final HashMap<String, Buffer> monoids = new HashMap<>() {{
        put(PLUS, GRBMONOID.plusMonoidDouble());
        put(OR, GRBMONOID.lorMonoid());
        put(MIN, GRBMONOID.minMonoidDouble());
    }};

    Buffer nativeMatrix;
    Buffer nativeResult;
    Buffer descriptor;

    @Override
    @Setup
    public void setup() {
        super.setup();

        initNonBlocking();
        nativeMatrix = ToNativeMatrixConverter.convert(matrix);
        nativeResult = createVector(doubleType(), matrix.numRows);
        descriptor = createDescriptor();
        // otherwise would be reduceRowWise
        setDescriptorValue(descriptor, GrB_INP0, GrB_TRAN);
    }

    @Benchmark
    public void reduceColWiseNativeWithMask(Blackhole bh) {
        checkStatusCode(GRBOPSMAT.matrixReduceMonoid(nativeResult, null, null, monoids.get(monoidName), nativeMatrix, descriptor));
        bh.consume(vectorWait(nativeResult));
    }

    @TearDown
    public void tearDown() {
        freeVector(nativeResult);
        freeMatrix(nativeMatrix);
        freeDescriptor(descriptor);
        checkStatusCode(grbFinalize());
    }
}