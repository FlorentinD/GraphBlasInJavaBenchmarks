package org.github.florentind.core.ejml;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.neo4j.graphalgo.BaseTest;
import org.neo4j.graphalgo.api.CSRGraph;
import org.neo4j.graphalgo.extension.GdlExtension;
import org.neo4j.graphalgo.extension.GdlGraph;
import org.neo4j.graphalgo.extension.Inject;
import org.neo4j.graphalgo.extension.TestGraph;

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
        adjacencyCursor.init(0, 0);
    }

    @Test
    void remaining() {
        assertEquals(3, adjacencyCursor.remaining());
        adjacencyCursor.nextVLong();
        adjacencyCursor.nextVLong();
        adjacencyCursor.nextVLong();
        assertEquals(0, adjacencyCursor.remaining());
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
