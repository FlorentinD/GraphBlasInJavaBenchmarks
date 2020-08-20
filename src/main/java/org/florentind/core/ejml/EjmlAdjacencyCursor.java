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
package org.florentind.core.ejml;

import org.ejml.data.DMatrixSparseCSC;
import org.neo4j.graphalgo.api.AdjacencyCursor;

public class EjmlAdjacencyCursor implements AdjacencyCursor {

    private final DMatrixSparseCSC adjacencyMatrix;
    private int sourceNodeId;
    private int currentOffset;
    private int maxOffset;

    public EjmlAdjacencyCursor(DMatrixSparseCSC adjacencyMatrix) {
        this.adjacencyMatrix = adjacencyMatrix;
    }

    static EjmlAdjacencyCursor cursor(EjmlAdjacencyCursor reuse, long node) {
        return reuse.init(node);
    }

    public EjmlAdjacencyCursor init(long node) {
        int nodeId = (int) node;
        sourceNodeId = nodeId;
        currentOffset = adjacencyMatrix.col_idx[nodeId];
        // as exclusive range
        maxOffset = adjacencyMatrix.col_idx[nodeId + 1] - 1;
        return this;
    }


    @Override
    public int size() {
        return maxOffset - adjacencyMatrix.col_idx[sourceNodeId];
    }

    @Override
    public boolean hasNextVLong() {
        return currentOffset < maxOffset;
    }

    @Override
    public long nextVLong() {
        currentOffset++;
        return adjacencyMatrix.nz_rows[currentOffset];
    }

    @Override
    public long peekVLong() {
        return adjacencyMatrix.nz_rows[currentOffset];
    }

    @Override
    public int remaining() {
        return maxOffset - currentOffset;
    }

    @Override
    public void close() {
    }

    /**
     * Read and decode target ids until it is strictly larger than ({@literal >}) the provided {@code target}.
     * Return -1 iff the cursor did exhaust before finding an
     * id that is large enough.
     * {@code skipUntil(target) <= target} can be used to distinguish the no-more-ids case and afterwards {@link #hasNextVLong()}
     * will return {@code false}
     */
    long skipUntil(long nodeId) {
        int targetId = -1;
        for (; currentOffset <= maxOffset; currentOffset++) {
            targetId = adjacencyMatrix.nz_rows[currentOffset];
            if (targetId > nodeId) {
                return targetId;
            }
        }

        return targetId;
    }

    /**
     * Copy iteration state from another cursor without changing {@code other}.
     */
    void copyFrom(EjmlAdjacencyCursor sourceCursor) {
        sourceNodeId = sourceCursor.sourceNodeId;
        currentOffset = sourceCursor.currentOffset;
        maxOffset = sourceCursor.maxOffset;
    }

    long advance(long nodeId) {
        int targetId = -1;
        for (; currentOffset <= maxOffset; currentOffset++) {
            targetId = adjacencyMatrix.nz_rows[currentOffset];
            if (targetId >= nodeId) {
                return targetId;
            }
        }

        return targetId;
    }
}
