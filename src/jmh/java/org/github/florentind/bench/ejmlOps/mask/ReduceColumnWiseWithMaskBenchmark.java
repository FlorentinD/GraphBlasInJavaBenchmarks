package org.github.florentind.bench.ejmlOps.mask;

import org.ejml.data.DVectorSparse;
import org.ejml.masks.DMasks;
import org.ejml.masks.Mask;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Random;

public class ReduceColumnWiseWithMaskBenchmark extends MatrixOpsWithMaskBaseBenchmark {
    Mask mask;

    @Param({"false", "true"})
    protected boolean structuralMask;

    @Param({"false", "true"})
    protected boolean denseMask;

    @Param({"1000000"})
    protected int dimension;

    @Param({"5", "50"})
    protected int maskValues;

    @Override
    @Setup
    public void setup() {
        super.setup();

        var sparseVector = new DVectorSparse(RandomMatrices_DSCC.generateUniform(dimension, 1, maskValues, 1, 1, new Random(42)));

        if (denseMask) {
            double[] denseVector = new double[dimension];
            sparseVector.createIterator().forEachRemaining(coord -> denseVector[coord.index] = coord.value);
            mask = DMasks.builder(denseVector).withNumCols(matrix.numCols).withNegated(negatedMask).build();
        } else {
            mask = DMasks.builder(sparseVector, structuralMask).withNegated(negatedMask).build();
        }
    }

    @Benchmark
    public void reduceColWiseWithMask(Blackhole bh) {
        bh.consume(CommonOps_DSCC.reduceColumnWise(matrix, 0.0, Double::sum, null, mask, null, true));
    }

}
