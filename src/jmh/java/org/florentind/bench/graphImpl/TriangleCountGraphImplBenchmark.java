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
import org.neo4j.graphalgo.beta.generator.RandomGraphGenerator;
import org.neo4j.graphalgo.beta.generator.RelationshipDistribution;
import org.neo4j.graphalgo.config.RandomGraphGeneratorConfig;
import org.neo4j.graphalgo.core.Aggregation;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.neo4j.graphalgo.triangle.ImmutableTriangleCountStatsConfig;
import org.neo4j.graphalgo.triangle.IntersectingTriangleCount;
import org.neo4j.graphalgo.triangle.IntersectingTriangleCountFactory;
import org.neo4j.graphalgo.triangle.TriangleCountBaseConfig;
import org.neo4j.logging.NullLog;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Optional;

public class TriangleCountGraphImplBenchmark extends GraphImplBaseBenchmark {

    private TriangleCountBaseConfig config;

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
            Optional.empty(),
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

        config =  ImmutableTriangleCountStatsConfig
            .builder()
            .concurrency(concurrency)
            .build();
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void compute(Blackhole bh) {
        IntersectingTriangleCount algorithm = new IntersectingTriangleCountFactory<>().build(
            graph,
            config,
            AllocationTracker.EMPTY,
            NullLog.getInstance()
        );

        bh.consume(algorithm.compute());
    }

}
