
package org.github.florentind.bench.ejmlOps.others;

import org.ejml.sparse.csc.CommonOps_DSCC;
import org.github.florentind.bench.ejmlOps.MatrixOpsBaseBenchmark;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Uses JMH to compare the speed of scalar reduce vs different hard-coded version.
 * .. basically does inlining work and thus vectorization still apply?
 */
@SuppressWarnings("ManualMinMaxCalculation")
public class ReduceScalarBenchmark extends MatrixOpsBaseBenchmark {

    @Benchmark
    public void reduceScalarSum(Blackhole bh) {
        bh.consume(CommonOps_DSCC.reduceScalar(matrix, 0, Double::sum));
    }

    @Benchmark
    public void elementSum(Blackhole bh) {
        bh.consume(CommonOps_DSCC.elementSum(matrix));
    }

    @Benchmark
    public void reduceScalarMin(Blackhole bh) {
        bh.consume(CommonOps_DSCC.reduceScalar(matrix, Double.MIN_VALUE, (x, y) -> (x <= y) ? x : y));
    }

    @Benchmark
    public void elementMin(Blackhole bh) {
        bh.consume(CommonOps_DSCC.elementMin(matrix));
    }

    @Benchmark
    public void reduceScalarMinAbs(Blackhole bh) {
        bh.consume(CommonOps_DSCC.reduceScalar(matrix, 0, (x, y) -> (x <= Math.abs(y)) ? x : y));
    }

    @Benchmark
    public void elementMinAbs(Blackhole bh) {
        bh.consume(CommonOps_DSCC.elementMinAbs(matrix));
    }
}
