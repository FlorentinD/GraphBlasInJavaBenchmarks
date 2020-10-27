package org.github.florentind.bench.ejmlOps.mask;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.masks.DMasks;
import org.ejml.masks.Mask;
import org.ejml.ops.DSemiRings;
import org.ejml.sparse.csc.CommonOpsWithSemiRing_DSCC;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.github.florentind.bench.ejmlOps.MatrixOpsBaseBenchmark;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

public class MxMWithMaskBenchmark extends MatrixOpsBaseBenchmark {
    // most interesting:
    // (no action taken from there, just an observation as we need to apply the mask at calculation time for the right result structure)
    // TODO potential action: just remove unnecessary elements at the end instead of applying the mask during computation

    Mask mask;

    @Setup
    public void setup() {
        super.setup();
        mask = DMasks.builder(matrix, true).build();
    }

    @Benchmark
    public void mxmWithMask(Blackhole bh) {
        bh.consume(CommonOpsWithSemiRing_DSCC.mult(matrix, matrix, null, DSemiRings.AND_OR, mask, null, true));
    }

    @Benchmark
    public void mxm(Blackhole bh) {
        bh.consume(CommonOpsWithSemiRing_DSCC.mult(matrix, matrix, null, DSemiRings.AND_OR, null, null, true));
    }
}
