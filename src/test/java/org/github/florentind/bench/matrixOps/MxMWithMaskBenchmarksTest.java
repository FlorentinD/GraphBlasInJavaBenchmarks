package org.github.florentind.bench.matrixOps;

import com.github.fabianmurariu.unsafe.GRAPHBLAS;
import com.github.fabianmurariu.unsafe.GRBCORE;
import com.github.fabianmurariu.unsafe.GRBMONOID;
import com.github.fabianmurariu.unsafe.GRBOPSMAT;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.masks.DMasks;
import org.ejml.ops.DSemiRings;
import org.ejml.sparse.csc.CommonOpsWithSemiRing_DSCC;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.Stream;

import static com.github.fabianmurariu.unsafe.GRBCORE.*;
import static org.github.florentind.core.grapblas_native.NativeHelper.checkStatusCode;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MxMWithMaskBenchmarksTest {
    private static final Random RAND = new Random(42);
    protected DMatrixSparseCSC matrix;

    private int dimension = 100_000;
    private int avgDegree = 4;

    private static int[] AVG_MASK_DEGREES = {5, 50};
    private static boolean[] BOOL_VALUES = {true, false};

    private String degreeDistribution = "UNIFORM";

    private static Stream<Arguments> mxmWithMaskVariants() {
        Stream.Builder<Arguments> builder = Stream.builder();

        Arrays.stream(AVG_MASK_DEGREES).forEach(v -> {
                    for (boolean maskNegation : BOOL_VALUES) {
                        for (boolean structural : BOOL_VALUES) {
                            builder.add(Arguments.of(v, maskNegation, structural));
                        }
                    }
                }
        );

        return builder.build();
    }

    @BeforeEach
    public void setup() {
        matrix = RandomMatrices_DSCC.generateUniform(dimension, dimension, avgDegree, 1, 2, RAND);
    }

    @ParameterizedTest(name = "avgDegreeInMask: {0}, negateMask: {1}, structural: {2}")
    @MethodSource("mxmWithMaskVariants")
    public void testMxMWithMask(int avgDegreeInMask, boolean negatedMask, boolean structuralMask) {
        var maskMatrix = RandomMatrices_DSCC.generateUniform(dimension, dimension, avgDegreeInMask, -1, 1, RAND);
        if (!structuralMask) {
            CommonOps_DSCC.apply(maskMatrix, (v) -> (v > 0) ? 1 : 0);
        }

        var ejmlResult = CommonOpsWithSemiRing_DSCC
                .mult(matrix, matrix, null, DSemiRings.PLUS_TIMES, DMasks.builder(maskMatrix, structuralMask).withNegated(negatedMask).build(), null, true);

        GRBCORE.initNonBlocking();

        var nativeMatrix = ToNativeMatrixConverter.convert(matrix);
        var nativeMask = ToNativeMatrixConverter.convert(maskMatrix);
        var nativeResult = createMatrix(GRAPHBLAS.doubleType(), matrix.numRows, matrix.numCols);
        var semiring = createSemiring(GRBMONOID.plusMonoidDouble(), GRAPHBLAS.timesBinaryOpDouble());
        var descriptor = createDescriptor();

        if (negatedMask) setDescriptorValue(descriptor, GrB_MASK, GrB_COMP);
        if (structuralMask) setDescriptorValue(descriptor, GrB_MASK, GrB_STRUCTURE);

        checkStatusCode(GRBOPSMAT.mxm(nativeResult, nativeMask, null, semiring, nativeMatrix, nativeMatrix, descriptor));
        matrixWait(nativeResult);

        //System.out.println("ejmlResult.nz_length = " + ejmlResult.nz_length);
        assertEquals(ejmlResult.nz_length, GRBCORE.nvalsMatrix(nativeResult));

        freeMatrix(nativeResult);
        freeMatrix(nativeMatrix);
        freeMatrix(nativeMask);
        freeDescriptor(descriptor);
        freeSemiring(semiring);
        checkStatusCode(grbFinalize());
    }
}
