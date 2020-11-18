
package org.github.florentind.bench.ejmlOps.semiring;

import org.github.florentind.bench.ejmlOps.MatrixOpsBaseBenchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;

import java.util.Random;


public class MxVMultBaseBenchmark extends MatrixOpsBaseBenchmark {
    protected double[] denseInputVector;

    protected double[] output;

    @Param({"0.01", "0.8"})
    private float inputVectorDensity;

    @Override
    @Setup
    public void setup() throws Throwable {
        super.setup();
        denseInputVector = new double[matrix.numRows];
        output = new double[matrix.numRows];
        // fast init and actual values are not relevant for the benchmark

        int nonZeroElements = Math.round(inputVectorDensity * matrix.numCols);

        var rand = new Random(99);

        for (int i = 0; i < nonZeroElements; i++) {
           int index = rand.nextInt(denseInputVector.length);
           denseInputVector[index] = rand.nextDouble();
        }
    }
}
