package org.github.florentind.bench.weightedPageRank;


import org.apache.commons.lang3.tuple.Pair;
import org.github.florentind.bench.pageRank.PageRankBaseBenchmark;
import org.neo4j.graphalgo.api.CSRGraph;
import org.neo4j.graphalgo.beta.generator.PropertyProducer;

public class WeightedPageRankBaseBenchmark extends PageRankBaseBenchmark {
    protected static final String REL_PROPERTY_NAME = "weight";
}
