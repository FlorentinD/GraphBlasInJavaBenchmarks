package org.github.florentind.bench.triangleCount;

import org.github.florentind.bench.pageRank.PageRankPregelBenchmark;
import org.github.florentind.graphalgos.triangleCount.TriangleCountEjml;
import org.openjdk.jmh.infra.Blackhole;

public class TriangleCountEjmlNodeWiseBenchmark extends TriangleCountBaseBenchmark {
    // not using getAdjacencyMatrix (as the graph is unweighted and therefore A_T == A, e.g. no need to transpose)

    @Override
    protected void benchmarkFunc(Integer concurrency) {
        boolean useLowerTriangle = true;
        TriangleCountEjml.computeNodeWise(graph.matrix(), useLowerTriangle);
    }

    public static void main(String[] args) {
        new TriangleCountEjmlNodeWiseBenchmark().run();
    }
}
