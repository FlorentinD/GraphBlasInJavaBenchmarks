package org.github.florentind.bench.triangleCount;

import org.github.florentind.core.jgrapht.JGraphTConverter;
import org.jgrapht.Graph;
import org.jgrapht.GraphMetrics;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

public class TriangleCountJGraphTBenchmark extends TriangleCountBaseBenchmark {
    Graph jGraph;

    @Param({"10000", "100000"})
    int nodeCount;

    @Setup
    public void setup() {
        super.setup();

        jGraph = JGraphTConverter.convert(graph);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void globalJGraphT(Blackhole bh) {
        bh.consume(GraphMetrics.getNumberOfTriangles(jGraph));
    }
}
