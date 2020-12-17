
package org.github.florentind.bench.ejmlOps.semiring;

import org.github.florentind.bench.ejmlOps.MatrixOpsBaseBenchmark;
import org.openjdk.jmh.annotations.Param;

public class MatrixOpsWithSemiringBaseBenchmark extends MatrixOpsBaseBenchmark {
    protected static final String PLUS_TIMES = "(PLUS;TIMES)";
    protected static final String OR_AND = "(OR;AND)";
    protected static final String PLUS_AND = "(PLUS;AND)";
    protected static final String OR_TIMES = "(OR;TIMES)";
    protected static final String PLUS_FIRST = "(PLUS;FIRST)";
    protected static final String PLUS_BFIRST = "(PLUS;BOOLEAN-FIRST)";
    protected static final String OR_PAIR = "(OR;Pair)";
    protected static final String MIN_MAX = "(Min;Max)";
    protected static final String NONE = "(PLUS;TIMES) (inlined)";


    @Param({NONE, PLUS_TIMES, OR_PAIR, OR_AND, MIN_MAX})
    protected String semiRingName;
}
