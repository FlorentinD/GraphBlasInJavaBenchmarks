package org.github.florentind.bench.ejmlOps.mask;

import org.ejml.masks.DMasks;
import org.ejml.ops.DSemiRings;
import org.ejml.sparse.csc.CommonOpsWithSemiRing_DSCC;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

public class MxMWithMaskBenchmark extends MxMWithMaskBaseBenchmark {
    @Benchmark
    public void mxmWithMask(Blackhole bh) {
        bh.consume(CommonOpsWithSemiRing_DSCC.mult(matrix, matrix, null, DSemiRings.PLUS_TIMES, DMasks.builder(maskMatrix, structuralMask).withNegated(negatedMask).build(), null, true));
    }
}
