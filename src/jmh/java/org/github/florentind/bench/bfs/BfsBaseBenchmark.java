package org.github.florentind.bench.bfs;


import org.github.florentind.bench.SimpleEjmlGraphBaseBenchmark;

import java.util.ArrayList;
import java.util.List;

public abstract class BfsBaseBenchmark extends SimpleEjmlGraphBaseBenchmark {
    // node in the middle (id-wise)
    int startNode;

    // as JGraphT supports no max-iterations parameter
    final static int MAX_ITERATIONS = Integer.MAX_VALUE;

    @Override
    public void setup(String dataset) {
        super.setup(dataset);
        startNode = Math.toIntExact(graph.nodeCount() / 2);
    }

    public static void main(String[] args) {
        List<SimpleEjmlGraphBaseBenchmark> benchmarks = List.of(
                new BfsLevelEjmlBenchmark(),
                new BfsLevelJGraphTBenchmark(),
                new BfsLevelNativeBenchmark(),
                new BfsLevelPregelBenchmark(),
                new BfsParentPregelBenchmark(),
                new BfsParentEjmlBenchmark(),
                new BfsParentNativeBenchmark()
        );
        List<BenchmarkResult> results = benchmarks.stream()
                .map(SimpleEjmlGraphBaseBenchmark::run)
                .reduce(new ArrayList<>(), (acc, result) -> {
                    acc.addAll(result);
                    return acc;
                });

        printResults(results);
    }
}
