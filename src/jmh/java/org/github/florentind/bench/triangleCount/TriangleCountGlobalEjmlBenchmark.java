package org.github.florentind.bench.triangleCount;

import org.github.florentind.graphalgos.triangleCount.TriangleCountEjml;

public class TriangleCountGlobalEjmlBenchmark extends TriangleCountBaseBenchmark {
    // not using getAdjacencyMatrix (as the graph is unweighted and therefore A_T == A, e.g. no need to transpose)

    @Override
    protected void benchmarkFunc(Integer concurrency) {
        TriangleCountEjml.computeTotalSandia(graph.matrix());
    }

    public static void main(String[] args) {
        new TriangleCountGlobalEjmlBenchmark().run();
    }
}
