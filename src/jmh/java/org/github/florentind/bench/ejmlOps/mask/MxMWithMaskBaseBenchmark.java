package org.github.florentind.bench.ejmlOps.mask;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.github.florentind.bench.ejmlOps.mult.MxMBaseBenchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;

import java.util.Random;

public class MxMWithMaskBaseBenchmark extends MxMBaseBenchmark {
    // overriding
    @Param({"100000"})
    private int dimension;

    @Param({"5", "50"})
    protected int avgEntriesPerColumnInMask;

    @Param({"false", "true"})
    protected boolean negatedMask;

    @Param({"false", "true"})
    protected boolean structuralMask;

    DMatrixSparseCSC maskMatrix;

    @Setup
    public void setup() {
        super.setup();
        maskMatrix = RandomMatrices_DSCC.generateUniform(dimension, dimension, avgEntriesPerColumnInMask, -1, 1, new Random(42));
        if (!structuralMask) {
            // ca. 50% are true
            CommonOps_DSCC.apply(maskMatrix, (v) -> (v > 0) ? 1 : 0);
        }
    }
}
