
package org.github.florentind.bench.ejmlOps;

import org.ejml.data.DMatrixSparseCSC;
import org.github.florentind.bench.BaseBenchmark;
import org.github.florentind.bench.EjmlGraphBaseBenchmark;
import org.github.florentind.core.ejml.EjmlUtil;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;

public abstract class MatrixOpsBaseBenchmark extends EjmlGraphBaseBenchmark {
    protected DMatrixSparseCSC matrix;

    @Override
    @Setup
    public void setup() {
        super.setup();
        matrix = graph.matrix();
    }
}
