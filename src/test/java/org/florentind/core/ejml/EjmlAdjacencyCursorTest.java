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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.graphalgo.BaseTest;
import org.neo4j.graphalgo.api.CSRGraph;
import org.neo4j.graphalgo.extension.GdlExtension;
import org.neo4j.graphalgo.extension.GdlGraph;
import org.neo4j.graphalgo.extension.Inject;

import static org.junit.jupiter.api.Assertions.*;

@GdlExtension
class EjmlAdjacencyCursorTest extends BaseTest {

    @GdlGraph
    private static final String DB_CYPHER =
        "(a)-->(b)" +
        "(a)-->(c)" +
        "(a)-->(a)";

    @Inject
    private CSRGraph graph;

    private EjmlAdjacencyCursor adjacencyCursor;

    @BeforeEach
    void setup() {
        EjmlGraph ejmlGraph = EjmlGraph.create(graph);
        adjacencyCursor = new EjmlAdjacencyCursor(ejmlGraph.matrix());
        adjacencyCursor.init(0);
    }

    @Test
    void shouldIterateInOrder() {
        long lastNodeId = 0;
        while (adjacencyCursor.hasNextVLong()) {
            assertTrue(lastNodeId <= adjacencyCursor.nextVLong());
        }
    }

    @Test
    void shouldSkipUntilLargerValue() {
        assertEquals(2, adjacencyCursor.skipUntil(1));
        assertFalse(adjacencyCursor.hasNextVLong());
    }

    @Test
    void shouldAdvanceUntilEqualValue() {
        assertEquals(1, adjacencyCursor.advance(1));
        assertEquals(2, adjacencyCursor.nextVLong());
        assertFalse(adjacencyCursor.hasNextVLong());
    }

    @Test
    void shouldNotReturnLastValueWhenAdvanceExhaustsCursor() {
        assertEquals(2, adjacencyCursor.advance(2));
    }
}
