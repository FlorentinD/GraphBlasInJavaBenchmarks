package org.github.florentind.bench.pageRank;


import org.github.florentind.core.jgrapht.JGraphTConverter;
import org.jgrapht.Graph;
import org.jgrapht.alg.scoring.PageRank;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

public class PageRankJGraphTBenchmark extends PageRankBaseBenchmark {
    Graph jGraph;

    @Setup
    public void setup() {
        super.setup();

        jGraph = JGraphTConverter.convert(graph);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void jGraphT(Blackhole bh) {
        bh.consume(new PageRank(jGraph, dampingFactor, maxIterations, tolerance).getScores());
    }
}
