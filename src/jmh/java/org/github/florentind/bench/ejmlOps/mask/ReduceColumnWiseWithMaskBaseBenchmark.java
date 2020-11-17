package org.github.florentind.bench.ejmlOps.mask;

import org.ejml.data.DVectorSparse;
import org.ejml.masks.DMasks;
import org.ejml.masks.Mask;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;

import java.util.Random;

public class ReduceColumnWiseWithMaskBaseBenchmark extends MatrixOpsWithMaskBaseBenchmark {
    Mask mask;
    DVectorSparse maskVector;

    @Param({"false", "true"})
    protected boolean denseMask;

    // overriding default
    @Param({"500000"})
    protected int dimension;

    @Override
    @Setup
    public void setup() {
        super.setup();

        maskVector = new DVectorSparse(RandomMatrices_DSCC.generateUniform(dimension, 1, avgEntriesPerColumnInMask, 1, 1, new Random(42)));

        if (denseMask) {
            double[] denseVector = new double[dimension];
            maskVector.createIterator().forEachRemaining(coord -> denseVector[coord.index] = coord.value);
            mask = DMasks.builder(denseVector).withNumCols(matrix.numCols).withNegated(negatedMask).build();
        } else {
            mask = DMasks.builder(maskVector, structuralMask).withNegated(negatedMask).build();
        }
    }
}
