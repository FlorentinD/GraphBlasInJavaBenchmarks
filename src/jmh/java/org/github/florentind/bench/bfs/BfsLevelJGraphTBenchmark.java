package org.github.florentind.bench.bfs;


import org.github.florentind.core.jgrapht.JGraphTConverter;
import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.BFSShortestPath;

public class BfsLevelJGraphTBenchmark extends BfsBaseBenchmark {
    Graph jGraph;

    @Override
    public void setup(String dataset) {
        super.setup(dataset);
        jGraph = JGraphTConverter.convert(graph);
    }

    @Override
    protected void benchmarkFunc(Integer concurrency) {
        new BFSShortestPath<>(jGraph).getPaths(startNode);
    }

    public static void main(String[] args) {
        new BfsLevelJGraphTBenchmark().run();
    }
}
