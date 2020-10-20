
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

    @Param({"10000","100000","1000000"})
    private int dimension;

    // TODO also scale avgEntriesPerColumn?
    @Param({"4"})
    private int avgEntriesPerColumn;

    @Param({"UNIFORM"})
    private String degreeDistribution;

    @Setup
    public void setup() {
        matrix = RandomMatrices_DSCC.generateUniform(dimension, dimension, avgEntriesPerColumn, 1, 2, new Random(42));
    }
}
