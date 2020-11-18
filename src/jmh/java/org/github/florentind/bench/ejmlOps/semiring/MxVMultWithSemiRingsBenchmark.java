
package org.github.florentind.bench.ejmlOps.semiring;

import org.ejml.ops.DSemiRing;
import org.ejml.ops.DSemiRings;
import org.ejml.sparse.csc.mult.MatrixVectorMultWithSemiRing_DSCC;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;

import java.util.HashMap;


public class MxVMultWithSemiRingsBenchmark extends MxVMultBaseBenchmark {
    private static final String PLUS_TIMES = "Plus, Times";
    private static final String OR_AND = "Or, And";
    private static final String MIN_MAX = "Min, Max";
    private static final String MIN_FIRST = "Min, First";

    HashMap<String, DSemiRing> semiRings = new HashMap<>() {{
        put(PLUS_TIMES, DSemiRings.PLUS_TIMES);
        put(OR_AND, DSemiRings.OR_AND);
        put(MIN_MAX, DSemiRings.MIN_MAX);
        put(MIN_FIRST, DSemiRings.MIN_FIRST);
    }};

    @Param({PLUS_TIMES, OR_AND, MIN_MAX, MIN_FIRST})
    private String semiRing;

    @Benchmark
    public void mxv() {
        MatrixVectorMultWithSemiRing_DSCC.mult(matrix, denseInputVector, output, semiRings.get(semiRing), null, null, true);
    }

    @Benchmark
    public void vxm() {
        MatrixVectorMultWithSemiRing_DSCC.mult(denseInputVector, matrix, output, semiRings.get(semiRing));
    }
}
