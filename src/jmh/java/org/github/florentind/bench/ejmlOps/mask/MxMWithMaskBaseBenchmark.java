package org.github.florentind.bench.ejmlOps.mask;

import org.ejml.data.DMatrixSparseCSC;
import org.github.florentind.core.ejml.EjmlUtil;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;

public class MxMWithMaskBaseBenchmark extends MatrixOpsWithMaskBaseBenchmark {
    // overriding
    @Param({"100000"})
    private int dimension;

    DMatrixSparseCSC maskMatrix;

    @Setup
    public void setup() throws Throwable {
        super.setup();
        maskMatrix = EjmlUtil.createOrLoadRandomMatrix(dimension, dimension, avgEntriesPerColumnInMask, 1, 1, 42);
    }
}
