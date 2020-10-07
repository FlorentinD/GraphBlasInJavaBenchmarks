
package org.github.florentind.bench.ejmlOps;

import org.ejml.data.DMatrixSparseCSC;
import org.github.florentind.core.ejml.EjmlGraph;
import org.neo4j.graphalgo.beta.generator.RandomGraphGenerator;
import org.neo4j.graphalgo.beta.generator.RelationshipDistribution;
import org.neo4j.graphalgo.config.RandomGraphGeneratorConfig;
import org.neo4j.graphalgo.core.Aggregation;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;


@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
@Fork(value = 1, warmups = 2)
public abstract class MatrixOpsBaseBenchmark {
    protected DMatrixSparseCSC matrix;

    @Param({"300000"})
    private int dimension;

    @Param({"4"})
    private int avgDegree;

    @Param({"UNIFORM"})
    private String degreeDistribution;

    @Setup
    public void setup() {
        // TODO: replace with RandomMatrix generator when its faster
        matrix = EjmlGraph.create(RandomGraphGenerator.builder()
                .nodeCount(dimension)
                .averageDegree(avgDegree)
                .seed(42L)
                .aggregation(Aggregation.MAX)
                .allocationTracker(AllocationTracker.empty())
                .allowSelfLoops(RandomGraphGeneratorConfig.AllowSelfLoops.YES)
                .relationshipDistribution(RelationshipDistribution.valueOf(degreeDistribution))
                .build().generate()).matrix();
//        matrix = RandomMatrices_DSCC.rectangle(dimension, dimension, dimension * avgDegree, new Random(42));
    }
}
