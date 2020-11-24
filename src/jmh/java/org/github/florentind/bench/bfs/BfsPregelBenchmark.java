package org.github.florentind.bench.bfs;


import org.neo4j.graphalgo.beta.pregel.Pregel;
import org.neo4j.graphalgo.beta.pregel.bfs.BFSLevelPregel;
import org.neo4j.graphalgo.beta.pregel.bfs.BFSParentPregel;
import org.neo4j.graphalgo.beta.pregel.bfs.BFSPregelConfig;
import org.neo4j.graphalgo.beta.pregel.bfs.ImmutableBFSPregelConfig;
import org.neo4j.graphalgo.core.concurrency.ParallelUtil;
import org.neo4j.graphalgo.core.concurrency.Pools;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.infra.Blackhole;

public class BfsPregelBenchmark extends BfsBaseBenchmark {

    @Param({"1", "8"})
    private int concurrency;

    private BFSPregelConfig config;

    private Pregel<BFSPregelConfig> bfsLevelJob;

    private Pregel<BFSPregelConfig> bfsParentJob;


    @Override
    public void setup() {
        super.setup();

        config = ImmutableBFSPregelConfig.builder()
                .maxIterations(MAX_ITERATIONS)
                .startNode(0)
                .concurrency(concurrency)
                .build();
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void pregelBfsLevel(Blackhole bh) {
        bfsLevelJob = Pregel.create(
                graph,
                config,
                new BFSLevelPregel(),
                Pools.DEFAULT,
                AllocationTracker.empty()
        );

        bh.consume(bfsLevelJob.run());
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void pregelBfsParent(Blackhole bh) {
        bfsParentJob = Pregel.create(
                graph,
                config,
                new BFSParentPregel(),
                Pools.DEFAULT,
                AllocationTracker.empty()
        );

        bh.consume(bfsParentJob.run());
    }
}
