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
package org.florentind.bench;


import org.ejml.data.DMatrixSparseCSC;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.florentind.core.ejml.EjmlGraph;
import org.neo4j.graphalgo.StoreLoaderBuilder;
import org.neo4j.graphalgo.api.CSRGraph;
import org.neo4j.graphalgo.compat.GdsGraphDatabaseAPI;
import org.neo4j.graphalgo.core.Aggregation;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

/**
 * Benchmarks only based on EJML-Graphs
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
@State(Scope.Benchmark)
@Fork(value = 1, warmups = 2)
public class EjmlGraphBaseBenchmark {
    GdsGraphDatabaseAPI db;

    protected EjmlGraph graph;
    // untransposed version
    protected DMatrixSparseCSC matrix;

//    DatasetManager datasetManager;

//    @Param({"empty"})
//    String dataset;


    protected CSRGraph getCSRGraph() {
        return (CSRGraph) new StoreLoaderBuilder()
            .api(db)
            .globalAggregation(Aggregation.SINGLE)
            .build()
            .graphStore()
            .getUnion();
    }

    @Setup(Level.Invocation)
    public void setup() {
//        datasetManager = new DatasetManager(Path.of("/tmp"));

//        db = datasetManager.openDb(dataset);
        var hugeGraph = getCSRGraph();

        graph = EjmlGraph.create(hugeGraph);
        matrix = CommonOps_DSCC.transpose(graph.matrix(), null, null);

        hugeGraph.release();
    }

    @TearDown
    public void tearDown() {
//        datasetManager.closeDb(db);
        graph.release();
    }
}
