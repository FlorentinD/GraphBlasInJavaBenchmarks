
package org.github.florentind.bench.ejmlOps.mult;

import org.github.florentind.bench.ejmlOps.MatrixOpsBaseBenchmark;
import org.openjdk.jmh.annotations.Param;

public class MatrixOpsWithMonoidBaseBenchmark extends MatrixOpsBaseBenchmark {
    protected static final String PLUS = "Plus";
    protected static final String OR = "Or";
    protected static final String FIRST = "First";
    protected static final String MIN = "Min";

    @Param({"500000", "1000000", "5000000"})
    protected int dimension;

    @Param({PLUS, OR, MIN, FIRST})
    protected String monoidName;
}
