package org.github.florentind.bench.ejmlOps.semiring;

import com.github.fabianmurariu.unsafe.GRAPHBLAS;
import com.github.fabianmurariu.unsafe.GRBCORE;
import com.github.fabianmurariu.unsafe.GRBMONOID;
import com.github.fabianmurariu.unsafe.GRBOPSMAT;
import org.apache.commons.lang3.tuple.Pair;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.Buffer;
import java.util.HashMap;

import static org.github.florentind.core.grapblas_native.NativeHelper.checkStatusCode;

public class MxMWithSemiringNativeBenchmark extends MatrixOpsWithSemiringBaseBenchmark {
    protected static final HashMap<String, Pair<Buffer, Buffer>> semiRings = new HashMap<>() {{
        put(PLUS_TIMES, Pair.of(GRBMONOID.plusMonoidDouble(), GRAPHBLAS.timesBinaryOpDouble()));
        put(OR_AND, Pair.of(GRBMONOID.lorMonoid(), GRAPHBLAS.landBinaryOp()));
        put(MIN_MAX, Pair.of(GRBMONOID.minMonoidDouble(), GRAPHBLAS.maxBinaryOpDouble()));
        put(OR_TIMES, Pair.of(GRBMONOID.minMonoidDouble(), GRAPHBLAS.maxBinaryOpDouble()));
        put(OR_PAIR, Pair.of(GRBMONOID.lorMonoid(), GRAPHBLAS.pairBinaryOpBoolean()));
    }};

    protected Buffer nativeMatrix;
    protected Buffer nativeResult;
    protected Buffer semiring;

    @Param({PLUS_TIMES, OR_PAIR, OR_AND, MIN_MAX})
    protected String semiRingName;

    @Override
    @Setup
    public void setup() throws Throwable {
        super.setup();

        GRBCORE.initNonBlocking();
        nativeMatrix = ToNativeMatrixConverter.convert(matrix);
        nativeResult = GRBCORE.createMatrix(GRAPHBLAS.doubleType(), matrix.numRows, matrix.numCols);
        var monoids = semiRings.get(semiRingName);
        semiring = GRBCORE.createSemiring(monoids.getLeft(), monoids.getRight());
    }

    @Benchmark
    public void mxmNative(Blackhole bh) {
        checkStatusCode(GRBOPSMAT.mxm(nativeResult, null, null, semiring, nativeMatrix, nativeMatrix, null));
        bh.consume(GRBCORE.matrixWait(nativeResult));
    }

    @TearDown
    public void tearDown() {
        GRBCORE.freeMatrix(nativeResult);
        GRBCORE.freeMatrix(nativeMatrix);
        checkStatusCode(GRBCORE.grbFinalize());
    }
}
