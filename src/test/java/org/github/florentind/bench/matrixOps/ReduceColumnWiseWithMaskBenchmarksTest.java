package org.github.florentind.bench.matrixOps;

import com.github.fabianmurariu.unsafe.*;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.DVectorSparse;
import org.ejml.masks.DMasks;
import org.ejml.masks.Mask;
import org.ejml.ops.DMonoid;
import org.ejml.ops.DMonoids;
import org.ejml.ops.DSemiRings;
import org.ejml.sparse.csc.CommonOpsWithSemiRing_DSCC;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.github.florentind.core.grapblas_native.ToNativeVectorConverter;
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

public class ReduceColumnWiseWithMaskBenchmarksTest {
    private static final Random RAND = new Random(42);
    protected DMatrixSparseCSC matrix;

    private int dimension = 100_000;
    private int avgDegree = 4;

    private static int[] NON_ZERO_MASK_VALUES = {5, 50};
    private static boolean[] BOOL_VALUES = {true, false};

    private static Stream<Arguments> mxmWithMaskVariants() {
        Stream.Builder<Arguments> builder = Stream.builder();

        Arrays.stream(NON_ZERO_MASK_VALUES).forEach(v -> {
                    for (boolean maskNegation : BOOL_VALUES) {
                        for (boolean structural : BOOL_VALUES) {
                            for (boolean denseMask : BOOL_VALUES) {
                                builder.add(Arguments.of(v, maskNegation, structural, denseMask));
                            }
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

    @ParameterizedTest(name = "avgDegreeInMask: {0}, negateMask: {1}, structural: {2}, dense: {3}")
    @MethodSource("mxmWithMaskVariants")
    public void testMxMWithMask(int nzMaskValues, boolean negatedMask, boolean structuralMask, boolean denseMask) {
        var sparseVector = new DVectorSparse(RandomMatrices_DSCC.generateUniform(dimension, 1, nzMaskValues, 1, 1, new Random(42)));

        Mask mask;
        if (denseMask) {
            double[] denseVector = new double[dimension];
            sparseVector.createIterator().forEachRemaining(coord -> denseVector[coord.index] = coord.value);
            mask = DMasks.builder(denseVector).withNumCols(matrix.numCols).withNegated(negatedMask).build();
        } else {
            mask = DMasks.builder(sparseVector, structuralMask).withNegated(negatedMask).build();
        }

        var ejmlResult = CommonOps_DSCC.reduceColumnWise(matrix, 0, DMonoids.PLUS.func, null, mask, null, true);

        if(denseMask) {
            // only sparse matrices in Native
            return;
        }

        GRBCORE.initNonBlocking();

        var nativeMatrix = ToNativeMatrixConverter.convert(matrix);
        var nativeMask = ToNativeVectorConverter.convert(sparseVector);
        var nativeResult = createVector(GRAPHBLAS.doubleType(), matrix.numCols);
        var semiring = createSemiring(GRBMONOID.plusMonoidDouble(), GRAPHBLAS.timesBinaryOpDouble());
        var descriptor = createDescriptor();

        if (negatedMask) setDescriptorValue(descriptor, GrB_MASK, GrB_COMP);
        if (structuralMask) setDescriptorValue(descriptor, GrB_MASK, GrB_STRUCTURE);

        // TODO: map Vecotr reduce in GRBOPSVEC
//        checkStatusCode(GRBOPSVEC.(nativeResult, nativeMask, null, semiring, nativeMatrix, nativeMatrix, descriptor));
//        matrixWait(nativeResult);

        //System.out.println("ejmlResult.nz_length = " + ejmlResult.nz_length);
        //assertEquals(ejmlResult.nz_length, GRBCORE.nvalsMatrix(nativeResult));

        freeMatrix(nativeResult);
        freeMatrix(nativeMatrix);
        freeMatrix(nativeMask);
        freeDescriptor(descriptor);
        freeSemiring(semiring);
        checkStatusCode(grbFinalize());
    }
}
