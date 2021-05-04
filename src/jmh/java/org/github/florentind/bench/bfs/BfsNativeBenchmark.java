package org.github.florentind.bench.bfs;

import com.github.fabianmurariu.unsafe.GRBCORE;
import com.github.fabianmurariu.unsafe.GRBOPSMAT;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.github.florentind.graphalgos.bfs.BfsNative;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.Buffer;
import java.util.List;

public abstract class BfsNativeBenchmark extends BfsBaseBenchmark {

    @Override
    protected List<Integer> concurrencies() {
        return List.of(1, 8);
    }

//    @Param({"true", "false"})
//    private boolean isCSC;

    protected Buffer jniMatrix;

    @Override
    public void setup(String dataset) {
        super.setup(dataset);
        GRBCORE.initNonBlocking();
        //jniMatrix = ToNativeMatrixConverter.convert(graph, isCSC);
        jniMatrix = ToNativeMatrixConverter.convert(graph);
        GRBOPSMAT.transpose(jniMatrix, null, null, jniMatrix, null);
    }

    @Override
    public void tearDown() {
        super.tearDown();
        GRBCORE.freeMatrix(jniMatrix);
        GRBCORE.grbFinalize();
    }
}
