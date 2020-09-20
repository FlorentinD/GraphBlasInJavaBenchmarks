
package org.github.florentind.bench.ejmlOps;

import org.ejml.sparse.csc.CommonOps_DSCC;
import org.openjdk.jmh.annotations.*;

/**
 * Uses JMH to compare the speed of apply vs different hard-coded version.
 * .. basically does inlining work and thus vectorization still apply?
 */
public class ApplyBenchmark extends MatrixOpsBaseBenchmark {

//    @Benchmark
//    public void applyAdd() {
//        CommonOps_DSCC.apply(matrix, (x) -> x + 10);
//    }

    @Benchmark
    public void applyScale() {
        CommonOps_DSCC.apply(matrix, (x) -> x * 10);
    }

    @Benchmark
    public void applyDivide() {
        CommonOps_DSCC.apply(matrix, (x) -> 10 / x);
    }

//    @Benchmark
//    public void applyAddAndScale() {
//        CommonOps_DSCC.apply(matrix, (x) -> 10 / x + 12);
//    }

    @Benchmark
    public void scale() {
        CommonOps_DSCC.scale(10, matrix, matrix);
    }

    @Benchmark
    public void divide() {
        CommonOps_DSCC.divide(10, matrix, matrix);
    }
}
