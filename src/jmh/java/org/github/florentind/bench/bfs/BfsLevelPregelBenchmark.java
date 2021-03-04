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

import java.util.List;

public class BfsLevelPregelBenchmark extends BfsBaseBenchmark {

    @Override
    protected List<Integer> concurrencies() {
        return List.of(1, 8);
    }

    @Override
    protected void benchmarkFunc(Integer concurrency) {
        BFSPregelConfig config = ImmutableBFSPregelConfig.builder()
                .maxIterations(MAX_ITERATIONS)
                .startNode(0)
                .concurrency(concurrency)
                .build();

        var bfsLevelJob = Pregel.create(
                graph,
                config,
                new BFSLevelPregel(),
                Pools.DEFAULT,
                AllocationTracker.empty()
        );

        bfsLevelJob.run();
    }

    public static void main(String[] args) {
        new BfsLevelPregelBenchmark().run();
    }
}
