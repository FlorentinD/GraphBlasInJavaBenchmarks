package org.github.florentind.bench.weightedPageRank;


import com.github.fabianmurariu.unsafe.GRBCORE;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.github.florentind.graphalgos.pageRank.PageRankNative;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.Buffer;

public class WeightedPageRankNativeBenchmark extends WeightedPageRankBaseBenchmark {
    Buffer jniMatrix;

    @Param({"1", "8"})
    private int concurrency;

    @Param({"true"})
    private boolean by_col;

    @Override
    @Setup
    public void setup() {
        super.setup();
        GRBCORE.initNonBlocking();
        jniMatrix = ToNativeMatrixConverter.convert(graph, by_col);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void jni(Blackhole bh) {
        bh.consume(PageRankNative.computeWeighted(jniMatrix, dampingFactor, tolerance, maxIterations, concurrency));
    }

    @Override
    @TearDown
    public void tearDown() {
        super.tearDown();
        GRBCORE.freeMatrix(jniMatrix);
        GRBCORE.grbFinalize();
    }
}
