package org.github.florentind.bench.pageRank;


import org.github.florentind.core.jgrapht.JGraphTConverter;
import org.jgrapht.Graph;
import org.jgrapht.alg.scoring.PageRank;

public class PageRankJGraphTBenchmark extends PageRankBaseBenchmark {
    Graph jGraph;

    @Override
    public void setup(String dataset) {
        super.setup(dataset);
        jGraph = JGraphTConverter.convert(graph);
    }

    @Override
    protected void benchmarkFunc(Integer concurrency) {
        new PageRank(jGraph, dampingFactor, maxIterations, tolerance).getScores();
    }

    public static void main(String[] args) {
        new PageRankJGraphTBenchmark().run();
    }
}
