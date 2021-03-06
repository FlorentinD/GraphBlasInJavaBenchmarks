
package org.github.florentind.bench.ejmlOps.others;

import org.ejml.data.DMatrixRMaj;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.github.florentind.bench.ejmlOps.MatrixOpsBaseBenchmark;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Uses JMH to compare the speed of reduceColumnWise vs different hard-coded version.
 * .. basically does inlining work and thus vectorization still apply?
 */
public class ReduceColumnWiseBenchmark extends MatrixOpsBaseBenchmark {
    DMatrixRMaj output;

    @Override
    @Setup
    public void setup() {
        super.setup();
        output = new DMatrixRMaj(matrix.numRows, 1);
    }

    @Benchmark
    public void reduceColumnWiseMaxAbs(Blackhole bh) {
        bh.consume(CommonOps_DSCC.reduceColumnWise(matrix, 0, (x, y) -> (x >= Math.abs(y)) ? x : Math.abs(y), output));
    }

    @Benchmark
    public void columnMaxAbs(Blackhole bh) {
        CommonOps_DSCC.maxAbsCols(matrix, output);
    }

    // f.i. divideColumns is not directly translate-able as it allows different values per column
}
