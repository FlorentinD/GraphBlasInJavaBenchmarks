package org.github.florentind.bench.ejmlOps.mask;

import org.ejml.masks.DMasks;
import org.ejml.masks.Mask;
import org.ejml.ops.DSemiRings;
import org.ejml.sparse.csc.CommonOpsWithSemiRing_DSCC;
import org.github.florentind.bench.ejmlOps.mult.MxMBaseBenchmark;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

public class MxMWithMaskBenchmark extends MxMBaseBenchmark {
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
}
