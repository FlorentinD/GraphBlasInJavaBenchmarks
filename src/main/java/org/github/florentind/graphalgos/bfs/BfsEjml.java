package org.github.florentind.graphalgos.bfs;

import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.masks.DMasks;
import org.ejml.masks.Mask;
import org.ejml.masks.PrimitiveDMask;
import org.ejml.ops.*;
import org.ejml.sparse.csc.CommonOpsWithSemiRing_DSCC;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.MaskUtil_DSCC;
import org.ejml.sparse.csc.mult.MatrixVectorMultWithSemiRing_DSCC;

import java.util.Arrays;

// variants: boolean/parents/level/multi-bfs  + sparse/dense result vector
public class BfsEjml {
    // TODO: version that uses the same operators as used in graphblas (probably using dense vectors .. as easier)


    // TODO: is the tmp iterationResult really necessary? (just the inputVector could be enough)


    public BfsDenseDoubleResult computeDense(DMatrixSparseCSC adjacencyMatrix, BfsVariation bfsVariation, int startNode, int maxIterations) {
        // if the inputVector entry is not zero -> return true (sparse adjacency matrix -> entry exists == true)
        DMonoid firstNotZeroMonoid = new DMonoid(0, (a, b) -> (a != 0) ? 1 : 0);
        // as dense here: cannot use FIRST instead of OR
        DSemiRing levelSemiRing = new DSemiRing(DMonoids.OR, firstNotZeroMonoid);
        DSemiRing semiRing = bfsVariation == BfsVariation.PARENTS ? DSemiRings.MIN_FIRST : levelSemiRing;
        double[] result = new double[adjacencyMatrix.numCols];
        Arrays.fill(result, semiRing.add.id);

        if (bfsVariation == BfsVariation.PARENTS) {
            result[startNode] = startNode + 1;
        } else {
            result[startNode] = 1;
        }


        // or use dense matrix and reduceScalar to count non-zero elements
        double[] iterationResult = new double[adjacencyMatrix.numCols];

        int visitedNodes = 1;
        int prevVisitedNodes;

        double[] inputVector = result.clone();
        boolean isFixPoint = false;
        int iteration = 1;

        // negated -> dont compute values for visited nodes
        // replace -> iterationResult is basically the new inputVector
        PrimitiveDMask mask = DMasks.builder(result).withZeroElement(semiRing.add.id).withNegated(true).withReplace(true).build();

        for (; (iteration <= maxIterations) && !isFixPoint; iteration++) {
            // clear iterationsResult to only contain newly discovered nodes
            Arrays.fill(iterationResult, semiRing.add.id);
            iterationResult = MatrixVectorMultWithSemiRing_DSCC.mult(inputVector, adjacencyMatrix, iterationResult, semiRing, mask, null);

            prevVisitedNodes = visitedNodes;

            // add newly visited nodes
            for (double v : iterationResult) {
                if (v != semiRing.add.id) {
                    visitedNodes++;
                }
            }

            inputVector = iterationResult.clone();

            if (bfsVariation == BfsVariation.LEVEL) {
                // TODO: check if apply has any overhead compared to inlined for-loop (potentially due to uneccesary assignments of semiRing.add.id)
                int finalIteration = iteration;
                CommonOps_DArray.apply(iterationResult, i -> (i != semiRing.add.id) ? finalIteration + 1 : semiRing.add.id);
            }

            if (bfsVariation == BfsVariation.PARENTS) {
                for (int i = 0; i < inputVector.length; i++) {
                    if (inputVector[i] != semiRing.add.id) {
                        inputVector[i] = i + 1;
                    }
                }
            }

            result = MaskUtil_DSCC.combineOutputs(result, iterationResult, mask, null, true);

            //System.out.println(Arrays.toString(result));
            isFixPoint = (visitedNodes == prevVisitedNodes) || (visitedNodes == adjacencyMatrix.numCols);
        }

        return new BfsDenseDoubleResult(result, iteration - 1, semiRing.add.id);
    }

