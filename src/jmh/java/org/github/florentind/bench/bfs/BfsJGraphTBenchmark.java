package org.github.florentind.bench.bfs;


import org.github.florentind.core.jgrapht.JGraphTConverter;
import org.jgrapht.Graph;
import org.jgrapht.alg.shortestpath.BFSShortestPath;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

public class BfsJGraphTBenchmark extends BfsBaseBenchmark {
    Graph jGraph;

    @Override
    @Setup
    public void setup() {
        super.setup();

        jGraph = JGraphTConverter.convert(graph);
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void jGraphTBfsLevel(Blackhole bh) {
        bh.consume(new BFSShortestPath<>(jGraph).getPaths(startNode));
    }
}
