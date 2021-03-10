package org.github.florentind.bench.triangleCount;

import org.github.florentind.graphalgos.triangleCount.TriangleCountEjml;

public class TriangleCountVertexWiseEjmlBenchmark extends TriangleCountBaseBenchmark {
    // not using getAdjacencyMatrix (as the graph is unweighted and therefore A_T == A, e.g. no need to transpose)

    @Override
    protected void benchmarkFunc(Integer concurrency) {
        boolean useLowerTriangle = true;
        TriangleCountEjml.computeNodeWise(graph.matrix(), useLowerTriangle);
    }

    public static void main(String[] args) {
        new TriangleCountVertexWiseEjmlBenchmark().run();
    }
}
