package org.github.florentind.bench.ejmlOps.mask;

import org.github.florentind.bench.ejmlOps.MatrixOpsBaseBenchmark;
import org.openjdk.jmh.annotations.Param;

public class MatrixOpsWithMaskBaseBenchmark extends MatrixOpsBaseBenchmark {
    @Param({"5", "50"})
    protected int avgEntriesPerColumnInMask;

    @Param({"false", "true"})
    protected boolean negatedMask;

    @Param({"false", "true"})
    protected boolean structuralMask;
}
