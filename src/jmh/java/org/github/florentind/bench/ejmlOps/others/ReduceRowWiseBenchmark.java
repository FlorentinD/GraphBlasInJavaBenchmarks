
package org.github.florentind.bench.ejmlOps.others;

import org.ejml.data.DMatrixRMaj;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.github.florentind.bench.ejmlOps.MatrixOpsBaseBenchmark;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Uses JMH to compare the speed of reduceRowWise vs different hard-coded version.
 * .. basically does inlining work and thus vectorization still apply?
 */
public class ReduceRowWiseBenchmark extends MatrixOpsBaseBenchmark {
    DMatrixRMaj output;

    @Override
    @Setup
    public void setup() {
        super.setup();
        output = new DMatrixRMaj(matrix.numRows, 1);
    }


    @Benchmark
    public void reduceRowWiseSum(Blackhole bh) {
        bh.consume(CommonOps_DSCC.reduceRowWise(matrix, 0, Double::sum, output));
    }

    @Benchmark
    public void reduceRowWiseMax(Blackhole bh) {
        bh.consume(CommonOps_DSCC.reduceRowWise(matrix, 0, (x, y) -> (x >= y) ? x : y, output));
    }

    @Benchmark
    public void sumRows(Blackhole bh) {
        bh.consume(CommonOps_DSCC.sumRows(matrix, output));
    }

    @Benchmark
    public void maxRows(Blackhole bh) {
        bh.consume(CommonOps_DSCC.maxRows(matrix, output, null));
    }
}
