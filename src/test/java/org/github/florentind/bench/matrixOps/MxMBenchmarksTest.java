package org.github.florentind.bench.matrixOps;

import com.github.fabianmurariu.unsafe.GRAPHBLAS;
import com.github.fabianmurariu.unsafe.GRBCORE;
import com.github.fabianmurariu.unsafe.GRBMONOID;
import com.github.fabianmurariu.unsafe.GRBOPSMAT;
import org.apache.commons.lang3.tuple.Pair;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.ops.DSemiRing;
import org.ejml.ops.DSemiRings;
import org.ejml.sparse.csc.CommonOpsWithSemiRing_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.github.florentind.core.ejml.EjmlUtil;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.Buffer;
import java.util.HashMap;
import java.util.Random;
import java.util.stream.Stream;

import static com.github.fabianmurariu.unsafe.GRBCORE.*;
import static org.github.florentind.core.grapblas_native.NativeHelper.checkStatusCode;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MxMBenchmarksTest {
    protected static final String PLUS_TIMES = "Plus, Times";
    protected static final String OR_AND = "Or, And";
    protected static final String OR_PAIR = "Or, Pair";
    protected static final String MIN_MAX = "Min, Max";

    // TODO: create Semirings here
    protected static final HashMap<String, Pair<DSemiRing, Pair<Buffer, Buffer>>> semirings = new HashMap<>() {{
        put(PLUS_TIMES, Pair.of(DSemiRings.PLUS_TIMES, Pair.of(GRBMONOID.plusMonoidDouble(), GRAPHBLAS.timesBinaryOpDouble())));
        put(OR_AND, Pair.of(DSemiRings.OR_AND, Pair.of(GRBMONOID.lorMonoid(), GRAPHBLAS.landBinaryOp())));
        put(MIN_MAX, Pair.of(DSemiRings.MIN_MAX, Pair.of(GRBMONOID.minMonoidDouble(), GRAPHBLAS.maxBinaryOpDouble())));
        put(OR_PAIR, Pair.of(EjmlUtil.OR_PAIR, Pair.of(GRBMONOID.lorMonoid(), GRAPHBLAS.pairBinaryOpBoolean())));
    }};

    private static final Random RAND = new Random(42);
    protected DMatrixSparseCSC matrix;

    // smaller dimension than in benchmark for faster equal check
    private int dimension = 100;
    private int avgDegree = 4;

    private static Stream<Arguments> mxmVariants() {
        Stream.Builder<Arguments> builder = Stream.builder();
        semirings.forEach((name, semiringTriple) -> builder.accept(Arguments.of(name, semiringTriple.getLeft(), semiringTriple.getRight())));
        return builder.build();
    }

    @BeforeEach
    public void setup() {
        matrix = RandomMatrices_DSCC.generateUniform(dimension, dimension, avgDegree, 1, 2, RAND);
    }

    @ParameterizedTest(name = "semiring: {0}")
    @MethodSource("mxmVariants")
    public void testMxM(String semiringDesc, DSemiRing ejmlSemiring, Pair<Buffer, Buffer> nativeSemiringArgs) {
        var ejmlResult = CommonOpsWithSemiRing_DSCC
                .mult(matrix, matrix, null, ejmlSemiring, null, null, true);

        GRBCORE.initNonBlocking();

        var nativeMatrix = ToNativeMatrixConverter.convert(matrix);
        var nativeResult = createMatrix(GRAPHBLAS.doubleType(), matrix.numRows, matrix.numCols);
        var nativeSemiring = GRBCORE.createSemiring(nativeSemiringArgs.getLeft(), nativeSemiringArgs.getRight());

        checkStatusCode(GRBOPSMAT.mxm(nativeResult, null, null, nativeSemiring, nativeMatrix, nativeMatrix, null));
        matrixWait(nativeResult);


        assertEquals(ejmlResult.nz_length, GRBCORE.nvalsMatrix(nativeResult));
        //System.out.println("ejmlResult.nz_length = " + ejmlResult.nz_length);

        ejmlResult.createCoordinateIterator()
                .forEachRemaining(coord -> assertEquals(coord.value, GRAPHBLAS.getMatrixElementDouble(nativeResult, coord.row, coord.col)[0]));

        freeMatrix(nativeResult);
        freeMatrix(nativeMatrix);
        checkStatusCode(grbFinalize());
    }
}
