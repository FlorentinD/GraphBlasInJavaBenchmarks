package org.github.florentind.bench;


import org.ejml.data.DMatrixSparseCSC;
import org.github.florentind.core.ejml.EjmlGraph;
import org.github.florentind.core.ejml.EjmlUtil;
import org.neo4j.graphalgo.StoreLoaderBuilder;
import org.neo4j.graphalgo.api.CSRGraph;
import org.neo4j.graphalgo.compat.GdsGraphDatabaseAPI;
import org.neo4j.graphalgo.core.Aggregation;
import org.neo4j.graphalgo.core.GdsEdition;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * Benchmarks only based on EJML-Graphs
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
@Fork(value = 1, warmups = 2)
public class EjmlGraphBaseBenchmark {
    GdsGraphDatabaseAPI db;

    protected EjmlGraph graph;
    // untransposed version
    protected DMatrixSparseCSC getAdjacencyMatrix() {
        return EjmlUtil.getAdjacencyMatrix(graph);
    }

//    DatasetManager datasetManager;

//    @Param({"empty"})
//    String dataset;


    protected CSRGraph getCSRGraph() {
        return (CSRGraph) new StoreLoaderBuilder()
            .api(db)
            .globalAggregation(Aggregation.SINGLE)
            .build()
            .graphStore()
            .getUnion();
    }

    @Setup
    public void setup() {
//        datasetManager = new DatasetManager(Path.of("/tmp"));

//        db = datasetManager.openDb(dataset);
        var hugeGraph = getCSRGraph();

        graph = EjmlGraph.create(hugeGraph);

        // for usage of higher concurrencies in gds benchmarks
        GdsEdition.instance().setToEnterpriseEdition();


        hugeGraph.release();
    }

    @TearDown
    public void tearDown() {
//        datasetManager.closeDb(db);
        graph.release();
    }
}
