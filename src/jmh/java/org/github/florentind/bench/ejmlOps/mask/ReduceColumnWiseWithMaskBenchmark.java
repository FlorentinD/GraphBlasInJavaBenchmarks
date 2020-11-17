package org.github.florentind.bench.ejmlOps.mask;

import org.ejml.ops.DMonoids;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

public class ReduceColumnWiseWithMaskBenchmark extends ReduceColumnWiseWithMaskBaseBenchmark {
    @Benchmark
    public void reduceColWiseWithMask(Blackhole bh) {
        bh.consume(CommonOps_DSCC.reduceColumnWise(matrix, DMonoids.TIMES.id, DMonoids.TIMES.func, null, mask, null, true));
    }
}
