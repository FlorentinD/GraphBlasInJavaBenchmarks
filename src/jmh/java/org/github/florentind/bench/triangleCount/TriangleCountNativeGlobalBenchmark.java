package org.github.florentind.bench.triangleCount;

import com.github.fabianmurariu.unsafe.GRBCORE;
import org.github.florentind.bench.pageRank.PageRankPregelBenchmark;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.github.florentind.graphalgos.triangleCount.TriangleCountNative;

import java.nio.Buffer;
import java.util.List;

public class TriangleCountNativeGlobalBenchmark extends TriangleCountBaseBenchmark {

    Buffer jniMatrix;

    @Override
    protected List<Integer> concurrencies() {
        return List.of(1, 8);
    }

    public void setup(String dataset) {
        super.setup(dataset);
        GRBCORE.initNonBlocking();
        jniMatrix = ToNativeMatrixConverter.convert(graph);
    }

    @Override
    protected void benchmarkFunc(Integer concurrency) {
        TriangleCountNative.computeTotalSandia(jniMatrix, concurrency);
    }

    @Override
    public void tearDown() {
        super.tearDown();

        GRBCORE.freeMatrix(jniMatrix);
        GRBCORE.grbFinalize();
    }

    public static void main(String[] args) {
        new TriangleCountNativeGlobalBenchmark().run();
    }
}
