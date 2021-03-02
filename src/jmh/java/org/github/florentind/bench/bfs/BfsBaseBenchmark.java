package org.github.florentind.bench.bfs;


import org.github.florentind.bench.EjmlGraphBaseBenchmark;
import org.neo4j.graphalgo.Orientation;
import org.neo4j.graphalgo.api.CSRGraph;
import org.neo4j.graphalgo.beta.generator.RandomGraphGenerator;
import org.neo4j.graphalgo.beta.generator.RelationshipDistribution;
import org.neo4j.graphalgo.config.RandomGraphGeneratorConfig;
import org.neo4j.graphalgo.core.Aggregation;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;

public class BfsBaseBenchmark extends EjmlGraphBaseBenchmark {
    // node in the middle (id-wise)
    int startNode;

    // as JGraphT supports no max-iterations parameter
    final static int MAX_ITERATIONS = Integer.MAX_VALUE;

    @Override
    @Setup
    public void setup() {
        super.setup();
        startNode = Math.toIntExact(graph.nodeCount() / 2);
    }
}
