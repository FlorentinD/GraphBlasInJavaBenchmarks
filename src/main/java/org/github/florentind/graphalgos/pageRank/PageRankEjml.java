package org.github.florentind.graphalgos.pageRank;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.masks.DMasks;
import org.ejml.masks.PrimitiveDMask;
import org.ejml.ops.CommonOps_DArray;
import org.ejml.ops.DMonoids;
import org.ejml.ops.DSemiRings;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.mult.MatrixVectorMultWithSemiRing_DSCC;

import java.util.Arrays;

public class PageRankEjml {
    public static final int DEFAULT_MAX_ITERATIONS = 20;
    public static final double DEFAULT_DAMPING_FACTOR = 0.85;
    public static final float DEFAULT_TOLERANCE = 1e-7f;

    /**
     * based on https://github.com/GraphBLAS/LAGraph/blob/master/Source/Algorithm/LAGraph_pagerank2.c
     * and therefore LDBC-Graphalytics conform
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

        // Mask set for every node with a nodeDegree > 0
        // In subsequent operations, this mask can be used to select dangling nodes.
        // Difference to reference: not creating a boolean vector for non-dangling nodes and inverting at this point already
        // as here we have explicit mask objects
        PrimitiveDMask danglingNodesMask = DMasks.builder(outDegrees)
                .withZeroElement(0)
                .withReplace(true)
                .withNegated(true)
                .build();


        // init result vector
        double[] pr = new double[nodeCount];
        double[] prevResult = new double[nodeCount];
        Arrays.fill(pr, 1.0 / nodeCount);

        double[] importanceVec = new double[nodeCount];
        double[] importanceResultVec = new double[nodeCount];

        //iterations
        for (; iterations < maxIterations && resultDiff > tolerance; iterations++) {
            // cache result from previous iteration
            System.arraycopy(pr, 0, prevResult, 0, pr.length);

            //
            // Importance calculation
            //

            // Divide previous PageRank with number of outbound edges
            // Difference to reference: need to use nonDanglingNodesMask here to avoid division by 0
            // Reason: outDegrees vector would be sparse in other GraphBLAS implementations (here always dense)
            // TODO: this should only be done if outDegree != 0?
            //  (otherwise division through 0, but result is ignored in next mult-op either way) (see Performance impact first)
            importanceVec = CommonOps_DArray.elementWiseMult(pr, outDegrees, importanceVec, (a, b) -> a / b);

            // Multiply importance by damping factor
            CommonOps_DArray.apply(importanceVec, i -> i * dampingFactor);

            // Calculate total PR of all inbound nodes
            // !! Difference to reference: input vector must be different to initial output vector (otherwise dirty reads) for `multTransA`
            //  --> importanceResultVec (instead of allocating a new result array per iteration)
            importanceResultVec = MatrixVectorMultWithSemiRing_DSCC.multTransA(
                    adjacencyMatrix,
                    importanceVec,
                    importanceResultVec,
                    DSemiRings.PLUS_TIMES,
                    null,
                    null
            );


            //
            // Dangling calculation
            //

            // Sum the previous PR values of dangling nodes together
            // !! Difference to reference:  mask in reduceScalar is for the input-vector
            // --> not extracting dangling pr entries before
            double danglingSum = CommonOps_DArray.reduceScalar(pr, DMonoids.PLUS, danglingNodesMask);


            // Multiply by damping factor and 1 / |V|
            danglingSum *= (dampingFactor / nodeCount);

            //
            // PageRank summarization
            // Add teleport, importanceVec, and dangling_vec components together
            //
            Arrays.fill(pr, teleport + danglingSum);
            CommonOps_DArray.elementWiseAdd(pr, importanceResultVec, pr, DMonoids.PLUS);


            // !! Difference to reference: no tolerance contained
            // TODO: this is basically elementWiseAdd/Mult using minus-monoid
            // calculate diff (for tolerance check)
            for (int i = 0; i < prevResult.length; i++) {
                prevResult[i] = prevResult[i] - pr[i];
            }
            CommonOps_DArray.apply(prevResult, Math::abs);
            resultDiff = CommonOps_DArray.reduceScalar(prevResult, DMonoids.PLUS);
        }

        return new PageRankResult(pr, iterations);
    }
}
