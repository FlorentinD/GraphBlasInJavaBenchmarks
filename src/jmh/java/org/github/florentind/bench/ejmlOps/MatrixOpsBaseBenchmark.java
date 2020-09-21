
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

    @Param({"500000"})
    private int dimension;

    @Param({"4"})
    private int avgDegree;

    @Setup
    public void setup() {
        matrix = RandomMatrices_DSCC.rectangle(dimension, dimension, dimension * avgDegree, new Random(42));
    }
}
