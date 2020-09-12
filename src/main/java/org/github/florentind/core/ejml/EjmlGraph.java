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
package org.github.florentind.core.ejml;

import org.ejml.data.DMatrixSparseCSC;
import org.neo4j.graphalgo.api.*;

import java.util.stream.Stream;

/**
 * Graph implementation backed by a `DMatrixSparseCSC` from EJML (https://github.com/lessthanoptimal/ejml).
 * Its indexed by outgoing relationships by saving the transposed adjacency matrix.
 * The graph topology corresponds to the structure of the adjacency matrix.
 * Relationship properties are saved as the entries of the adjacency matrix, otherwise the default value is `1`.
 *
 * Noteable restrictions:
 *      - does not support graphs with more than 2<sup>31</sup>-1 nodes.
 *      - does not support multi-graphs (as this would correspond to multiple values for one matrix entry)
 */
public final class EjmlGraph extends FilterGraph {

    private DMatrixSparseCSC transposedMatrix;

    private boolean canRelease = true;

    /**
     * Converts a Graph into an EJML graph.
     * ! Releases its topology
     */
    public static EjmlGraph create(CSRGraph graph) {
        EjmlRelationships ejmlRelationships = EjmlRelationships.of(graph);
        graph.releaseTopology();
        return new EjmlGraph(graph, ejmlRelationships);
    }

    private EjmlGraph(Graph graph, EjmlRelationships ejmlRelationships) {
        super(graph);
        this.transposedMatrix = ejmlRelationships.transposedMatrix();
    }

    @Override
    public long relationshipCount() {
        return transposedMatrix.getNumElements();
    }

    @Override
    public double relationshipProperty(long sourceNodeId, long targetNodeId) {
        return transposedMatrix.get(Math.toIntExact(targetNodeId), Math.toIntExact(sourceNodeId));
    }

    @Override
    public double relationshipProperty(long sourceId, long targetId, double fallbackValue) {
        int row = Math.toIntExact(targetId);
        int col = Math.toIntExact(sourceId);
        return transposedMatrix.get(row, col, fallbackValue);
    }

    @Override
    public Stream<RelationshipCursor> streamRelationships(long nodeId, double fallbackValue) {
        throw new UnsupportedOperationException(
            "org.neo4j.gds.core.decompressedEJML.EjmlGraph.streamRelationships"
        );
    }

    @Override
    public void forEachRelationship(long nodeId, RelationshipConsumer consumer) {
        runForEach(nodeId, consumer);
    }

    @Override
    public void forEachRelationship(long nodeId, double fallbackValue, RelationshipWithPropertyConsumer consumer) {
        runForEach(nodeId, fallbackValue, consumer);
    }

    @Override
    public int degree(long node) {
        int nodeId = Math.toIntExact(node);
        if (node >= transposedMatrix.numCols) {
            return 0;
        }

        return transposedMatrix.col_idx[nodeId + 1] - transposedMatrix.col_idx[nodeId];
    }

    @Override
    public int degreeWithoutParallelRelationships(long nodeId) {
        return 0;
    }

    @Override
    public EjmlGraph concurrentCopy() {
        // as the EjmlGraph does not cache any cursors
        return this;
    }

    @Override
    public RelationshipIntersect intersection(long maxDegree) {
        return new EjmlGraphIntersect(transposedMatrix, maxDegree);
    }

    // TODO: transfer this into the gds repo?
    @Override
    public RelationshipIntersect intersection() {
        return this.intersection(9223372036854775807L);
    }

    public DMatrixSparseCSC matrix() {
        return transposedMatrix;
    }

    @Override
    public boolean exists(long sourceNodeId, long targetNodeId) {
        return transposedMatrix.isAssigned(Math.toIntExact(sourceNodeId), Math.toIntExact(targetNodeId));
    }


    @Override
    public long getTarget(long sourceNodeId, long index) {
        int sourceNode = Math.toIntExact(sourceNodeId);

        if (sourceNode < transposedMatrix.numCols) {
            int startIndex = transposedMatrix.col_idx[sourceNode];
            int endIndex = transposedMatrix.col_idx[sourceNode + 1];
            if (index < startIndex - endIndex) {
                return transposedMatrix.nz_rows[startIndex + Math.toIntExact(index)];
            }
        }

        return -1;
    }

    private void runForEach(long sourceId, RelationshipConsumer consumer) {
        int sourceNodeId = Math.toIntExact(sourceId);
        int startIndex = transposedMatrix.col_idx[sourceNodeId];
        int endIndex = transposedMatrix.col_idx[sourceNodeId + 1];
        for (int index = startIndex; index < endIndex; index++) {
            consumer.accept(sourceId, transposedMatrix.nz_rows[index]);
        }
    }

    private void runForEach(long sourceId, double fallbackValue, RelationshipWithPropertyConsumer consumer) {
        if (!hasRelationshipProperty()) {
            runForEach(sourceId, (s, t) -> consumer.accept(s, t, fallbackValue));
        } else {
            int sourceNodeId = Math.toIntExact(sourceId);
            int startIndex = transposedMatrix.col_idx[sourceNodeId];
            int endIndex = transposedMatrix.col_idx[sourceNodeId + 1];
            for (int index = startIndex; index < endIndex; index++) {
                consumer.accept(sourceId, transposedMatrix.nz_rows[index],
                    transposedMatrix.nz_values[index]
                );
            }
        }
    }

    @Override
    public void canRelease(boolean canRelease) {
        this.canRelease = canRelease;
    }

    @Override
    public void releaseTopology() {
        if (!canRelease) return;

        if (transposedMatrix != null) {
            // todo: track allocated memory?
            transposedMatrix.col_idx = null;
            transposedMatrix.nz_rows = null;
            transposedMatrix.nz_values = null;
            transposedMatrix = null;
        }
    }

    @Override
    public boolean isMultiGraph() {
        return false;
    }
}
