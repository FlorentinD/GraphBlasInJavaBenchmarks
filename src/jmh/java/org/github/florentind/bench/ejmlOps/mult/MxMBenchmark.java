
package org.github.florentind.bench.ejmlOps.mult;

import org.ejml.ops.DMonoid;
import org.ejml.ops.DMonoids;
import org.ejml.ops.DSemiRing;
import org.ejml.ops.DSemiRings;
import org.ejml.sparse.csc.CommonOpsWithSemiRing_DSCC;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.infra.Blackhole;

import java.util.HashMap;


public class MxMBenchmark extends MxMBaseBenchmark {
    private static final String PLUS_TIMES = "Plus, Times";
    private static final String OR_AND = "Or, And";
    private static final String PLUS_AND = "Plus, And";
    private static final String OR_TIMES = "Or, Times";
    private static final String PLUS_FIRST = "Plus, First";
    private static final String PLUS_BFIRST = "Plus, Boolean-First";
    private static final String PLUS_ONE = "Plus, One (const)";
    private static final String MIN_MAX = "Min, Max";
    private static final String NONE = "Plus, Times (inlined)";


    HashMap<String, DSemiRing> semiRings = new HashMap<>() {{
        put(PLUS_TIMES, DSemiRings.PLUS_TIMES);
        put(OR_AND, DSemiRings.OR_AND);
        put(PLUS_AND, new DSemiRing(DMonoids.PLUS, DMonoids.AND));
        put(OR_TIMES, new DSemiRing(DMonoids.OR, DMonoids.TIMES));
        put(PLUS_FIRST, DSemiRings.PLUS_FIRST);
        put(MIN_MAX, DSemiRings.MIN_MAX);
        put(PLUS_BFIRST, new DSemiRing(DMonoids.PLUS, new DMonoid(0, (x,y) -> (x == 0) ? 0 : 1 )));
        put(PLUS_ONE, new DSemiRing(DMonoids.PLUS, new DMonoid(0, (x,y) -> 1)));
    }};

    @Param({NONE, PLUS_ONE})
    private String semiRing;

    @Benchmark
    public void mxm(Blackhole bh) {
        if (semiRing.equals(NONE)) {
            CommonOps_DSCC.mult(matrix, matrix, result);
        } else {
            CommonOpsWithSemiRing_DSCC.mult(matrix, matrix, result, semiRings.get(semiRing), null, null);
        }
        bh.consume(result);
    }
}
