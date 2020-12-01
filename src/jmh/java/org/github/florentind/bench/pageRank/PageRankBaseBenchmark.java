package org.github.florentind.bench.pageRank;


import org.apache.commons.lang3.tuple.Pair;
import org.github.florentind.bench.EjmlGraphBaseBenchmark;
import org.neo4j.graphalgo.Orientation;
import org.neo4j.graphalgo.api.CSRGraph;
import org.neo4j.graphalgo.beta.generator.RandomGraphGenerator;
import org.neo4j.graphalgo.beta.generator.RandomGraphGeneratorBuilder;
import org.neo4j.graphalgo.beta.generator.RelationshipDistribution;
import org.neo4j.graphalgo.config.RandomGraphGeneratorConfig;
import org.neo4j.graphalgo.core.Aggregation;
import org.neo4j.graphalgo.core.utils.mem.AllocationTracker;
import org.openjdk.jmh.annotations.Param;

import java.util.HashMap;

public class PageRankBaseBenchmark extends EjmlGraphBaseBenchmark {
    protected static HashMap<String, Pair<Integer, Integer>> nodeCountIterationsPairs = new HashMap<>() {{
        put("0.1M;20", Pair.of(100_000, 20));
        put("0.5M;20", Pair.of(500_000, 20));
        put("1M;20", Pair.of(1_000_000, 20));
        put("5M;20", Pair.of(5_000_000, 20));
        put("1M;5", Pair.of(1_000_000, 5));
        put("1M;10", Pair.of(1_000_000, 10));
        put("1M;15", Pair.of(1_000_000, 15));
    }};

    @Param({"0.1M;20", "0.5M;20", "1M;20", "5M;20", "1M;5", "1M;10", "1M;15"})
    protected String nodeCountIterationCombinations;

    protected int nodeCount;

    @Param({"4"})
    protected int avgDegree;

    protected int maxIterations;

    @Param({"0.85"})
    protected float dampingFactor;

    @Param({"Undirected"})
    protected String orientation;

    @Param({"1e-32"})
    protected float tolerance;

    protected RandomGraphGeneratorBuilder getGraphBuilder() {
        return RandomGraphGenerator.builder()
                .nodeCount(nodeCount)
                .averageDegree(avgDegree)
                .seed(42L)
                .aggregation(Aggregation.MAX)
                .orientation(Orientation.of(orientation))
                .allocationTracker(AllocationTracker.empty())
                .allowSelfLoops(RandomGraphGeneratorConfig.AllowSelfLoops.NO)
                .relationshipDistribution(RelationshipDistribution.POWER_LAW);
    }

    @Override
    protected CSRGraph getCSRGraph() {
        Pair<Integer, Integer> nodeCountIterationsPair = nodeCountIterationsPairs.get(nodeCountIterationCombinations);
        nodeCount = nodeCountIterationsPair.getLeft();
        maxIterations = nodeCountIterationsPair.getRight();

        return getGraphBuilder().build().generate();
    }
}
