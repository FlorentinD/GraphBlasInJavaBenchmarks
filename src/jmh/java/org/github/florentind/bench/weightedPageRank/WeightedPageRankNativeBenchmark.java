package org.github.florentind.bench.weightedPageRank;


import com.github.fabianmurariu.unsafe.GRBCORE;
import org.github.florentind.bench.pageRank.PageRankBaseBenchmark;
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
    private boolean blockingMode;

    @Setup
    public void setup() {
        super.setup();

        if (blockingMode) {
            // according to GraphBLAS only for debugging, but more resembles the ejml version
            GRBCORE.initBlocking();
        } else {
            GRBCORE.initNonBlocking();
        }

        assert blockingMode == (GRBCORE.getGlobalInt(GRBCORE.GxB_MODE) == GRBCORE.GrB_BLOCKING);

        jniMatrix = ToNativeMatrixConverter.convert(graph);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void jni(Blackhole bh) {
        bh.consume(PageRankNative.computeWeighted(jniMatrix, dampingFactor, tolerance, maxIterations, concurrency));
    }

    @TearDown
    public void tearDown() {
        super.tearDown();

        GRBCORE.freeMatrix(jniMatrix);
        GRBCORE.grbFinalize();
    }
}
