package org.github.florentind.bench.ejmlOps.mask;

import org.ejml.data.DMatrixSparseCSC;
import org.github.florentind.core.ejml.EjmlUtil;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;

public class MxMWithMaskBaseBenchmark extends MatrixOpsWithMaskBaseBenchmark {

    DMatrixSparseCSC maskMatrix;

    @Setup
    public void setup() {
        super.setup();
        maskMatrix = matrix;
    }
}
