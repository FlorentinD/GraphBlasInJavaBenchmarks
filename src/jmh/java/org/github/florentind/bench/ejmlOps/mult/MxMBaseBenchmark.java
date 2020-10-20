
package org.github.florentind.bench.ejmlOps.mult;

import org.ejml.data.DMatrixSparseCSC;
import org.github.florentind.bench.ejmlOps.MatrixOpsBaseBenchmark;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
@Fork(value = 1, warmups = 2)
public class MxMBaseBenchmark  extends MatrixOpsBaseBenchmark {
    protected DMatrixSparseCSC result;

    @Override
    @Setup
    public void setup() {
        super.setup();
        result = new DMatrixSparseCSC(matrix.numRows, matrix.numCols);
    }
}