    public BfsSparseResult computeSparse(DMatrixSparseCSC adjacencyMatrix, BfsVariation bfsVariation, int[] startNodes, int maxIterations) {
        // TODO: use transposed result matrix as startNodes.length << adjacencyMatrix.length
        //         need to transpose result of VxM before combining
        //          -> use a DSparseVector and write MatrixSparseVector mult op (then just one start node allowed)
        DMatrixSparseCSC result = new DMatrixSparseCSC(startNodes.length, adjacencyMatrix.numCols);

        // init result vector
        for (int i = 0; i < startNodes.length; i++) {
            if (bfsVariation == BfsVariation.PARENTS) {
                result.set(0, startNodes[i], startNodes[i] + 1);
            } else {
                result.set(0, startNodes[i], 1);
            }
        }

        DMatrixSparseCSC inputVector = result.copy();
        DMatrixSparseCSC iterationResult = null;

        // for reusing memory
        IGrowArray gw = new IGrowArray();
        DGrowArray gx = new DGrowArray();

        int nodesVisited = startNodes.length;

        // as the id of the monoid is never used for sparse mult .. this works nicely to use FIRST even for plus here
        // find out why id actually matters for sparse mult
        DMonoid first_monoid = new DMonoid(0, (a, b) -> a);

        DSemiRing semiRing = bfsVariation == BfsVariation.PARENTS ? DSemiRings.MIN_FIRST : new DSemiRing(first_monoid, first_monoid);

        int iteration = 1;

        for (;; iteration++) {
            // negated -> dont compute values for visited nodes
            // replace -> iterationResult is basically the new inputVector
            Mask mask = DMasks.builder(result, true).withNegated(true).withReplace(true).build();
            iterationResult = CommonOpsWithSemiRing_DSCC.mult(inputVector, adjacencyMatrix, iterationResult, semiRing, mask, null, gw, gx);

            nodesVisited += iterationResult.nz_length;


            // set inputVector based on newly discovered nodes
            // TODO: do this via an `assign` that supports a mask (double[] out, DMatrixSparse_CSC/Vector input)
            inputVector = iterationResult.copy();

            if (bfsVariation == BfsVariation.LEVEL) {
                int currentIteration = iteration + 1;
                CommonOps_DSCC.apply(iterationResult, x -> currentIteration);
            }

            if (bfsVariation == BfsVariation.PARENTS) {
                // TODO: generalize to apply for a (row, col, value) -> newValue
                // set value to its own id
                for (int col = 0; col < inputVector.numCols; col++) {
                    int idx = inputVector.col_idx[col];
                    int endIdx = inputVector.col_idx[col + 1];

                    for (; idx < endIdx; idx++) {
                        inputVector.nz_values[idx] = col + 1;
                    }
                }
            }

            // combine iterationResult and result
            // TODO: use a dense result vector for here!
            result = MaskUtil_DSCC.combineOutputs(result, iterationResult, null, null);

            // check for fixPoint
            if ((iterationResult.nz_length == 0) || (nodesVisited == adjacencyMatrix.numCols) || (iteration >= maxIterations)) {
                break;
            }
        }

        // expect the result to be a row vector / row vectors
        return new BfsSparseResult(result, iteration, semiRing.add.id);
    }


    public BfsDenseDoubleResult computeDenseSparse(DMatrixSparseCSC adjacencyMatrix, BfsVariation bfsVariation, int startNode, int maxIterations) {
        int nodeCount = adjacencyMatrix.numCols;
        double[] result = new double[nodeCount];
        DMatrixSparseCSC inputVector = new DMatrixSparseCSC(1, nodeCount);

        // init result vector

            if (bfsVariation == BfsVariation.PARENTS) {
                inputVector.set(0, startNode, startNode + 1);
            } else {
                inputVector.set(0, startNode, 1);
            }

        // for reusing memory
        IGrowArray gw = new IGrowArray();
        DGrowArray gx = new DGrowArray();
        DMatrixSparseCSC iterationResult = null;

        int nodesVisited = 0;

        // as the id of the monoid is never used for sparse mult .. this works nicely to use FIRST even for plus here
        // find out why id actually matters for sparse mult
        DMonoid first_monoid = new DMonoid(0, (a, b) -> a);
        DSemiRing semiRing = bfsVariation == BfsVariation.PARENTS ? DSemiRings.MIN_FIRST : new DSemiRing(first_monoid, first_monoid);

        int iteration = 1;

        // negated -> dont compute values for visited nodes
        // replace -> iterationResult is basically the new inputVector
        Mask mask = DMasks.builder(result).withNumCols(nodeCount).withNegated(true).withReplace(true).build();

        for (;; iteration++) {
            nodesVisited += inputVector.nz_length;

            if (bfsVariation == BfsVariation.LEVEL) {
                int currentIteration = iteration;
                CommonOps_DSCC.apply(inputVector, x -> currentIteration);
            }

            // TODO assign scalar for level or boolean (inputVector as a mask)
            result = CommonOps_DArray.assign(result, inputVector);

            if (bfsVariation == BfsVariation.PARENTS) {
                // set value to its own id
                for (int col = 0; col < inputVector.numCols; col++) {
                    // as inputVector only has 1 row ..
                    int nz_idx = inputVector.col_idx[col];
                    if (nz_idx != inputVector.col_idx[col + 1]) {
                        inputVector.nz_values[nz_idx] = col + 1;
                    }
                }
            }

            // check for fixPoint
            if ((inputVector.nz_length == 0) || (nodesVisited == nodeCount) || (iteration >= maxIterations)) {
                break;
            }

            iterationResult = CommonOpsWithSemiRing_DSCC.mult(inputVector, adjacencyMatrix, iterationResult, semiRing, mask, null, gw, gx);

            // switch references .. less costly then clone
            DMatrixSparseCSC tmp = inputVector;
            inputVector = iterationResult;
            iterationResult = tmp;
        }

        // expect the result to be a row vector / row vectors
        return new BfsDenseDoubleResult(result, iteration - 1, semiRing.add.id);
    }

    public enum BfsVariation {
        BOOLEAN, PARENTS, LEVEL
    }
}
