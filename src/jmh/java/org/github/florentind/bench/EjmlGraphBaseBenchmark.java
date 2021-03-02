package org.github.florentind.bench;


import org.ejml.data.DMatrixSparseCSC;
import org.github.florentind.core.ejml.EjmlGraph;
import org.github.florentind.core.ejml.EjmlUtil;
import org.neo4j.graphalgo.StoreLoaderBuilder;
import org.neo4j.graphalgo.api.CSRGraph;
import org.neo4j.graphalgo.core.Aggregation;
import org.neo4j.graphalgo.core.GdsEdition;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.TearDown;

/**
 * Benchmarks only based on EJML-Graphs
 */
public class EjmlGraphBaseBenchmark extends BaseBenchmark {
    protected GraphDatabaseAPI db;
    private DataSetManager datasetManager;

    protected EjmlGraph graph;

    // untransposed version
    protected DMatrixSparseCSC getAdjacencyMatrix() {
        return EjmlUtil.getAdjacencyMatrix(graph);
    }

    @Param({"LiveJournal", "LDBC01", "POKEC"})
    String dataset;

    @Param({"1"})
    private int concurrency;

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
        datasetManager = new DataSetManager();
        db = datasetManager.openDb(dataset);

        var hugeGraph = getCSRGraph();
        System.out.println("nodeCount = " + hugeGraph.nodeCount());
        graph = EjmlGraph.create(hugeGraph);

        // for usage of higher concurrency in gds benchmarks
        GdsEdition.instance().setToEnterpriseEdition();

        hugeGraph.release();
    }

    @TearDown
    public void tearDown() {
        datasetManager.closeDb(db);
        graph.release();
    }
}
