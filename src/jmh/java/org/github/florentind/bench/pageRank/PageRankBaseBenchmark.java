package org.github.florentind.bench.pageRank;


import org.github.florentind.bench.SimpleEjmlGraphBaseBenchmark;

import java.util.ArrayList;
import java.util.List;

public abstract class PageRankBaseBenchmark extends SimpleEjmlGraphBaseBenchmark {

    protected int maxIterations = 20;

    protected float dampingFactor = 0.85f;

    protected float tolerance = 1e-32f;

    public static void main(String[] args) {
        List<SimpleEjmlGraphBaseBenchmark> benchmarks = List.of(
                new PageRankNativeBenchmark(),
                new PageRankEjmlBenchmark(),
                new PageRankPregelBenchmark(),
                new PageRankJGraphTBenchmark(),
                new PageRankGdsBenchmark()
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
