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

/**
 * (mask is applied during the computation for mxv)
 * Benchmark                                      (avgDegree)  (dimension)  Mode  Cnt   Score   Error  Units
 * ejmlOps.mask.MxVWithMaskBenchmark.mxv                    4       500000  avgt    5   7.007 ± 1.011  ms/op
 * ejmlOps.mask.MxVWithMaskBenchmark.mxvWithMask            4       500000  avgt    5  19.057 ± 4.624  ms/op
 * ejmlOps.mask.MxVWithMaskBenchmark.vxm                    4       500000  avgt    5   5.415 ± 2.413  ms/op
 * ejmlOps.mask.MxVWithMaskBenchmark.vxmWithMask            4       500000  avgt    5   5.417 ± 2.075  ms/op
 *
 * (mask is applied at the end for mxv)
 * Benchmark                                      (avgDegree)  (dimension)  Mode  Cnt  Score   Error  Units
 * ejmlOps.mask.MxVWithMaskBenchmark.mxv                    4       500000  avgt    5  6.232 ± 0.247  ms/op
 * ejmlOps.mask.MxVWithMaskBenchmark.mxvWithMask            4       500000  avgt    5  6.650 ± 0.297  ms/op
 * ejmlOps.mask.MxVWithMaskBenchmark.vxm                    4       500000  avgt    5  4.738 ± 0.426  ms/op
 * ejmlOps.mask.MxVWithMaskBenchmark.vxmWithMask            4       500000  avgt    5  4.851 ± 0.399  ms/op
 */
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
