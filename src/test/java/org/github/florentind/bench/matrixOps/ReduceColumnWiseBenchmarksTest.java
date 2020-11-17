package org.github.florentind.bench.matrixOps;

import com.github.fabianmurariu.unsafe.GRAPHBLAS;
import com.github.fabianmurariu.unsafe.GRBCORE;
import com.github.fabianmurariu.unsafe.GRBMONOID;
import com.github.fabianmurariu.unsafe.GRBOPSMAT;
import org.apache.commons.lang3.tuple.Pair;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.ops.DMonoid;
import org.ejml.ops.DMonoids;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.Buffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.Stream;

import static com.github.fabianmurariu.unsafe.GRBCORE.*;
import static org.github.florentind.core.grapblas_native.NativeHelper.checkStatusCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReduceColumnWiseBenchmarksTest {
    private static final Random RAND = new Random(42);
    protected DMatrixSparseCSC matrix;

    protected static final HashMap<String, Pair<DMonoid, Buffer>> monoids = new HashMap<>() {{
        put("PLUS", Pair.of(DMonoids.PLUS, GRBMONOID.plusMonoidDouble()));
        put("OR", Pair.of(DMonoids.OR, GRBMONOID.lorMonoid()));
        put("MIN", Pair.of(DMonoids.MIN, GRBMONOID.minMonoidDouble()));
    }};

    // lower dimension then in benchmark for faster test
    private static int[] DIMENSIONS = {100_000};
    private int avgDegree = 4;


    private static Stream<Arguments> reduceColumnWiseVariants() {
        Stream.Builder<Arguments> builder = Stream.builder();

        Arrays.stream(DIMENSIONS).forEach(dim ->
                monoids.forEach((name, pair) -> builder.add(Arguments.of(name, dim, pair.getLeft(), pair.getRight())))
        );

        return builder.build();
    }

    @ParameterizedTest(name = "monoid: {0}, dim: {1}")
    @MethodSource("reduceColumnWiseVariants")
    public void testReduceColumnWise(String name, int dimension, DMonoid monoid, Buffer nativeMonoid) {
        matrix = RandomMatrices_DSCC.generateUniform(dimension, dimension, avgDegree, 1, 2, RAND);

        var ejmlResult = CommonOps_DSCC.reduceColumnWise(matrix, monoid.id, monoid.func, null);

        GRBCORE.initNonBlocking();

        var nativeMatrix = ToNativeMatrixConverter.convert(matrix);
        var nativeResult = createVector(GRAPHBLAS.doubleType(), matrix.numCols);
        var descriptor = createDescriptor();
        setDescriptorValue(descriptor, GrB_INP0, GrB_TRAN);

        checkStatusCode(GRBOPSMAT.matrixReduceMonoid(nativeResult, null, null, nativeMonoid, nativeMatrix, descriptor));
        vectorWait(nativeResult);

        for (int idx = 0; idx < ejmlResult.data.length; idx++) {
            double ejmlValue = ejmlResult.get(idx);
            double[] nativeEntry = GRAPHBLAS.getVectorElementDouble(nativeResult, idx);
            if (ejmlValue != monoid.id) {
                assertEquals(ejmlValue, nativeEntry[0], 1e-5);
            } else {
                assertEquals(nativeEntry.length, 0);
            }
        }

        freeMatrix(nativeResult);
        freeMatrix(nativeMatrix);
        freeDescriptor(descriptor);
        checkStatusCode(grbFinalize());
    }
}
