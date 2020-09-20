
package org.github.florentind.bench.ejmlOps.mult;

import org.ejml.sparse.csc.CommonOps_DSCC;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Uses JMH to compare the speed of reduceColumnWise vs different hard-coded version.
 * .. basically does inlining work and thus vectorization still apply?
 *
 * @author Florentin Doerre
 */
public class MxMBenchmark extends MxMBaseBenchmark {

    @Benchmark
    public void mxm(Blackhole bh) {
        CommonOps_DSCC.mult(matrix, otherMatrix, result);
        bh.consume(result);
    }
}
