package org.github.florentind.bench.pageRank;


import org.github.florentind.bench.SimpleEjmlGraphBaseBenchmark;

public abstract class PageRankBaseBenchmark extends SimpleEjmlGraphBaseBenchmark {

    protected int maxIterations = 20;

    protected float dampingFactor = 0.85f;

    protected float tolerance = 1e-32f;

}
