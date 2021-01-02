
package org.github.florentind.bench.ejmlOps.semiring;

import org.github.florentind.bench.ejmlOps.MatrixOpsBaseBenchmark;
import org.openjdk.jmh.annotations.Param;

public class MatrixOpsWithMonoidBaseBenchmark extends MatrixOpsBaseBenchmark {
    protected static final String PLUS = "PLUS";
    protected static final String TIMES = "TIMES";
    protected static final String OR = "OR";
    protected static final String MIN = "MIN";
    protected static final String AND = "AND";

    @Param({"500000", "1000000", "1500000"})
    protected int dimension;

    @Param({PLUS, OR, MIN})
    protected String monoidName;
}
