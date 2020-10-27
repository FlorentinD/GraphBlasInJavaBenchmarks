package org.github.florentind.bench.ejmlOps.mask;

import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.masks.DMasks;
import org.ejml.masks.PrimitiveDMask;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.github.florentind.bench.ejmlOps.MatrixOpsBaseBenchmark;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;

public class ReduceWithMaskBenchmark extends MatrixOpsBaseBenchmark {
    PrimitiveDMask reduceRowWiseMask;
    PrimitiveDMask reduceColWiseMask;

    @Override
    @Setup
    public void setup() {
        super.setup();

        double[] denseVector = RandomMatrices_DDRM.rectangle(1, matrix.numCols, new Random(42)).data;
        reduceRowWiseMask = DMasks.builder(denseVector).build();
        reduceColWiseMask = DMasks.builder(denseVector).withNumCols(matrix.numCols).build();
    }


    @Benchmark
    public void reduceRowWise(Blackhole bh) {
        bh.consume(CommonOps_DSCC.reduceRowWise(matrix, 0.0, Double::sum, null));
    }

    @Benchmark
    public void reduceRowWiseWithMask(Blackhole bh) {
        bh.consume(CommonOps_DSCC.reduceRowWise(matrix, 0.0, Double::sum, null , reduceRowWiseMask, null, true));
    }

    @Benchmark
    public void reduceColWise(Blackhole bh) {
        bh.consume(CommonOps_DSCC.reduceColumnWise(matrix, 0.0, Double::sum, null));
    }

    @Benchmark
    public void reduceColWiseWithMask(Blackhole bh) {
        bh.consume(CommonOps_DSCC.reduceColumnWise(matrix, 0.0, Double::sum, null , reduceColWiseMask, null, true));
    }

}
