package org.github.florentind.bench.bfs;

import com.github.fabianmurariu.unsafe.GRBCORE;
import com.github.fabianmurariu.unsafe.GRBOPSMAT;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.github.florentind.graphalgos.bfs.BfsNative;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.Buffer;

public class BfsNativeBenchmark extends BfsBaseBenchmark {

    @Param({"1"})
    private int concurrency;

//    @Param({"true", "false"})
//    private boolean isCSC;

    protected Buffer jniMatrix;

    @Override
    @Setup
    public void setup() {
        super.setup();
        GRBCORE.initNonBlocking();
        //jniMatrix = ToNativeMatrixConverter.convert(graph, isCSC);
        jniMatrix = ToNativeMatrixConverter.convert(graph);
        GRBOPSMAT.transpose(jniMatrix, null, null, jniMatrix, null);
    }


    @Benchmark
    public void jniBfsLevel(Blackhole bh) {
        bh.consume(new BfsNative().computeLevel(jniMatrix, startNode, maxIterations, concurrency));
    }

    @Benchmark
    public void jniBfsParent(Blackhole bh) {
        bh.consume(new BfsNative().computeParent(jniMatrix, startNode, maxIterations, concurrency));
    }

    @Override
    @TearDown
    public void tearDown() {
        super.tearDown();
        GRBCORE.freeMatrix(jniMatrix);
        GRBCORE.grbFinalize();
    }
}
