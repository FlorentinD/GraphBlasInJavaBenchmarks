package org.github.florentind.bench.ejmlOps.mask;

import org.ejml.masks.DMasks;
import org.ejml.ops.DSemiRings;
import org.ejml.sparse.csc.CommonOpsWithSemiRing_DSCC;
import org.github.florentind.bench.ejmlOps.semiring.MxMWithSemiringNativeBenchmark;

public class MxMWithMaskEjmlBenchmark extends SimpleMatrixOpsWithMaskBaseBenchmark {
    @Override
    protected void benchmarkFunc(Integer concurrency, Boolean structural, Boolean negated) {
        CommonOpsWithSemiRing_DSCC.mult(matrix, matrix, null, DSemiRings.PLUS_TIMES, DMasks.builder(matrix, structural).withNegated(negated).build(), null, true);
    }

    public static void main(String[] args) {
        new MxMWithMaskEjmlBenchmark().run();
    }
}
