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
    DMatrixSparseCSC transposedMatrix;

    @Setup
    public void setup() {
        super.setup();
        mask = DMasks.builder(matrix, true).build();
        transposedMatrix = CommonOps_DSCC.transpose(matrix, null, null);
    }

    @Benchmark
    public void mxmWithMask(Blackhole bh) {
        bh.consume(CommonOpsWithSemiRing_DSCC.mult(matrix, matrix, null, DSemiRings.AND_OR, mask, null));
    }

    @Benchmark
    public void mxm(Blackhole bh) {
        bh.consume(CommonOpsWithSemiRing_DSCC.mult(matrix, matrix, null, DSemiRings.AND_OR, null, null));
    }

    // these are really really slow (as expected)
//    @Benchmark
//    public void mxmTWithMask(Blackhole bh) {
//        bh.consume(CommonOpsWithSemiRing_DSCC.multTransB(matrix, transposedMatrix, null, DSemiRings.AND_OR, mask, null, null, null));
//    }
//
//    @Benchmark
//    public void mxmT(Blackhole bh) {
//        bh.consume(CommonOpsWithSemiRing_DSCC.multTransB(matrix, transposedMatrix, null, DSemiRings.AND_OR, null, null, null, null));
//    }

    @Benchmark
    public void mTxmWithMask(Blackhole bh) {
        bh.consume(CommonOpsWithSemiRing_DSCC.multTransA(transposedMatrix, matrix, null, DSemiRings.AND_OR, mask, null, null, null));
    }

    @Benchmark
    public void mTxm(Blackhole bh) {
        bh.consume(CommonOpsWithSemiRing_DSCC.multTransA(transposedMatrix, matrix, null, DSemiRings.AND_OR, null, null, null, null));
    }
}
