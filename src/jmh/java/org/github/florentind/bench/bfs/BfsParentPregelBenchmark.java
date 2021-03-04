package org.github.florentind.bench.bfs;


import org.github.florentind.bench.pageRank.PageRankEjmlBenchmark;
import org.neo4j.graphalgo.beta.pregel.Pregel;
import org.neo4j.graphalgo.beta.pregel.bfs.BFSLevelPregel;
import org.neo4j.graphalgo.beta.pregel.bfs.BFSParentPregel;
import org.neo4j.graphalgo.beta.pregel.bfs.BFSPregelConfig;
import org.neo4j.graphalgo.beta.pregel.bfs.ImmutableBFSPregelConfig;
import org.neo4j.graphalgo.core.concurrency.Pools;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.openjdk.jmh.infra.Blackhole;

import java.util.List;

public class BfsParentPregelBenchmark extends BfsBaseBenchmark {

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

        var bfsParentJob = Pregel.create(
                graph,
                config,
                new BFSParentPregel(),
                Pools.DEFAULT,
                AllocationTracker.empty()
        );

        bfsParentJob.run();
    }

    public static void main(String[] args) {
        new BfsParentPregelBenchmark().run();
    }
}
