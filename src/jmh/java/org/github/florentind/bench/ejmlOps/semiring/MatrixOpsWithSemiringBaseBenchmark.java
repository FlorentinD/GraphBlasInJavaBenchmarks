
package org.github.florentind.bench.ejmlOps.semiring;

import org.apache.commons.lang.NotImplementedException;
import org.github.florentind.bench.ejmlOps.SimpleMatrixOpsBaseBenchmark;

import java.util.ArrayList;
import java.util.List;

public abstract class MatrixOpsWithSemiringBaseBenchmark extends SimpleMatrixOpsBaseBenchmark {
    protected static final String PLUS_TIMES = "(PLUS;TIMES)";
    protected static final String OR_AND = "(OR;AND)";
    protected static final String PLUS_AND = "(PLUS;AND)";
    protected static final String OR_TIMES = "(OR;TIMES)";
    protected static final String PLUS_FIRST = "(PLUS;FIRST)";
    protected static final String PLUS_BFIRST = "(PLUS;BOOLEAN-FIRST)";
    protected static final String OR_PAIR = "(OR;Pair)";
    protected static final String MIN_MAX = "(Min;Max)";
    protected static final String NONE = "(PLUS;TIMES) (inlined)";

    protected List<String> semiRings() {
        return List.of(NONE, PLUS_TIMES, OR_PAIR, OR_AND, MIN_MAX);
    }

    protected void benchmarkFunc(Integer concurrency) {
        throw new NotImplementedException();
    }

    protected abstract void benchmarkFunc(Integer concurrency, String semiring);

    @Override
    public List<BenchmarkResult> run() {
        List<BenchmarkResult> results = new ArrayList<>(datasets().size() * concurrencies().size());

        for (String dataset : datasets()) {
            setup(dataset);

            for (Integer concurrency : concurrencies()) {
                for (String semiRing : semiRings()) {
                    System.out.println("Benchmark: " + this.getClass().getSimpleName() + "semiring " + semiRing);
                    for (int i = 0; i < warmUpIterations; i++) {
                        try {
                            beforeEach();
                            benchmarkFunc(concurrency, semiRing);
                            afterEach();
                        } catch (Throwable e) {
                            e.printStackTrace();
                            break;
                        }
                        System.out.printf("warmup: %d/%d%n", i + 1, warmUpIterations);
                    }

                    List<Long> timings = new ArrayList<>(iterations);

                    for (int i = 0; i < iterations; i++) {
                        beforeEach();
                        var start = System.nanoTime();
                        try {
                            benchmarkFunc(concurrency, semiRing);
                            var end = System.nanoTime();
                            long duration = Math.round((end - start) / 1_000_000.0);
                            System.out.println("Iteration: " + i + ", time: " + duration + "ms");
                            timings.add(duration);
                            afterEach();
                        } catch (Throwable e) {
                            e.printStackTrace();
                            break;
                        }
                    }

                    if (timings.size() > 0) {
                        timings.sort(Long::compare);
                        long median = timings.get(Math.round(timings.size() / 2.0f));
                        System.out.println("median: " + median);
                        System.out.println("stats: " + timings.stream().mapToLong(Long::longValue).summaryStatistics().toString());
                        results.add(new BenchmarkSemiringResult(this.getClass().getSimpleName(), concurrency, dataset, semiRing, median));
                    }

                }
            }
            tearDown();
        }
        printResults(results);
        return results;
    }

    class BenchmarkSemiringResult extends BenchmarkResult {
        String semiring;

        public BenchmarkSemiringResult(String simpleName, Integer concurrency, String dataset, String semiRing, long median) {
            super(simpleName, concurrency, dataset, median);
            this.semiring = semiRing;
        }

        @Override
        public String header() {
            return super.header() + ", semiring";
        }

        @Override
        public String toString() {
            return super.toString() + "," + semiring;
        }
    }

}
