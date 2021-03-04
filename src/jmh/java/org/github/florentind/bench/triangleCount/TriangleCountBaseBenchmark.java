package org.github.florentind.bench.triangleCount;

import org.github.florentind.bench.SimpleEjmlGraphBaseBenchmark;
import org.neo4j.graphalgo.Orientation;
import org.neo4j.graphalgo.StoreLoaderBuilder;
import org.neo4j.graphalgo.api.CSRGraph;
import org.neo4j.graphalgo.core.Aggregation;

public abstract class TriangleCountBaseBenchmark extends SimpleEjmlGraphBaseBenchmark {
    // !! undirected
    @Override
    protected CSRGraph getCSRGraph() {
        return (CSRGraph) new StoreLoaderBuilder()
                .api(db)
                .globalOrientation(Orientation.UNDIRECTED)
                .globalAggregation(Aggregation.SINGLE)
                .build()
                .graphStore()
                .getUnion();
    }
}
