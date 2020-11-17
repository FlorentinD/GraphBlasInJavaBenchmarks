
package org.github.florentind.bench.ejmlOps.mult;

import org.github.florentind.bench.ejmlOps.MatrixOpsBaseBenchmark;
import org.openjdk.jmh.annotations.Param;

public class MatrixOpsWithSemiringBaseBenchmark extends MatrixOpsBaseBenchmark {
    protected static final String PLUS_TIMES = "Plus, Times";
    protected static final String OR_AND = "Or, And";
    protected static final String PLUS_AND = "Plus, And";
    protected static final String OR_TIMES = "Or, Times";
    protected static final String PLUS_FIRST = "Plus, First";
    protected static final String PLUS_BFIRST = "Plus, Boolean-First";
    protected static final String OR_PAIR = "Or, Pair";
    protected static final String MIN_MAX = "Min, Max";
    protected static final String NONE = "Plus, Times (inlined)";


    @Param({NONE, PLUS_TIMES, OR_PAIR, OR_AND, MIN_MAX})
    protected String semiRingName;
}
