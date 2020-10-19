package org.github.florentind.bench.bfs;

import com.github.fabianmurariu.unsafe.GRBCORE;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
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

    @Param({"false"})
    private boolean blockingMode;

    protected Buffer jniMatrix;

    @Override
    @Setup
    public void setup() {
        super.setup();

        if (blockingMode) {
            // according to GraphBLAS only for debugging, but more resembles the ejml version
            GRBCORE.initBlocking();
        }else {
            GRBCORE.initNonBlocking();
        }

        assert blockingMode == (GRBCORE.getGlobalInt(GRBCORE.GxB_MODE) == GRBCORE.GrB_BLOCKING);

        jniMatrix = ToNativeMatrixConverter.convert(getAdjacencyMatrix());
    }


    @Benchmark
    public void jniBfsLevel(Blackhole bh) {
        bh.consume(new BfsNative().computeLevel(jniMatrix, startNode, maxIterations, concurrency));
    }

    @TearDown
    public void tearDown() {
        super.tearDown();

        GRBCORE.freeMatrix(jniMatrix);
        GRBCORE.grbFinalize();
    }
}
