package org.github.florentind.bench.bfs;

import org.github.florentind.graphalgos.bfs.BfsEjml;

public class BfsLevelEjmlBenchmark extends BfsBaseBenchmark {
    @Override
    protected void benchmarkFunc(Integer concurrency) {
        new BfsEjml().computeDenseSparse(graph.matrix(), BfsEjml.BfsVariation.LEVEL, startNode, MAX_ITERATIONS);
    }

    public static void main(String[] args) {
        new BfsLevelEjmlBenchmark().run();
    }
}
