package org.github.florentind.bench.triangleCount;

import org.github.florentind.graphalgos.triangleCount.TriangleCountEjml;
import org.openjdk.jmh.infra.Blackhole;

public class TriangleCountEjmlBenchmark extends TriangleCountBaseBenchmark {

    @org.openjdk.jmh.annotations.Benchmark
    public void nodeWiseEjml(Blackhole bh) {
        boolean useLowerTriangle = true;
        bh.consume(TriangleCountEjml.computeNodeWise(graph.matrix(), useLowerTriangle));
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void globalSandiaEjml(Blackhole bh) {
        bh.consume(TriangleCountEjml.computeTotalSandia(graph.matrix()));
    }
}
