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
    private static final DMonoid SECOND_MONOID = new DMonoid(0, (a, b) -> b);
    // TODO: version that uses the same operators as used in graphblas (probably using dense vectors .. as easier)
    //          f.i. fixPoint recognized using reduce with OR-monoid

    // TODO use DSemiRing FIRST_AND instead of OR_AND (first has 1 as a default?) .. OR_FIRST / OR_SECOND
    //      for dense version: FIRST still needs to check for (a != 0)?!
    //  as its just a boolean value thats interesting here
    // .. hope for short circuit OR (or else write in eval part)

    // TODO: is the tmp iterationResult really necessary? (just the inputVector could be enough)


    public BfsDenseDoubleResult computeDense(DMatrixSparseCSC adjacencyMatrix, BfsVariation bfsVariation, int startNode, int maxIterations) {
        DMonoid firstNotZeroMonoid = new DMonoid(0, (a, b) -> (a != 0) ? 1 : 0);
        // as dense here:
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

        for (; (iteration <= maxIterations) && !isFixPoint; iteration++) {
            // negated -> dont compute values for visited nodes
            // replace -> iterationResult is basically the new inputVector
            PrimitiveDMask mask = DMasks.builder(result).withZeroElement(semiRing.add.id).withNegated(true).withReplace(true).build();
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
        //          -> use a DSparseVector (then just one start node allowed)
        DMatrixSparseCSC result = new DMatrixSparseCSC(startNodes.length, adjacencyMatrix.numCols);
        // DMatrixSparseCSC iterationResult = result.createLike();

        // init result vector
        for (int i = 0; i < startNodes.length; i++) {
            if (bfsVariation == BfsVariation.PARENTS) {
                result.set(0, startNodes[i], i + 1);
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
        DMonoid first_monoid = new DMonoid(1, (a, b) -> a);
        // TODO see why first cannot be used for plus ! .. need sparse vector x csc matrix op for this to work ..
        DSemiRing semiRing = bfsVariation == BfsVariation.PARENTS ? DSemiRings.MIN_FIRST : new DSemiRing(SECOND_MONOID, first_monoid);

        int iteration = 1;

        for (; (iteration <= maxIterations); iteration++) {
            // negated -> dont compute values for visited nodes
            // replace -> iterationResult is basically the new inputVector
            Mask mask = DMasks.builder(result, true).withNegated(true).withReplace(true).build();
            iterationResult = CommonOpsWithSemiRing_DSCC.mult(inputVector, adjacencyMatrix, iterationResult, semiRing, mask, null, gw, gx);

            if (mask.replace) {
                nodesVisited += iterationResult.nz_length;
            }

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
            if ((iterationResult.nz_length == 0) || (nodesVisited == adjacencyMatrix.numCols)) {
                break;
            }
        }

        // expect the result to be a row vector / row vectors
        return new BfsSparseResult(result, iteration, semiRing.add.id);
    }


    public enum BfsVariation {
        BOOLEAN, PARENTS, LEVEL
    }
}
