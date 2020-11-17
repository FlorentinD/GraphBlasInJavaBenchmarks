package org.github.florentind.bench.matrixOps;

import com.github.fabianmurariu.unsafe.*;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.DVectorSparse;
import org.ejml.masks.DMasks;
import org.ejml.masks.Mask;
import org.ejml.ops.DMonoid;
import org.ejml.ops.DMonoids;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReduceColumnWiseWithMaskBenchmarksTest {
    private static final Random RAND = new Random(42);
    protected DMatrixSparseCSC matrix;

    private int dimension = 100_000;
    private int avgDegree = 4;

    private static int[] NON_ZERO_MASK_VALUES = {5, 50};
    private static boolean[] BOOL_VALUES = {true, false};

    private static Stream<Arguments> reduceColumnWiseWithMaskVariants() {
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
    @MethodSource("reduceColumnWiseWithMaskVariants")
    public void testReduceColumnWiseWithMask(int nzMaskValues, boolean negatedMask, boolean structuralMask, boolean denseMask) {
        var sparseVector = new DVectorSparse(RandomMatrices_DSCC.generateUniform(dimension, 1, nzMaskValues, 1, 1, new Random(42)));

        Mask mask;
        if (denseMask) {
            double[] denseVector = new double[dimension];
            sparseVector.createIterator().forEachRemaining(coord -> denseVector[coord.index] = coord.value);
            mask = DMasks.builder(denseVector).withNumCols(matrix.numCols).withNegated(negatedMask).build();
        } else {
            mask = DMasks.builder(sparseVector, structuralMask).withNegated(negatedMask).build();
        }

        DMonoid monoid = DMonoids.TIMES;
        var ejmlResult = CommonOps_DSCC.reduceColumnWise(matrix, monoid.id, monoid.func, null, mask, null, true);

        if(denseMask) {
            // only sparse matrices in Native
            return;
        }

        GRBCORE.initNonBlocking();

        var nativeMatrix = ToNativeMatrixConverter.convert(matrix);
        var nativeMask = ToNativeVectorConverter.convert(sparseVector);
        var nativeResult = createVector(GRAPHBLAS.doubleType(), matrix.numCols);

        var nativeMonoid = GRBMONOID.timesMonoidDouble();
        var descriptor = createDescriptor();
        setDescriptorValue(descriptor, GrB_INP0, GrB_TRAN);
        if (negatedMask) setDescriptorValue(descriptor, GrB_MASK, GrB_COMP);
        if (structuralMask) setDescriptorValue(descriptor, GrB_MASK, GrB_STRUCTURE);

        checkStatusCode(GRBOPSMAT.matrixReduceMonoid(nativeResult, nativeMask, null, nativeMonoid, nativeMatrix, descriptor));
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
        freeMatrix(nativeMask);
        freeDescriptor(descriptor);
        freeSemiring(nativeMonoid);
        checkStatusCode(grbFinalize());
    }
}
