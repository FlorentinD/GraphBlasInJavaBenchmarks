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
                .maxIterations(maxIterations)
                .startNode(0)
                .concurrency(concurrency)
                .build();

        // init Pregel structures beforehand
        bfsLevelJob = Pregel.create(
                graph,
                config,
                new BFSLevelPregel(),
                Pools.DEFAULT,
                AllocationTracker.empty()
        );

        bfsParentJob = Pregel.create(
                graph,
                config,
                new BFSParentPregel(),
                Pools.DEFAULT,
                AllocationTracker.empty()
        );
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void pregelBfsLevel(Blackhole bh) {
        bh.consume(bfsLevelJob.run());
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void pregelBfsParent(Blackhole bh) {
        bh.consume(bfsParentJob.run());
    }
}
