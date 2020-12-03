package org.github.florentind.bench.loading;


import org.github.florentind.core.ejml.EjmlGraph;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.github.florentind.core.jgrapht.JGraphTConverter;
import org.neo4j.graphalgo.Orientation;
import org.neo4j.graphalgo.beta.generator.PropertyProducer;
import org.neo4j.graphalgo.beta.generator.RandomGraphGenerator;
import org.neo4j.graphalgo.beta.generator.RandomGraphGeneratorBuilder;
import org.neo4j.graphalgo.beta.generator.RelationshipDistribution;
import org.neo4j.graphalgo.config.RandomGraphGeneratorConfig;
import org.neo4j.graphalgo.core.Aggregation;
import org.neo4j.graphalgo.core.huge.HugeGraph;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

import static com.github.fabianmurariu.unsafe.GRBCORE.*;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
@Fork(value = 2, warmups = 1)
public class GraphLoadBenchmark {

    @Param({"1000000"})
    int nodeCount;

    @Param({"2", "4", "8"})
    int avgDegree;

    @Param({"POWER_LAW"})
    String degreeDistribution;

    @Param({"true", "false"})
    boolean weighted;

    @Param({"1"})
    int concurrency;

    protected HugeGraph graph;

    @Setup
    public void setup() {

        RandomGraphGeneratorBuilder builder = RandomGraphGenerator.builder()
                .nodeCount(nodeCount)
                .averageDegree(avgDegree)
                .seed(42L)
                .aggregation(Aggregation.MAX)
                .orientation(Orientation.NATURAL)
                .allocationTracker(AllocationTracker.empty())
                .allowSelfLoops(RandomGraphGeneratorConfig.AllowSelfLoops.NO)
                .relationshipDistribution(RelationshipDistribution.valueOf(degreeDistribution));

        if (weighted) {
            builder.relationshipPropertyProducer(PropertyProducer.random("weight", 0, 1));
        }

        graph = builder.build().generate();

        initBlocking();
        setGlobalInt(GxB_NTHREADS, concurrency);
    }

    @Benchmark
    public void loadEjmlGraph(Blackhole bh) {
        bh.consume(EjmlGraph.create(graph));
    }

    @Benchmark
    public void loadJniGraph(Blackhole bh) {
        bh.consume(matrixWait(ToNativeMatrixConverter.convert(graph)));
    }

    // directed, weighted graph
    @Benchmark
    public void loadJGraphTGraph(Blackhole bh) {
        bh.consume(JGraphTConverter.convert(graph));
    }
//    @Benchmark
//    public void loadJniEdeWise(Blackhole bh) {
//        bh.consume(GRBCORE.matrixWait(ToNativeMatrixConverter.convertEdgeWise(graph, true)));
//    }


    @TearDown
    public void tearDown() {
        grbFinalize();
        graph.release();
    }
}
