package org.github.florentind.bench.cc;


import org.github.florentind.bench.bfs.BfsBaseBenchmark;
import org.neo4j.graphalgo.beta.pregel.Pregel;
import org.neo4j.graphalgo.beta.pregel.cc.ConnectedComponentsConfig;
import org.neo4j.graphalgo.beta.pregel.cc.ConnectedComponentsPregel;
import org.neo4j.graphalgo.beta.pregel.cc.ImmutableConnectedComponentsConfig;
import org.neo4j.graphalgo.core.concurrency.ParallelUtil;
import org.neo4j.graphalgo.core.concurrency.Pools;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.infra.Blackhole;

// TODO clarify .. strongly or weakly connected components? (should be strongly in pregel)
public class ConnectedComponentesPregelBenchmark extends BfsBaseBenchmark {

    @Param({"1", "8"})
    private int concurrency;

    private ConnectedComponentsConfig config;

    private Pregel<ConnectedComponentsConfig> sccJob;

    @Override
    public void setup() {
        super.setup();

        config = ImmutableConnectedComponentsConfig.builder()
                .maxIterations(maxIterations)
                .concurrency(concurrency)
                .build();

        // init Pregel structures beforehand
        sccJob = Pregel.create(
                graph,
                config,
                new ConnectedComponentsPregel(),
                Pools.DEFAULT,
                AllocationTracker.EMPTY
        );
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void pregelScc(Blackhole bh) {
        bh.consume(sccJob.run());
    }
}
