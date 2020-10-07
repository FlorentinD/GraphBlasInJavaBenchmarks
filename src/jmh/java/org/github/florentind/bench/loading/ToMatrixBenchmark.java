package org.github.florentind.bench.loading;


import com.github.fabianmurariu.unsafe.GRAPHBLAS;
import com.github.fabianmurariu.unsafe.GRBCORE;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.github.florentind.core.ejml.EjmlGraph;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.github.florentind.core.jgrapht.JGraphTConverter;
import org.neo4j.graphalgo.StoreLoaderBuilder;
import org.neo4j.graphalgo.api.CSRGraph;
import org.neo4j.graphalgo.beta.generator.RandomGraphGenerator;
import org.neo4j.graphalgo.beta.generator.RelationshipDistribution;
import org.neo4j.graphalgo.compat.GdsGraphDatabaseAPI;
import org.neo4j.graphalgo.config.RandomGraphGeneratorConfig;
import org.neo4j.graphalgo.core.Aggregation;
import org.neo4j.graphalgo.core.GdsEdition;
import org.neo4j.graphalgo.core.huge.HugeGraph;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
@Fork(value = 1, warmups = 2)
public class ToMatrixBenchmark {

    @Param({"300000", "3000000"})
    int nodeCount;

    @Param({"4"})
    int avgDegree;

    @Param({"POWER_LAW", "UNIFORM"})
    String degreeDistribution;

    @Param({"1"})
    int concurrency;


    protected HugeGraph graph;


    @Setup
    public void setup() {

        graph = RandomGraphGenerator.builder()
                .nodeCount(nodeCount)
                .averageDegree(avgDegree)
                .seed(42L)
                .aggregation(Aggregation.MAX)
                .allocationTracker(AllocationTracker.empty())
                .allowSelfLoops(RandomGraphGeneratorConfig.AllowSelfLoops.YES)
                .relationshipDistribution(RelationshipDistribution.valueOf(degreeDistribution))
                .build().generate();

        GRBCORE.initBlocking();
        GRBCORE.setGlobalInt(GRBCORE.GxB_NTHREADS, concurrency);
    }

    @Benchmark
    public void loadEjmlGraph(Blackhole bh) {
        bh.consume(EjmlGraph.create(graph));
    }

    @Benchmark
    public void loadJniGraph(Blackhole bh) {
        bh.consume(ToNativeMatrixConverter.convert(graph));
    }

    @TearDown
    public void tearDown() {
//        datasetManager.closeDb(db);

        GRBCORE.grbFinalize();
        graph.release();
    }
}
