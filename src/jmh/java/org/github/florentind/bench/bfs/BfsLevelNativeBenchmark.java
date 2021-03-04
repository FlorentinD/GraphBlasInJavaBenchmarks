package org.github.florentind.bench.bfs;

import com.github.fabianmurariu.unsafe.GRBCORE;
import com.github.fabianmurariu.unsafe.GRBOPSMAT;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.github.florentind.graphalgos.bfs.BfsNative;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.Buffer;
import java.util.List;

public class BfsLevelNativeBenchmark extends BfsNativeBenchmark {

    @Override
    protected void benchmarkFunc(Integer concurrency) {
        new BfsNative().computeLevel(jniMatrix, startNode, MAX_ITERATIONS, concurrency);
    }

    public static void main(String[] args) {
        new BfsLevelNativeBenchmark().run();
    }
}
