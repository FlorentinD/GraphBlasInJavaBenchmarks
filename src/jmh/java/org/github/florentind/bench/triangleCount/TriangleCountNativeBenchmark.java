package org.github.florentind.bench.triangleCount;

import com.github.fabianmurariu.unsafe.GRBCORE;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.github.florentind.graphalgos.triangleCount.TriangleCountNative;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.Buffer;

public class TriangleCountNativeBenchmark extends TriangleCountBaseBenchmark {

    Buffer jniMatrix;

    @Param({"1", "8"})
    private int concurrency;

    @Setup
    public void setup() {
        super.setup();
        GRBCORE.initNonBlocking();
        jniMatrix = ToNativeMatrixConverter.convert(graph);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void jniSandia(Blackhole bh) {
        bh.consume(TriangleCountNative.computeTotalSandia(jniMatrix, concurrency));
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void jniNodeWise(Blackhole bh) {
        bh.consume(TriangleCountNative.computeNodeWise(jniMatrix, concurrency));
    }

    @Override
    @TearDown
    public void tearDown() {
        super.tearDown();

        GRBCORE.freeMatrix(jniMatrix);
        GRBCORE.grbFinalize();
    }
}
