package org.github.florentind.bench.ejmlOps.mask;

import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.masks.DMasks;
import org.ejml.masks.PrimitiveDMask;
import org.ejml.ops.DSemiRings;
import org.ejml.sparse.csc.mult.MatrixVectorMultWithSemiRing_DSCC;
import org.github.florentind.bench.ejmlOps.MatrixOpsBaseBenchmark;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;

public class MxVWithMaskBenchmark extends MatrixOpsBaseBenchmark {
    // most interesting:
    // mxv (as m in CSC)

    PrimitiveDMask mask;
    double[] denseVector;
    double[] output;

    @Override
    @Setup
    public void setup() {
        super.setup();

        denseVector = RandomMatrices_DDRM.rectangle(1, matrix.numCols, new Random(42)).data;
        output = new double[matrix.numCols];
        mask = DMasks.builder(denseVector).build();
    }

    @Benchmark
    public void mxvWithMask(Blackhole bh) {
        bh.consume(MatrixVectorMultWithSemiRing_DSCC.mult(matrix, denseVector, output, DSemiRings.AND_OR, mask, null, true));
    }

    @Benchmark
    public void mxv(Blackhole bh) {
        bh.consume(MatrixVectorMultWithSemiRing_DSCC.mult(matrix, denseVector, output, DSemiRings.AND_OR));
    }

    @Benchmark
    public void vxmWithMask(Blackhole bh) {
        bh.consume(MatrixVectorMultWithSemiRing_DSCC.mult(denseVector, matrix, output, DSemiRings.AND_OR, mask, null, true));
    }

    @Benchmark
    public void vxm(Blackhole bh) {
        bh.consume(MatrixVectorMultWithSemiRing_DSCC.mult(denseVector, matrix, output, DSemiRings.AND_OR));
    }
}
