package org.github.florentind.bench.ejmlOps.mult;

import com.github.fabianmurariu.unsafe.GRAPHBLAS;
import com.github.fabianmurariu.unsafe.GRBCORE;
import com.github.fabianmurariu.unsafe.GRBMONOID;
import com.github.fabianmurariu.unsafe.GRBOPSMAT;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.Buffer;
import java.util.HashMap;

import static org.github.florentind.core.grapblas_native.NativeHelper.checkStatusCode;

public class MxMWithSemiringNativeBenchmark extends MatrixOpsWithSemiringBaseBenchmark {
    protected static final HashMap<String, Buffer> semiRings = new HashMap<>() {{
        put(PLUS_TIMES, GRBCORE.createSemiring(GRBMONOID.plusMonoidDouble(), GRAPHBLAS.timesBinaryOpDouble()));
        put(OR_AND, GRBCORE.createSemiring(GRBMONOID.lorMonoid(), GRAPHBLAS.landBinaryOp()));
        put(MIN_MAX, GRBCORE.createSemiring(GRBMONOID.minMonoidDouble(), GRAPHBLAS.maxBinaryOpDouble()));
        put(OR_TIMES, GRBCORE.createSemiring(GRBMONOID.minMonoidDouble(), GRAPHBLAS.maxBinaryOpDouble()));
        put(OR_PAIR, GRBCORE.createSemiring(GRBMONOID.lorMonoid(), GRAPHBLAS.pairBinaryOpBoolean()));
    }};

    protected Buffer nativeMatrix;
    protected Buffer nativeResult;
    protected Buffer semiring;

    @Override
    @Setup
    public void setup() {
        super.setup();

        GRBCORE.initNonBlocking();
        nativeMatrix = ToNativeMatrixConverter.convert(matrix);
        nativeResult = GRBCORE.createMatrix(GRAPHBLAS.doubleType(), matrix.numRows, matrix.numCols);
    }

    @Benchmark
    public void mxmNative(Blackhole bh) {
        if (!semiRingName.equals(NONE)) {
            semiring = semiRings.get(semiRingName);
            checkStatusCode(GRBOPSMAT.mxm(nativeResult, null, null, semiring, nativeMatrix, nativeMatrix, null));
            bh.consume(GRBCORE.matrixWait(nativeResult));
        }
    }

    @TearDown
    public void tearDown() {
        if (!semiRingName.equals(NONE)) {
            GRBCORE.freeMatrix(nativeResult);
        }
        GRBCORE.freeMatrix(nativeMatrix);
        checkStatusCode(GRBCORE.grbFinalize());
    }
}
