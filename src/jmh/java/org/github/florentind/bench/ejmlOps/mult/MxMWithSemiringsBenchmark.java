
package org.github.florentind.bench.ejmlOps.mult;

import org.ejml.ops.DMonoids;
import org.ejml.ops.DSemiRing;
import org.ejml.ops.DSemiRings;
import org.ejml.sparse.csc.CommonOpsWithSemiRing_DSCC;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

import java.util.HashMap;


public class MxMWithSemiringsBenchmark extends MatrixOpsWithSemiringBaseBenchmark {
    protected static final HashMap<String, DSemiRing> semiRings = new HashMap<>() {{
        put(PLUS_TIMES, DSemiRings.PLUS_TIMES);
        put(OR_AND, DSemiRings.OR_AND);
        put(PLUS_AND, new DSemiRing(DMonoids.PLUS, DMonoids.AND));
        put(OR_TIMES, new DSemiRing(DMonoids.OR, DMonoids.TIMES));
        put(PLUS_FIRST, DSemiRings.PLUS_FIRST);
        put(MIN_MAX, DSemiRings.MIN_MAX);
        put(PLUS_BFIRST, new DSemiRing(DMonoids.PLUS, (x,y) -> (x == 0) ? 0 : 1 ));
        put(OR_PAIR, new DSemiRing(DMonoids.OR, (x, y) -> 1));
    }};

    @Benchmark
    public void mxm(Blackhole bh) {
        if (semiRingName.equals(NONE)) {
            bh.consume(CommonOps_DSCC.mult(matrix, matrix, null));
        } else {
            bh.consume(CommonOpsWithSemiRing_DSCC.mult(matrix, matrix, null, semiRings.get(semiRingName), null, null, true));
        }
    }
}
