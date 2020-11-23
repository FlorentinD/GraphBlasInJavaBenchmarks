
package org.github.florentind.bench.ejmlOps;

import org.ejml.data.DMatrixSparseCSC;
import org.github.florentind.core.ejml.EjmlUtil;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
@Fork(value = 2, warmups = 1)
public abstract class MatrixOpsBaseBenchmark {
    protected DMatrixSparseCSC matrix;

    @Param({"100000", "500000", "1000000"})
    protected int dimension;

    // TODO also scale avgEntriesPerColumn?
    @Param({"4"})
    protected int avgEntriesPerColumn;

    @Param({"UNIFORM"})
    protected String degreeDistribution;

    @Setup
    public void setup() throws Throwable {
        matrix = EjmlUtil.createOrLoadRandomMatrix(dimension, dimension, avgEntriesPerColumn, 1, 2, 42);
    }
}
