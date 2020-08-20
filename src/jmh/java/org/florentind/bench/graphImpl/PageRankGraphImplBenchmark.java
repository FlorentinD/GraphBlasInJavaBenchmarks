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
package org.florentind.bench.graphImpl;


import org.neo4j.graphalgo.Orientation;
import org.neo4j.graphalgo.api.CSRGraph;
import org.neo4j.graphalgo.beta.generator.PropertyProducer;
import org.neo4j.graphalgo.beta.generator.RandomGraphGenerator;
import org.neo4j.graphalgo.beta.generator.RelationshipDistribution;
import org.neo4j.graphalgo.config.RandomGraphGeneratorConfig;
import org.neo4j.graphalgo.core.Aggregation;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.neo4j.graphalgo.pagerank.ImmutablePageRankStatsConfig;
import org.neo4j.graphalgo.pagerank.PageRank;
import org.neo4j.graphalgo.pagerank.PageRankBaseConfig;
import org.neo4j.graphalgo.pagerank.PageRankFactory;
import org.neo4j.logging.NullLog;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Optional;


public class PageRankGraphImplBenchmark extends GraphImplBaseBenchmark {

    private PageRankBaseConfig unweightedConfig;
    private PageRankBaseConfig weightedConfig;

    private static String RELATIONSHIP_PROPERTY = "weight";

    @Param({"4"})
    private int concurrency;

    @Param({"3000000"})
    private int nodeCount;

    @Param({"4"})
    private int avgDegree;

    @Override
    CSRGraph getCSRGraph() {
        RandomGraphGenerator generator = new RandomGraphGenerator(
            nodeCount,
            avgDegree,
            RelationshipDistribution.POWER_LAW,
            42L,
            Optional.empty(),
            Optional.of(new PropertyProducer.Random(RELATIONSHIP_PROPERTY, -3, 3)),
            Aggregation.MAX,
            Orientation.NATURAL,
            RandomGraphGeneratorConfig.AllowSelfLoops.YES,
            AllocationTracker.EMPTY
        );
        return generator.generate();
    }

    @Setup(Level.Invocation)
    public void setup() {
        super.setup();

        unweightedConfig =  ImmutablePageRankStatsConfig
            .builder()
            .concurrency(concurrency)
            .build();

        weightedConfig = ImmutablePageRankStatsConfig
            .builder()
            .from(unweightedConfig)
            .relationshipWeightProperty(RELATIONSHIP_PROPERTY)
            .build();
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void unweighted(Blackhole bh) {
        PageRank algorithm = new PageRankFactory<>().build(
            graph,
            unweightedConfig,
            AllocationTracker.EMPTY,
            NullLog.getInstance()
        );

        bh.consume(algorithm.compute());
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void weighted(Blackhole bh) {
        PageRank algorithm = new PageRankFactory<>().build(
            graph,
            weightedConfig,
            AllocationTracker.EMPTY,
            NullLog.getInstance()
        );

        bh.consume(algorithm.compute());
    }
}
