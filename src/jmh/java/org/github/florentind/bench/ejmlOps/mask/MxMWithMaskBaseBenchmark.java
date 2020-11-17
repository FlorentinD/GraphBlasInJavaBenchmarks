package org.github.florentind.bench.ejmlOps.mask;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;

import java.util.Random;

public class MxMWithMaskBaseBenchmark extends MatrixOpsWithMaskBaseBenchmark {
    // overriding
    @Param({"100000"})
    private int dimension;

    DMatrixSparseCSC maskMatrix;

    @Setup
    public void setup() {
        super.setup();
        maskMatrix = RandomMatrices_DSCC.generateUniform(dimension, dimension, avgEntriesPerColumnInMask, 1, 1, new Random(42));
    }
}
