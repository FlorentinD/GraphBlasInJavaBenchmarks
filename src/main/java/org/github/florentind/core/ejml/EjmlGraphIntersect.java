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
import org.neo4j.annotations.service.ServiceProvider;
import org.neo4j.graphalgo.api.Graph;
import org.neo4j.graphalgo.api.RelationshipIntersect;
import org.neo4j.graphalgo.triangle.intersect.GraphIntersect;
import org.neo4j.graphalgo.triangle.intersect.RelationshipIntersectConfig;
import org.neo4j.graphalgo.triangle.intersect.RelationshipIntersectFactory;

public class EjmlGraphIntersect extends GraphIntersect<EjmlAdjacencyCursor> {

    private final DMatrixSparseCSC adjacencyMatrix;

    EjmlGraphIntersect(final DMatrixSparseCSC adjacencyMatrix, long maxDegree) {
        super(
                new EjmlAdjacencyCursor(adjacencyMatrix),
                new EjmlAdjacencyCursor(adjacencyMatrix),
                new EjmlAdjacencyCursor(adjacencyMatrix),
                new EjmlAdjacencyCursor(adjacencyMatrix),
                maxDegree
        );
        this.adjacencyMatrix = adjacencyMatrix;
    }

    @Override
    protected int degree(long node) {
        return adjacencyMatrix.col_idx[(int) node + 1] - adjacencyMatrix.col_idx[(int) node];
    }

    @ServiceProvider
    public static final class EjmlGraphIntersectFactory implements RelationshipIntersectFactory {

        @Override
        public boolean canLoad(Graph graph) {
            return graph instanceof EjmlGraph;
        }

        @Override
        public RelationshipIntersect load(Graph graph, RelationshipIntersectConfig config) {
            var EjmlGraph = (EjmlGraph) graph;
            return new EjmlGraphIntersect(EjmlGraph.matrix(), config.maxDegree());
        }
    }
}
