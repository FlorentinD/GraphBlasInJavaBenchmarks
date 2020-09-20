
package org.github.florentind.bench.ejmlOps.mult;

import org.github.florentind.bench.ejmlOps.MatrixOpsBaseBenchmark;
import org.openjdk.jmh.annotations.Setup;

import java.util.Arrays;


public class MxVMultBaseBenchmark extends MatrixOpsBaseBenchmark {
    protected double[] inputVector;
    protected double[] output;

    @Override
    @Setup
    public void setup() {
        super.setup();
        inputVector = new double[matrix.numRows];
        output = new double[matrix.numRows];
        // fast init and actual values are not relevant for the benchmark
        Arrays.fill(output, 22);
    }
}
