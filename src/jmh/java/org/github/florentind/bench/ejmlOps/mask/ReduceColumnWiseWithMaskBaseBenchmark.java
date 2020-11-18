package org.github.florentind.bench.ejmlOps.mask;

import org.ejml.data.DVectorSparse;
import org.ejml.masks.DMasks;
import org.ejml.masks.Mask;
import org.github.florentind.core.ejml.EjmlUtil;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;

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
    public void setup() throws Throwable {
        super.setup();

        maskVector = new DVectorSparse(EjmlUtil.createOrLoadRandomMatrix(dimension, 1, avgEntriesPerColumnInMask, 1, 1, 42), false);

        if (denseMask) {
            double[] denseVector = new double[dimension];
            maskVector.createIterator().forEachRemaining(coord -> denseVector[coord.index] = coord.value);
            mask = DMasks.builder(denseVector).withNumCols(matrix.numCols).withNegated(negatedMask).build();
        } else {
            mask = DMasks.builder(maskVector, structuralMask).withNegated(negatedMask).build();
        }
    }
}
