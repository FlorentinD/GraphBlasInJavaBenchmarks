/*
 * Copyright (c) 2017-2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.florentind.bench.bfs;


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

public class BfsPregelBenchmark extends BfsEjmlBaseBenchmark {

    @Param({"1", "8"})
    private int concurrency;

    private BFSPregelConfig config;

    private int batchSize;

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

        batchSize = (int) ParallelUtil.adjustedBatchSize(graph.nodeCount(), config.concurrency());

        // init Pregel structures beforehand
        bfsLevelJob = Pregel.create(
                graph,
                config,
                new BFSLevelPregel(),
                batchSize,
                Pools.DEFAULT,
                AllocationTracker.EMPTY
        );

        bfsParentJob = Pregel.create(
                graph,
                config,
                new BFSParentPregel(),
                batchSize,
                Pools.DEFAULT,
                AllocationTracker.EMPTY
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
