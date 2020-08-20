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


import org.florentind.bench.EjmlGraphBaseBenchmark;
import org.neo4j.graphalgo.Orientation;
import org.neo4j.graphalgo.api.CSRGraph;
import org.neo4j.graphalgo.beta.generator.RandomGraphGenerator;
import org.neo4j.graphalgo.beta.generator.RandomGraphGeneratorBuilder;
import org.neo4j.graphalgo.beta.generator.RelationshipDistribution;
import org.neo4j.graphalgo.config.RandomGraphGeneratorConfig;
import org.neo4j.graphalgo.core.Aggregation;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.openjdk.jmh.annotations.Param;

import java.util.Optional;

// TODO: setup own benchmark repo, containing converter from EJML-Graph to JNI-Graph
public class BfsEjmlBaseBenchmark extends EjmlGraphBaseBenchmark {
    @Param({"3000000"})
    int nodeCount;

    @Param({"4"})
    int avgDegree;

    @Param({"0"})
    int startNode;

    // TODO: retrieve actually run iterations
    @Param({"100"})
    int maxIterations;


    @Override
    protected CSRGraph getCSRGraph() {
        return new RandomGraphGenerator(
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
        ).generate();
    }

    // TODO add MSBFS benchmark

}
