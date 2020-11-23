package org.github.florentind.bench.graphImpl;

import org.github.florentind.core.ejml.EjmlGraph;
import org.neo4j.graphalgo.StoreLoaderBuilder;
import org.neo4j.graphalgo.api.CSRGraph;
import org.neo4j.graphalgo.api.Graph;
import org.neo4j.graphalgo.compat.GdsGraphDatabaseAPI;
import org.neo4j.graphalgo.core.Aggregation;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
@Fork(value = 2, warmups = 1)
public class GraphImplBaseBenchmark {
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

        if(isEjmlGraph) {
            graph = EjmlGraph.create(hugeGraph);
            hugeGraph.release();
        }
        else {
            graph = hugeGraph;
        }
    }

    @TearDown
    public void tearDown() {
        graph.release();
    }
}
