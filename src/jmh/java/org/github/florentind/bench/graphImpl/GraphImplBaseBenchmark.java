package org.github.florentind.bench.graphImpl;

import org.github.florentind.bench.BaseBenchmark;
import org.github.florentind.core.ejml.EjmlGraph;
import org.neo4j.graphalgo.StoreLoaderBuilder;
import org.neo4j.graphalgo.api.CSRGraph;
import org.neo4j.graphalgo.api.Graph;
import org.neo4j.graphalgo.compat.GdsGraphDatabaseAPI;
import org.neo4j.graphalgo.core.Aggregation;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;


public class GraphImplBaseBenchmark extends BaseBenchmark {
    GdsGraphDatabaseAPI db;

    Graph graph;

    @Param({"true", "false"})
    boolean isEjmlGraph;

    CSRGraph getCSRGraph() {
        return (CSRGraph) new StoreLoaderBuilder()
                .api(db)
                .globalAggregation(Aggregation.SINGLE)
                .build()
                .graphStore()
                .getUnion();
    }

    @Setup
    public void setup() {
        var hugeGraph = getCSRGraph();

        if (isEjmlGraph) {
            graph = EjmlGraph.create(hugeGraph);
            hugeGraph.release();
        } else {
            graph = hugeGraph;
        }
    }

    @TearDown
    public void tearDown() {
        graph.release();
    }
}
