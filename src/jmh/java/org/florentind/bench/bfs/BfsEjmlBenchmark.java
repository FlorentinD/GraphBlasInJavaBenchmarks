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


import org.ejml.sparse.csc.graphAlgos.BFS_DSCC;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.infra.Blackhole;

// TODO: setup own benchmark repo, containing converter from EJML-Graph to JNI-Graph
public class BfsEjmlBenchmark extends BfsEjmlBaseBenchmark {

    @Param({"1"})
    private int concurrency;


// FIXME enable when bfs bug is fixed

//    @org.openjdk.jmh.annotations.Benchmark
//    public void gdsBfsTraverse(Blackhole bh) {
//        // Not really level/parent but just returns a list of visited nodes
//        Traverse algorithm = Traverse.bfs(graph, startNode, (s, t, w) -> Traverse.ExitPredicate.Result.FOLLOW, Traverse.DEFAULT_AGGREGATOR);
//
//        bh.consume(algorithm.compute());
//    }

    @org.openjdk.jmh.annotations.Benchmark
    public void ejmlSparseBfsBoolean(Blackhole bh) {
        int[] startNodes = {startNode};
        bh.consume(BFS_DSCC.computeSparse(matrix, BFS_DSCC.BfsVariation.BOOLEAN, startNodes, maxIterations));
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void ejmlSparseBfsLevel(Blackhole bh) {
        int[] startNodes = {startNode};
        bh.consume(BFS_DSCC.computeSparse(matrix, BFS_DSCC.BfsVariation.LEVEL, startNodes, maxIterations));
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void ejmlSparseBfsParent(Blackhole bh) {
        int[] startNodes = {startNode};
        bh.consume(BFS_DSCC.computeSparse(matrix, BFS_DSCC.BfsVariation.PARENTS, startNodes, maxIterations));
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void ejmlDenseBfsBoolean(Blackhole bh) {
        bh.consume(BFS_DSCC.computeDense(matrix, BFS_DSCC.BfsVariation.BOOLEAN, startNode, maxIterations));
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void ejmlDenseBfsLevel(Blackhole bh) {
        bh.consume(BFS_DSCC.computeDense(matrix, BFS_DSCC.BfsVariation.LEVEL, startNode, maxIterations));
    }

    @org.openjdk.jmh.annotations.Benchmark
    public void ejmlDenseBfsParent(Blackhole bh) {
        bh.consume(BFS_DSCC.computeDense(matrix, BFS_DSCC.BfsVariation.PARENTS, startNode, maxIterations));
    }

    // TODO add MSBFS benchmark
}
