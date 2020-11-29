package org.github.florentind.bench.weightedPageRank;


import org.apache.commons.lang3.tuple.Pair;
import org.github.florentind.bench.pageRank.PageRankBaseBenchmark;
import org.neo4j.graphalgo.api.CSRGraph;
import org.neo4j.graphalgo.beta.generator.PropertyProducer;

public class WeightedPageRankBaseBenchmark extends PageRankBaseBenchmark {
    protected static final String REL_PROPERTY_NAME = "weight";

    @Override
    protected CSRGraph getCSRGraph() {
        Pair<Integer, Integer> nodeCountIterationsPair = nodeCountIterationsPairs.get(nodeCountIterationCombinations);
        nodeCount = nodeCountIterationsPair.getLeft();
        maxIterations = nodeCountIterationsPair.getRight();

        return getGraphBuilder()
                .relationshipPropertyProducer(PropertyProducer.random(REL_PROPERTY_NAME, 0, 1.0))
                .build()
                .generate();

    }
}
