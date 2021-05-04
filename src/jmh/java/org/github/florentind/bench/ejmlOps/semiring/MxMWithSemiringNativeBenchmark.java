package org.github.florentind.bench.ejmlOps.semiring;

import com.github.fabianmurariu.unsafe.GRAPHBLAS;
import com.github.fabianmurariu.unsafe.GRBCORE;
import com.github.fabianmurariu.unsafe.GRBMONOID;
import com.github.fabianmurariu.unsafe.GRBOPSMAT;
import org.apache.commons.lang3.tuple.Pair;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.openjdk.jmh.annotations.Benchmark;

import java.nio.Buffer;
import java.util.HashMap;
import java.util.List;

import static com.github.fabianmurariu.unsafe.GRBCORE.*;
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
    Buffer descriptor;

    @Override
    protected List<String> semiRings() {
        return List.of(PLUS_TIMES, OR_PAIR, OR_AND, MIN_MAX);
    }

    @Override
    public void setup(String dataset) {
        super.setup(dataset);

        GRBCORE.initNonBlocking();
        setGlobalInt(GxB_NTHREADS, 1);
        nativeMatrix = ToNativeMatrixConverter.convert(matrix);
        nativeResult = GRBCORE.createMatrix(GRAPHBLAS.doubleType(), matrix.numRows, matrix.numCols);
        descriptor = createDescriptor();
        setDescriptorValue(descriptor, GxB_AxB_METHOD, GxB_AxB_GUSTAVSON);
    }

    private Buffer getSemiring(String semiring) {
        var monoids = semiRings.get(semiring);
        return GRBCORE.createSemiring(monoids.getLeft(), monoids.getRight());
    }

    @Benchmark
    public void benchmarkFunc(Integer concurrency, String semiring) {
        checkStatusCode(GRBOPSMAT.mxm(nativeResult, null, null, getSemiring(semiring), nativeMatrix, nativeMatrix, descriptor));
        GRBCORE.matrixWait(nativeResult);
    }

    @Override
    public void tearDown() {
        super.tearDown();
        GRBCORE.freeMatrix(nativeResult);
        GRBCORE.freeMatrix(nativeMatrix);
        GRBCORE.freeDescriptor(descriptor);
        checkStatusCode(GRBCORE.grbFinalize());
    }

    public static void main(String[] args) {
        new MxMWithSemiringNativeBenchmark().run();
    }
}
