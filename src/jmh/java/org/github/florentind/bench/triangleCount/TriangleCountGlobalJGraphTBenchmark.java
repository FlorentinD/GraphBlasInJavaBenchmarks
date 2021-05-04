package org.github.florentind.bench.triangleCount;

import org.github.florentind.core.jgrapht.JGraphTConverter;
import org.jgrapht.Graph;
import org.jgrapht.GraphMetrics;
import org.openjdk.jmh.annotations.Setup;

public class TriangleCountGlobalJGraphTBenchmark extends TriangleCountBaseBenchmark {
    Graph jGraph;

    @Setup
    public void setup(String dataset) {
        super.setup(dataset);
        jGraph = JGraphTConverter.convert(graph);
    }

    @Override
    protected void benchmarkFunc(Integer concurrency) {
        GraphMetrics.getNumberOfTriangles(jGraph);
    }

    public static void main(String[] args) {
        new TriangleCountGlobalJGraphTBenchmark().run();
    }
}
