
package org.github.florentind.bench.ejmlOps.mult;

import org.ejml.ops.DSemiRing;
import org.ejml.ops.DSemiRings;
import org.ejml.sparse.csc.CommonOpsWithSemiRing_DSCC;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.infra.Blackhole;

import java.util.HashMap;


public class MxMWithSemiRingBenchmark extends MxMBaseBenchmark {
    private static final String PLUS_TIMES = "Plus, Times";
    private static final String OR_AND = "Or, And";
    private static final String MIN_MAX = "Min, Max";
    private static final String MIN_FIRST = "Min, FIRST";

    HashMap<String, DSemiRing> semiRings = new HashMap<>() {{
        put(PLUS_TIMES, DSemiRings.PLUS_TIMES);
        put(OR_AND, DSemiRings.OR_AND);
        put(MIN_MAX, DSemiRings.MIN_MAX);
        put(MIN_FIRST, DSemiRings.MIN_FIRST);
    }};

    @Param({PLUS_TIMES, OR_AND, MIN_MAX})
    private String semiRing;

    @Benchmark
    public void mxm(Blackhole bh) {
        CommonOpsWithSemiRing_DSCC.mult(matrix, matrix, result, semiRings.get(semiRing), null, null);
        bh.consume(result);
    }
}
