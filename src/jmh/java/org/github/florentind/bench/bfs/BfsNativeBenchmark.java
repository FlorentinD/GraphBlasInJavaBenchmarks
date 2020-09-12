package org.github.florentind.bench.bfs;

import com.github.fabianmurariu.unsafe.GRBCORE;
import org.github.florentind.core.grapblas_native.EjmlToNativeMatrixConverter;
import org.github.florentind.graphalgos.bfs.BfsNative;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.Buffer;

public class BfsNativeBenchmark extends BfsBaseBenchmark {

    @Param({"1", "8"})
    private int concurrency;

    protected Buffer jniMatrix;

    @Setup
    public void setup() {
        super.setup();

        GRBCORE.initNonBlocking();

        jniMatrix = EjmlToNativeMatrixConverter.convert(matrix);
    }


    @Benchmark
    public void jniBfsLevel(Blackhole bh) {
        GRBCORE.setGlobalInt(GRBCORE.GxB_NTHREADS, concurrency);

        bh.consume(new BfsNative().computeLevel(jniMatrix, startNode, maxIterations));
    }

    @TearDown
    public void tearDown() {
        super.tearDown();

        GRBCORE.freeMatrix(jniMatrix);
    }
}
