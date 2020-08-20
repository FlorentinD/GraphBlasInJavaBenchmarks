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

import org.junit.jupiter.api.Test;
import org.neo4j.graphalgo.BaseTest;
import org.neo4j.graphalgo.api.CSRGraph;
import org.neo4j.graphalgo.extension.GdlExtension;
import org.neo4j.graphalgo.extension.GdlGraph;
import org.neo4j.graphalgo.extension.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.neo4j.graphalgo.TestSupport.assertGraphEquals;

@GdlExtension
class EjmlGraphTest extends BaseTest {

    @GdlGraph
    private static final String GRAPH_WITHOUT_PROPS =
        "(e)-->(d)" +
        "(d)-->(c)" +
        "(b)-->(e)" +
        "(a)-->(c)" +
        "(a)-->(b)" +
        "(a)-->(d)" +
        "(a)-->(e)";

    @GdlGraph(graphNamePrefix = "weighted")
    private static final String GRAPH =
        "CREATE " +
        "  (a:Node)" +
        ", (b:Node)" +
        ", (c:Node)" +
        ", (d:Node2)" +
        ", (e:Node2)" +
        ", (e)-[:TYPE {prop:2}]->(d)" +
        ", (d)-[:TYPE {prop:3}]->(c)" +
        ", (b)-[:TYPE {prop:7}]->(e)" +
        ", (a)-[:TYPE {prop:4}]->(c)" +
        ", (a)-[:TYPE {prop:1}]->(b)" +
        ", (a)-[:TYPE {prop:6}]->(d)" +
        ", (a)-[:TYPE {prop:8}]->(e)";

    @GdlGraph(graphNamePrefix = "multi")
    private static final String MULTI_GRAPH = GRAPH + ", (a)-[:TYPE {prop: 5}]->(d)";

    @Inject
    private CSRGraph weightedGraph;

    @Inject
    private CSRGraph graph;

    @Inject
    private CSRGraph multiGraph;

    @Test
    void testUnweighted() {
        EjmlGraph ejmlGraph = EjmlGraph.create(graph);
        assertGraphEquals(graph, ejmlGraph);
    }

    @Test
    void testWeighted() {
        EjmlGraph ejmlGraph = EjmlGraph.create(weightedGraph);
        assertGraphEquals(weightedGraph, ejmlGraph);
    }

    @Test
    void testMultiGraph() {
        IllegalArgumentException exc = assertThrows(IllegalArgumentException.class, () -> EjmlGraph.create(multiGraph));
        assertEquals("Retrieved multiple relationships for 0 -> 3, please use an aggregation", exc.getMessage());
    }
}
