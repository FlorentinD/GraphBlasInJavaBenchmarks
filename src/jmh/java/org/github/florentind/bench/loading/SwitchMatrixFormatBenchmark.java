package org.github.florentind.bench.loading;


import com.github.fabianmurariu.unsafe.GRBCORE;
import org.github.florentind.bench.BaseBenchmark;
import org.github.florentind.bench.EjmlGraphBaseBenchmark;
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


public class SwitchMatrixFormatBenchmark extends EjmlGraphBaseBenchmark {

    Buffer jniMatrix;

    @Param({"1"})
    int concurrency;

    @Param({"true", "false"})
    boolean byCol;


    @Setup
    public void setup() {
        GRBCORE.initBlocking();
        GRBCORE.setGlobalInt(GRBCORE.GxB_NTHREADS, concurrency);

        jniMatrix = ToNativeMatrixConverter.convert(graph, byCol);
    }

    //this takes no time at all
    @Benchmark
    public void switchMatrixFormat(Blackhole bh) {
        if (byCol) {
            bh.consume(GRBCORE.makeCSR(jniMatrix));
        } else {
            bh.consume(GRBCORE.makeCSC(jniMatrix));
        }
    }

    @TearDown
    public void tearDown() {
        GRBCORE.grbFinalize();
        graph.release();
    }
}
