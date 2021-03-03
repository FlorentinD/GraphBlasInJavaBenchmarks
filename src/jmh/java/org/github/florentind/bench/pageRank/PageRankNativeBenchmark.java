package org.github.florentind.bench.pageRank;


import com.github.fabianmurariu.unsafe.GRBCORE;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.github.florentind.graphalgos.pageRank.PageRankNative;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.Buffer;

public class PageRankNativeBenchmark extends PageRankBaseBenchmark {
    Buffer jniMatrix;

    // TODO allow parameter combinations
    //@Param({"1", "8"})
    private int concurrency = 8;

    private boolean by_col = true;

    @Override
    public void setup() {
        super.setup();
        GRBCORE.initNonBlocking();
        jniMatrix = ToNativeMatrixConverter.convert(graph, by_col);
    }

    @Override
    public void tearDown() {
        super.tearDown();

        GRBCORE.freeMatrix(jniMatrix);
        GRBCORE.grbFinalize();
    }

    @Override
    protected void benchmarkFunc() {
        PageRankNative.compute(jniMatrix, dampingFactor, tolerance, maxIterations, concurrency);
    }

    public static void main(String[] args) {
        new PageRankNativeBenchmark().run();
    }
}
