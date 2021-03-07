
package org.github.florentind.bench.ejmlOps.semiring;

import org.ejml.ops.DMonoids;
import org.ejml.ops.DSemiRing;
import org.ejml.ops.DSemiRings;
import org.ejml.sparse.csc.CommonOpsWithSemiRing_DSCC;
import org.ejml.sparse.csc.CommonOps_DSCC;

import java.util.HashMap;


public class MxMWithSemiringEjmlBenchmark extends MatrixOpsWithSemiringBaseBenchmark {
    protected static final HashMap<String, DSemiRing> semiRings = new HashMap<>() {{
        put(PLUS_TIMES, DSemiRings.PLUS_TIMES);
        put(OR_AND, DSemiRings.OR_AND);
        put(PLUS_AND, new DSemiRing(DMonoids.PLUS, DMonoids.AND));
        put(OR_TIMES, new DSemiRing(DMonoids.OR, DMonoids.TIMES));
        put(PLUS_FIRST, DSemiRings.PLUS_FIRST);
        put(MIN_MAX, DSemiRings.MIN_MAX);
        put(PLUS_BFIRST, new DSemiRing(DMonoids.PLUS, (x, y) -> (x == 0) ? 0 : 1));
        put(OR_PAIR, new DSemiRing(DMonoids.OR, (x, y) -> 1));
    }};

    @Override
    protected void benchmarkFunc(Integer concurrency, String semiring) {
        if (semiring.equals(NONE)) {
            CommonOps_DSCC.mult(matrix, matrix, null);
        } else {
            CommonOpsWithSemiRing_DSCC.mult(matrix, matrix, null, semiRings.get(semiring), null, null, true);
        }
    }

    public static void main(String[] args) {
        new MxMWithSemiringEjmlBenchmark().run();
    }
}
