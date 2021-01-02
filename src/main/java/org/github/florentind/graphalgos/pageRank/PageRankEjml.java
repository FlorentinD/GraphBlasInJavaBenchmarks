package org.github.florentind.graphalgos.pageRank;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.ops.CommonOps_DArray;
import org.ejml.ops.DMonoids;
import org.ejml.ops.DSemiRings;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.mult.MatrixVectorMultWithSemiRing_DSCC;

import java.util.Arrays;

/**
 * PageRank implementations similar to GDS version
 */
public class PageRankEjml {
    public static final int DEFAULT_MAX_ITERATIONS = 20;
    public static final double DEFAULT_DAMPING_FACTOR = 0.85;
    public static final float DEFAULT_TOLERANCE = 1e-7f;

    /**
     * simple pagerank as in gds ( PR(a) = (1-d) / N + d * (sum_incoming_neighbors(neighbor-score/neighbor-degree))
     *
     * @param adjacencyMatrix (Input) Graph
     * @param dampingFactor   How often are teleports
     * @param tolerance       Minimum change in scores between iterations
     * @param maxIterations   Maximum number of iterations
     * @return pr-scores (sum of all scores = 1)
     */
    public PageRankResult compute(DMatrixSparseCSC adjacencyMatrix, double dampingFactor, double tolerance, int maxIterations) {
        int nodeCount = adjacencyMatrix.getNumCols();
        final double teleport = (1.0 - dampingFactor) / nodeCount;
        // so first iteration is always run
        double resultDiff = 1;
        int iterations = 0;

        // Calculating outbound degrees of all nodes
        double[] outDegrees = CommonOps_DSCC.reduceRowWise(adjacencyMatrix, 0.0, (acc, b) -> acc + 1, null).data;

        // init result vector
        double[] pr = new double[nodeCount];
        double[] prevPr = new double[nodeCount];
        Arrays.fill(pr, 1.0 / nodeCount);

        double[] importanceVec = new double[nodeCount];

        double tmp[];

        //iterations
        for (; iterations < maxIterations && resultDiff > tolerance; iterations++) {
            // Swap prevPr and result
            tmp = pr;
            pr = prevPr;
            prevPr = tmp;

            //
            // Importance calculation
            //

            // Divide previous PageRank with number of outbound edges
            // Reason: outDegrees vector would be sparse in other GraphBLAS implementations (here always dense)
            // ! this should only be done if outDegree != 0?
            //  (otherwise division through 0, but result is ignored in next mult-op either way) (assume mask overhead is higher than gain)
            CommonOps_DArray.elementWiseMult(prevPr, outDegrees, pr, (a, b) -> a / b);

            // Calculate total PR of all inbound nodes
            //  --> importanceResultVec (instead of allocating a new result array per iteration)
            // !! using FIRST instead of TIMES as unweighted -> value is always 1 and 1 * x = x
            importanceVec = MatrixVectorMultWithSemiRing_DSCC.mult(
                    pr,
                    adjacencyMatrix,
                    importanceVec,
                    DSemiRings.PLUS_FIRST,
                    null,
                    null,
                    true
            );

            // to allow following apply in place
            tmp = importanceVec;
            importanceVec = pr;
            pr = tmp;

            //
            // PageRank summarization
            CommonOps_DArray.apply(pr, score -> score * dampingFactor + teleport);

            // calculate diff (for tolerance check)
            CommonOps_DArray.elementWiseMult(prevPr, pr, prevPr, (a,b) -> a - b);
            CommonOps_DArray.apply(prevPr, Math::abs);
            resultDiff = CommonOps_DArray.reduceScalar(prevPr,0, DMonoids.PLUS.func);
        }

        return new PageRankResult(pr, iterations);
    }

    /**
     * simple pagerank as in gds ( PR(a) = (1-d) / N + d * (sum_incoming_neighbors(neighbor-score/neighbor-degree))
     *
     * Propageted score to neighbors is divided by the sum of the weights instead of the degree
     *
     * @param adjacencyMatrix (Input) Graph
     * @param dampingFactor   How often are teleports
     * @param tolerance       Minimum change in scores between iterations
     * @param maxIterations   Maximum number of iterations
     * @return pr-scores (sum of all scores = 1)
     */
    public PageRankResult computeWeighted(DMatrixSparseCSC adjacencyMatrix, double dampingFactor, double tolerance, int maxIterations) {
        // Differences: normalize weights on copy adjacency matrix stuff -> no division by outDegree needed at beginning of iteration

        int nodeCount = adjacencyMatrix.getNumCols();
        final double teleport = (1.0 - dampingFactor) / nodeCount;
        // so first iteration is always run
        double resultDiff = 1;
        int iterations = 0;

        // Calculating sum of outgoing edge weights for each vertex
        double[] weightSums = CommonOps_DSCC.reduceRowWise(adjacencyMatrix, 0.0, Double::sum, null).data;

        // similar to native version, could refactor this into a helper method
        boolean weightsNormalized = Arrays.stream(weightSums).allMatch(sum -> sum == 1 || sum == 0);

        if (!weightsNormalized) {
            // create copy to not change input matrix
            adjacencyMatrix = adjacencyMatrix.copy();
            // normalize weights .. op based on row + values
            CommonOps_DSCC.applyRowIdx(adjacencyMatrix, (rowIdx, val) -> val / weightSums[rowIdx], adjacencyMatrix);
        }


        // init result vector
        double[] pr = new double[nodeCount];
        double[] prevPr = new double[nodeCount];
        Arrays.fill(pr, 1.0 / nodeCount);

        double[] tmp;

        double[] importanceVec = new double[nodeCount];

        //iterations
        for (; iterations < maxIterations && resultDiff > tolerance; iterations++) {
            // Swap prevPr and result
            tmp = pr;
            pr = prevPr;
            prevPr = tmp;

            //
            // Importance calculation
            //

            // Calculate total PR of all inbound nodes
            // !! Difference to reference: input vector must be different to initial output vector (otherwise dirty reads) for `mult`
            //  --> importanceResultVec (instead of allocating a new result array per iteration)
            // PLUS_TIMES as now entries are relevant
            importanceVec = MatrixVectorMultWithSemiRing_DSCC.mult(
                    prevPr,
                    adjacencyMatrix,
                    importanceVec,
                    DSemiRings.PLUS_TIMES,
                    null,
                    null,
                    true
            );

            // to allow following apply in place
            tmp = importanceVec;
            importanceVec = pr;
            pr = tmp;

            //
            // PageRank summarization
            CommonOps_DArray.apply(pr, score -> score * dampingFactor + teleport);


            // !! Difference to reference: no tolerance contained
            // calculate diff (for tolerance check)
            CommonOps_DArray.elementWiseMult(prevPr, pr, prevPr, (a,b) -> a - b);
            CommonOps_DArray.apply(prevPr, Math::abs);
            resultDiff = CommonOps_DArray.reduceScalar(prevPr, 0, DMonoids.PLUS.func);
        }

        return new PageRankResult(pr, iterations);
    }
}
