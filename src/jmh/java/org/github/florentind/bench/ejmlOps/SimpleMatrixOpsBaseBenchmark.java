
package org.github.florentind.bench.ejmlOps;

import org.ejml.data.DMatrixSparseCSC;
import org.github.florentind.bench.SimpleEjmlGraphBaseBenchmark;

import java.util.List;

public abstract class SimpleMatrixOpsBaseBenchmark extends SimpleEjmlGraphBaseBenchmark {
    protected DMatrixSparseCSC matrix;

    @Override
    protected List<String> datasets() { return List.of("Facebook", "Slashdot0902");}

    @Override
    public void setup(String dataset) {
        super.setup(dataset);
        matrix = graph.matrix();
    }
}
