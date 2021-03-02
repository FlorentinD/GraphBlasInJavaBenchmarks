package org.github.florentind.bench.pageRank;


import org.github.florentind.bench.EjmlGraphBaseBenchmark;
import org.openjdk.jmh.annotations.Param;

public class PageRankBaseBenchmark extends EjmlGraphBaseBenchmark {

    @Param({"20"})
    protected int maxIterations;

    @Param({"0.85"})
    protected float dampingFactor;

    @Param({"1e-32"})
    protected float tolerance;
}
