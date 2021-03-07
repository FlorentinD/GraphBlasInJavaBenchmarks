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

    protected int warmUpIterations = 5;
    protected int iterations = 10;

    protected CSRGraph getCSRGraph() {
        return (CSRGraph) new StoreLoaderBuilder()
                .api(db)
                .globalAggregation(Aggregation.SINGLE)
                .build()
                .graphStore()
                .getUnion();
    }

    public void setup(String dataset) {
        datasetManager = new DataSetManager();
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

    protected List<Integer> concurrencies() {
        return List.of(1);
    }


    protected List<String> datasets() {
        return List.of("Facebook", "LDBC01", "POKEC");
    }

    public List<BenchmarkResult> run() {
        List<BenchmarkResult> results = new ArrayList<>(datasets().size() * concurrencies().size());

        for (String dataset : datasets()) {
            setup(dataset);

            for (Integer concurrency : concurrencies()) {

                for (int i = 0; i < warmUpIterations; i++) {
                    try {
                        benchmarkFunc(concurrency);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        break;
                    }
                    System.out.printf("warmup: %d/%d%n", i + 1, warmUpIterations);
                }

                List<Long> timings = new ArrayList<>(iterations);
                System.out.println("Benchmark: " + this.getClass().getSimpleName());

                for (int i = 0; i < iterations; i++) {
                    var start = System.nanoTime();
                    try {
                        benchmarkFunc(concurrency);
                        var end = System.nanoTime();
                        long duration = Math.round((end - start) / 1_000_000.0);
                        System.out.println("Iteration: " + i + ", time: " + duration + "ms");
                        timings.add(duration);
                    } catch (Throwable e) {
                        e.printStackTrace();
                        break;
                    }
                }

                if (timings.size() > 0) {
                    timings.sort(Long::compare);
                    long median = timings.get(Math.round(timings.size() / 2.0f));
                    System.out.println("median: " + median);
                    System.out.println("stats: " + timings.stream().mapToLong(Long::longValue).summaryStatistics().toString());
                    results.add(new BenchmarkResult(this.getClass().getSimpleName(), concurrency, dataset, median));
                }

            }
            tearDown();
        }
        printResults(results);
        return results;
    }

    protected static void printResults(List<BenchmarkResult> results) {
        System.out.println("----------------------------");
        System.out.println(results.get(0).header());
        results.forEach((r) -> System.out.println(r.toString()));
    }

    protected class BenchmarkResult {
        String benchmark;
        Integer concurrency;
        String dataSet;
        long median;

        public BenchmarkResult(String benchmark, Integer concurrency, String dataSet, long median) {
            this.benchmark = benchmark;
            this.concurrency = concurrency;
            this.dataSet = dataSet;
            this.median = median;
        }

        public String header() {
            return "benchmark, concurrency, dataset, median";
        }

        @Override
        public String toString() {
            return benchmark + ',' + concurrency + ',' + dataSet + "," + median;
        }
    }
}
