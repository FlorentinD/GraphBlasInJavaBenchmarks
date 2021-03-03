package org.github.florentind.bench.weightedPageRank;


import org.github.florentind.core.jgrapht.JGraphTConverter;
import org.jgrapht.Graph;
import org.jgrapht.alg.scoring.PageRank;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

public class WeightedPageRankJGraphTBenchmark extends WeightedPageRankBaseBenchmark {
    Graph jGraph;

    @Override
    public void setup() {
        super.setup();
        jGraph = JGraphTConverter.convert(graph);
    }

    @Override
    protected void benchmarkFunc() {
        new PageRank(jGraph, dampingFactor, maxIterations, tolerance).getScores();
    }
}
