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
import org.ejml.data.DMatrixSparseTriplet;
import org.ejml.ops.ConvertDMatrixStruct;
import org.neo4j.graphalgo.annotation.ValueClass;
import org.neo4j.graphalgo.api.CSRGraph;
import org.neo4j.graphalgo.api.Relationships;

import java.util.HashSet;
import java.util.Set;

import static org.neo4j.graphalgo.utils.StringFormatting.formatWithLocale;

@ValueClass
public interface EjmlRelationships {

    // Needs to be 1 to indicate a relationship between two nodes in the matrix.
    double DEFAULT_RELATIONSHIP_PROPERTY = 1D;

    // Basically needs no values stored, as an entry in the structure could be seen as implicitly true/1
    DMatrixSparseCSC transposedMatrix();

    /**
     * Fills an EJML matrix based on the graph topology
     * and potential relationship property.
     * If no property exists, the value will be 1.
     */
    static EjmlRelationships of(CSRGraph graph) {
        Relationships.Topology relationships = graph.relationshipTopology();

        int nodeCount = Math.toIntExact(graph.nodeCount());
        DMatrixSparseTriplet tripleStore = new DMatrixSparseTriplet(
            nodeCount,
            nodeCount,
            Math.toIntExact(relationships.elementCount())
        );

        graph.forEachNode((id) -> {
            Set<Integer> targetIds = new HashSet<>();

            graph.forEachRelationship(id, DEFAULT_RELATIONSHIP_PROPERTY, (src, trg, weight) -> {
                int srcIntId = Math.toIntExact(src);
                int trgIntId = Math.toIntExact(trg);

                if (targetIds.add(trgIntId)) {
                    // saving as reversed as accessing the CSC format is made for accessing column wise (normally incoming rels)
                    tripleStore.addItem(trgIntId, srcIntId, weight);
                } else {
                    throw new IllegalArgumentException(
                        formatWithLocale(
                            "Retrieved multiple relationships for %d -> %d, please use an aggregation",
                            srcIntId,
                            trgIntId
                        ));
                }
                return true;
            });
            return true;
        });

        DMatrixSparseCSC transposedMatrix = ConvertDMatrixStruct.convert(tripleStore, (DMatrixSparseCSC) null);
        // based on the sorted insertion into the tripleStore, we know it is sorted
        transposedMatrix.indicesSorted = true;

        return ImmutableEjmlRelationships.of(transposedMatrix);
    }
}
