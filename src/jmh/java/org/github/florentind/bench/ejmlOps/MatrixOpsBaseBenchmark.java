
package org.github.florentind.bench.ejmlOps;

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
public abstract class MatrixOpsBaseBenchmark {

    protected DMatrixSparseCSC matrix;

    @Param({"100000"})
    private int dimension;

    @Param({"10000000"})
    private int elementCount;

    @Setup
    public void setup() {
        matrix = RandomMatrices_DSCC.rectangle(dimension, dimension, elementCount, new Random(42));
    }
}
