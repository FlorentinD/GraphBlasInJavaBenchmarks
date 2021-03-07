package org.github.florentind.bench.ejmlOps.mask;

import org.apache.commons.lang.NotImplementedException;
import org.github.florentind.bench.ejmlOps.SimpleMatrixOpsBaseBenchmark;

import java.util.ArrayList;
import java.util.List;

public abstract class SimpleMatrixOpsWithMaskBaseBenchmark extends SimpleMatrixOpsBaseBenchmark {
    protected List<Boolean> negatedMask = List.of(true, false);

    protected List<Boolean> structuralMask = List.of(true, false);

    protected void benchmarkFunc(Integer concurrency) {
        throw new NotImplementedException();
    }

    protected abstract void benchmarkFunc(Integer concurrency, Boolean structural, Boolean negated);

    @Override
    public List<BenchmarkResult> run() {
        List<BenchmarkResult> results = new ArrayList<>(datasets().size() * concurrencies().size());

        for (String dataset : datasets()) {
            setup(dataset);

            for (Integer concurrency : concurrencies()) {
                for (Boolean structural : structuralMask) {
                    for (Boolean negated : negatedMask) {
                        System.out.println("Benchmark: " + this.getClass().getSimpleName() + "Mask (structural, negated): " + structural + ", " + negated);

                        for (int i = 0; i < warmUpIterations; i++) {
                            try {
                                beforeEach();
                                benchmarkFunc(concurrency, structural, negated);
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
                                benchmarkFunc(concurrency, structural, negated);
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
                            results.add(new BenchmarkMaskResult(this.getClass().getSimpleName(), concurrency, dataset, negated, structural, median));
                        }

                    }
                }
            }
            tearDown();
        }
        printResults(results);
        return results;
    }

    class BenchmarkMaskResult extends BenchmarkResult {
        Boolean structural;
        Boolean negated;

        public BenchmarkMaskResult(String simpleName, Integer concurrency, String dataset, boolean negated, boolean structural, long median) {
            super(simpleName, concurrency, dataset, median);
            this.structural = structural;
            this.negated = negated;
        }

        @Override
        public String header() {
            return super.header() + ", structural, negated" ;
        }

        @Override
        public String toString() {
            return super.toString() + "," + structural + "," + negated;
        }
    }
}
