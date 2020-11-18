
package org.github.florentind.bench.ejmlOps.semiring;

import org.ejml.ops.DMonoid;
import org.ejml.ops.DMonoids;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

import java.util.HashMap;


public class ReduceColumnWiseBenchmark extends MatrixOpsWithMonoidBaseBenchmark {
    protected static final HashMap<String, DMonoid> monoids = new HashMap<>() {{
        put(PLUS, DMonoids.PLUS);
        put(OR, DMonoids.OR);
        put(MIN, DMonoids.MIN);
    }};

    @Benchmark
    public void reduceColumnWise(Blackhole bh) {
        var monoid = monoids.get(monoidName);
        bh.consume(CommonOps_DSCC.reduceColumnWise(matrix, monoid.id, monoid.func, null));
    }
}
