
package org.github.florentind.bench.ejmlOps.mult;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.openjdk.jmh.annotations.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
@Fork(value = 1, warmups = 2)
public class MxMBaseBenchmark {
    protected DMatrixSparseCSC result;

    protected DMatrixSparseCSC matrix;

    @Param({"10000","100000","1000000"})
    private int dimension;

    // TODO also scale avgEntriesPerColumn?
    @Param({"4"})
    private int avgEntriesPerColumn;

    @Setup
    public void setup() {
        matrix = RandomMatrices_DSCC.generateUniform(dimension, dimension, avgEntriesPerColumn, 0, 1, new Random(42));
        result = new DMatrixSparseCSC(matrix.numRows, matrix.numCols);
    }

    @TearDown
    public void tearDown() {
        result.zero();
    }
}
