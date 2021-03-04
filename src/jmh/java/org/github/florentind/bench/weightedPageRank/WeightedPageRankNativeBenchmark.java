package org.github.florentind.bench.weightedPageRank;


import com.github.fabianmurariu.unsafe.GRBCORE;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.github.florentind.graphalgos.pageRank.PageRankNative;

import java.nio.Buffer;
import java.util.List;

public class WeightedPageRankNativeBenchmark extends WeightedPageRankBaseBenchmark {
    Buffer jniMatrix;


    @Override
    protected List<Integer> concurrencies() {
        return List.of(1, 8);
    }

    private boolean by_col = true;

    @Override
    public void setup(String dataset) {
        super.setup(dataset);
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
    protected void benchmarkFunc(Integer concurrency) {
        PageRankNative.computeWeighted(jniMatrix, dampingFactor, tolerance, maxIterations, concurrency);
    }
}
