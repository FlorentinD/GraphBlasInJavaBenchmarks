
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
    protected DMatrixSparseCSC otherMatrix;
    protected DMatrixSparseCSC result;

    protected DMatrixSparseCSC matrix;

    @Param({"100000"})
    private int dimension;

    // not 10^7 as out of java heap space was insufficient
    @Param({"200000"})
    private int elementCount;

    @Setup
    public void setup() {
        matrix = RandomMatrices_DSCC.rectangle(dimension, dimension, elementCount, new Random(42));
        otherMatrix = RandomMatrices_DSCC.rectangle(dimension, dimension, elementCount, new Random(9000));
        result = new DMatrixSparseCSC(matrix.numRows, matrix.numCols);
    }

    @TearDown
    public void tearDown() {
        result.zero();
    }
}
