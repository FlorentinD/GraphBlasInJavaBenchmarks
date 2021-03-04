package org.github.florentind.bench;


import org.ejml.data.DMatrixSparseCSC;
import org.github.florentind.core.ejml.EjmlGraph;
import org.github.florentind.core.ejml.EjmlUtil;
import org.neo4j.graphalgo.StoreLoaderBuilder;
import org.neo4j.graphalgo.api.CSRGraph;
import org.neo4j.graphalgo.core.Aggregation;
import org.neo4j.graphalgo.core.GdsEdition;
import org.neo4j.kernel.internal.GraphDatabaseAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Benchmarks only based on EJML-Graphs
 */
public abstract class SimpleEjmlGraphBaseBenchmark {
    protected GraphDatabaseAPI db;
    private DataSetManager datasetManager;

    protected EjmlGraph graph;

    // untransposed version
    protected DMatrixSparseCSC getAdjacencyMatrix() {
        return EjmlUtil.getAdjacencyMatrix(graph);
    }

    int warmUpIterations = 5;
    int iterations = 10;

    protected CSRGraph getCSRGraph() {
        return (CSRGraph) new StoreLoaderBuilder()
                .api(db)
                .globalAggregation(Aggregation.SINGLE)
                .build()
                .graphStore()
                .getUnion();
    }

    public void setup(String dataset) {
        datasetManager = new DataSetManager("/home/florentin/masterThesis/graphblasOnJavaBenchmarks/datasets");
        db = datasetManager.openDb(dataset);

        var hugeGraph = getCSRGraph();
        System.out.println("nodeCount = " + hugeGraph.nodeCount());
        graph = EjmlGraph.create(hugeGraph);

        // for usage of higher concurrency in gds benchmarks
        GdsEdition.instance().setToEnterpriseEdition();

        hugeGraph.release();
    }

    public void tearDown() {
        datasetManager.closeDb(db);
        graph.release();
    }

    protected abstract void benchmarkFunc(Integer concurrency);

    protected abstract Map<String, String> parameterDesc();

    protected List<Integer> concurrencies() {
        return List.of(1);
    }


    protected List<String> datasets() {
        return List.of("LDBC01");
    }

    protected void run() {
        for (String dataset : datasets()) {
            setup(dataset);

            for (Integer concurrency : concurrencies()) {

                for (int i = 0; i < warmUpIterations; i++) {
                    benchmarkFunc(concurrency);
                    System.out.println(String.format("warmup: %d/%d", i + 1, warmUpIterations));
                }

                List<Long> timings = new ArrayList(iterations);
                System.out.println("Benchmark: " + this.getClass().getSimpleName());

                for (int i = 0; i < iterations; i++) {
                    var start = System.nanoTime();
                    benchmarkFunc(concurrency);
                    var end = System.nanoTime();
                    long duration = Math.round((end - start) / 1_000_000.0);
                    System.out.println("Iteration: " + i + ", time: " + duration + "ms");
                    timings.add(duration);
                }

                System.out.println("concurrency = " + concurrency);
                System.out.println("dataset = " + dataset);
                System.out.println("timings = " + timings);
                System.out.println("avg: " + timings.stream().reduce(0L, Long::sum) / timings.size());
                System.out.println("stats: " + timings.stream().mapToLong(Long::longValue).summaryStatistics().toString());

                tearDown();
            }
        }
    }
}
