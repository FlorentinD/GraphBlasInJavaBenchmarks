package org.github.florentind.core.jgrapht;

import org.junit.jupiter.api.Test;
import org.neo4j.graphalgo.Orientation;
import org.neo4j.graphalgo.api.Graph;

import static org.junit.jupiter.api.Assertions.*;
import static org.neo4j.graphalgo.TestSupport.fromGdl;

public class ConverterTest {

    @Test
    public void convert() {
        Graph graph = fromGdl("(a)-->(b), (b)-->(c)");

        org.jgrapht.Graph jGraph = JGraphTConverter.convert(graph);

        assertFalse(jGraph.getType().isWeighted());
        assertFalse(jGraph.getType().isUndirected());
        assertEquals(graph.relationshipCount(), jGraph.edgeSet().size());

        assertTrue(jGraph.containsEdge(0, 1));
        assertTrue(jGraph.containsEdge(1, 2));
    }

    @Test
    public void convertWeighted() {
        Graph graph = fromGdl("(a)-[{weight: 42}]->(b), (b)-[{weight: 1337}]->(c)");

        org.jgrapht.Graph jGraph = JGraphTConverter.convert(graph);

        assertTrue(jGraph.getType().isWeighted());
        assertFalse(jGraph.getType().isUndirected());
        assertEquals(graph.relationshipCount(), jGraph.edgeSet().size());

        assertEquals(42, jGraph.getEdgeWeight(jGraph.getEdge(0, 1)));
        assertEquals(1337, jGraph.getEdgeWeight(jGraph.getEdge(1, 2)));
    }

    @Test
    public void convertUndirected() {
        Graph graph = fromGdl("(a)-[{weight: 42}]->(b), (b)-[{weight: 1337}]->(c)", Orientation.UNDIRECTED);

        org.jgrapht.Graph jGraph = JGraphTConverter.convert(graph);

        assertTrue(jGraph.getType().isWeighted());
        assertTrue(jGraph.getType().isUndirected());
        assertEquals(graph.relationshipCount(), jGraph.edgeSet().size());

        assertEquals(42, jGraph.getEdgeWeight(jGraph.getEdge(0, 1)));
        assertEquals(1337, jGraph.getEdgeWeight(jGraph.getEdge(1, 2)));
    }
}
