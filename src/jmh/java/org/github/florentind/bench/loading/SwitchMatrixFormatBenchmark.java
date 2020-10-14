package org.github.florentind.bench.loading;


import com.github.fabianmurariu.unsafe.GRBCORE;
import org.github.florentind.core.ejml.EjmlGraph;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.neo4j.graphalgo.beta.generator.RandomGraphGenerator;
import org.neo4j.graphalgo.beta.generator.RelationshipDistribution;
import org.neo4j.graphalgo.config.RandomGraphGeneratorConfig;
import org.neo4j.graphalgo.core.Aggregation;
import org.neo4j.graphalgo.core.huge.HugeGraph;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.nio.Buffer;
import java.util.concurrent.TimeUnit;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
@Fork(value = 1, warmups = 2)
public class SwitchMatrixFormatBenchmark {

    protected HugeGraph graph;

    Buffer jniMatrix;

    @Param({"300000", "3000000"})
    int nodeCount;

    @Param({"4"})
    int avgDegree;

    @Param({"POWER_LAW", "UNIFORM"})
    String degreeDistribution;

    @Param({"1"})
    int concurrency;

    @Param({"true", "false"})
    boolean byCol;


    @Setup(Level.Trial)
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

        jniMatrix = ToNativeMatrixConverter.convert(graph, byCol);
    }

    // TODO: atm this takes no time at all
//    @Benchmark
//    public void switchMatrixFormat(Blackhole bh) {
//        if (byCol) {
//            bh.consume(GRBCORE.makeCSR(jniMatrix));
//        } else {
//            bh.consume(GRBCORE.makeCSC(jniMatrix));
//        }
//    }

    @TearDown
    public void tearDown() {
        GRBCORE.grbFinalize();
        graph.release();
    }
}