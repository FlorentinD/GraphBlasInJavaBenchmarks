
package org.github.florentind.bench.ejmlOps.mult;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.masks.DMasks;
import org.ejml.ops.DSemiRings;
import org.ejml.sparse.csc.CommonOpsWithSemiRing_DSCC;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Uses JMH to compare the speed of mxm vs different hard-coded version.
 * .. basically does inlining work and thus vectorization still apply?
 *
 */
public class MxMBenchmark extends MxMBaseBenchmark {

    DMatrixSparseCSC transposedMatrix;

    @Setup
    public void setup() {
        super.setup();
        transposedMatrix = CommonOps_DSCC.transpose(matrix, null, null);
    }

    @Benchmark
    public void mxm(Blackhole bh) {
        CommonOps_DSCC.mult(matrix, matrix, result);
        bh.consume(result);
    }

    @Benchmark
    public void mTxm(Blackhole bh) {
        bh.consume(CommonOps_DSCC.multTransA(transposedMatrix, matrix, null, null, null));
    }
}
