
package org.github.florentind.bench.ejmlOps.mult;

import org.ejml.sparse.csc.mult.MatrixVectorMult_DSCC;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;


public class MxVMultBenchmark extends MxVMultBaseBenchmark {

    @Benchmark
    public void mxv(Blackhole bh) {
        MatrixVectorMult_DSCC.mult(matrix, inputVector, 0, output, 0);
    }

    @Benchmark
    public void vxm(Blackhole bh) {
        MatrixVectorMult_DSCC.mult(inputVector, 0, matrix, output, 0);
    }
}
