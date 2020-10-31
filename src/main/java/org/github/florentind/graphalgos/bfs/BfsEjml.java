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

import static org.github.florentind.core.ejml.EjmlUtil.*;

// variants: boolean/parents/level/multi-bfs  + sparse/dense result vector
public class BfsEjml {
    // iteration-result needed, as mult cannot write into the same object in ejml

    public BfsDenseDoubleResult computeDense(DMatrixSparseCSC adjacencyMatrix, BfsVariation bfsVariation, int startNode, int maxIterations) {
        int nodeCount = adjacencyMatrix.numCols;
        // if the inputVector entry is not zero -> return true (sparse adjacency matrix -> entry exists == true)
        DBinaryOperator firstNotZeroOp = (a, b) -> (a != 0) ? 1 : 0;
        // as dense here: cannot use FIRST/ANY instead of OR
        DSemiRing levelSemiRing = new DSemiRing(DMonoids.OR, firstNotZeroOp);
        DSemiRing semiRing = bfsVariation == BfsVariation.PARENTS ? DSemiRings.MIN_FIRST : levelSemiRing;
        double[] result = new double[nodeCount];
        Arrays.fill(result, semiRing.add.id);

        if (bfsVariation == BfsVariation.PARENTS) {
            result[startNode] = startNode + 1;
        } else {
            result[startNode] = 1;
        }

        // or use dense matrix and reduceScalar to count non-zero elements
        double[] iterationResult = new double[nodeCount];

        int visitedNodes = 1;
        int prevVisitedNodes;

        double[] inputVector = result.clone();
        boolean isFixPoint = false;
        int iteration = 1;

        // negated -> dont compute values for visited nodes
        // replace -> iterationResult is basically the new inputVector
        PrimitiveDMask mask = DMasks.builder(result).withZeroElement(semiRing.add.id).withNegated(true).build();

        for (; (iteration <= maxIterations) && !isFixPoint; iteration++) {
            // clear iterationsResult to only contain newly discovered nodes
            Arrays.fill(iterationResult, semiRing.add.id);

            if (bfsVariation == BfsVariation.PARENTS) {
                CommonOps_DArray.applyIdx(inputVector, inputVector, (idx, val) -> (val != semiRing.add.id) ? idx + 1 : val);
            }

            iterationResult = MatrixVectorMultWithSemiRing_DSCC.mult(inputVector, adjacencyMatrix, iterationResult, semiRing, mask, null, true);

            prevVisitedNodes = visitedNodes;

            // add newly visited nodes
            visitedNodes = (int) CommonOps_DArray.reduceScalar(iterationResult, visitedNodes, (acc, v) -> (v != semiRing.add.id) ? ++acc : acc);

            if (bfsVariation == BfsVariation.LEVEL) {
                PrimitiveDMask resultMask = DMasks.builder(iterationResult).withZeroElement(semiRing.add.id).build();
                CommonOps_DArray.assignScalar(result, iteration + 1, resultMask);
            } else {
                // parents version
                result = MaskUtil_DSCC.combineOutputs(result, iterationResult, mask, null, true);
            }

            // switch pointers as iterationResult is the inputVector for next iteration
            double[] tmp = iterationResult;
            iterationResult = inputVector;
            inputVector = tmp;

            isFixPoint = (visitedNodes == prevVisitedNodes) || (visitedNodes == nodeCount);
        }

        return new BfsDenseDoubleResult(result, iteration - 1, semiRing.add.id);
    }

    // TODO try using a sparse vector
    public BfsSparseResult computeSparse(DMatrixSparseCSC adjacencyMatrix, BfsVariation bfsVariation, int[] startNodes, int maxIterations) {
        int nodeCount = adjacencyMatrix.numCols;
        DMatrixSparseCSC result = new DMatrixSparseCSC(startNodes.length, nodeCount);

        // init result vector
        for (int startNode : startNodes) {
            if (bfsVariation == BfsVariation.PARENTS) {
                result.set(0, startNode, startNode + 1);
            } else {
                result.set(0, startNode, 1);
            }
        }

        DMatrixSparseCSC inputVector = result.copy();
        DMatrixSparseCSC iterationResult = null;

        // for reusing memory
        IGrowArray gw = new IGrowArray();
        DGrowArray gx = new DGrowArray();

        int nodesVisited = startNodes.length;

        // first, as ANY is not existing in ejml
        DSemiRing semiRing = bfsVariation == BfsVariation.PARENTS ? DSemiRings.MIN_FIRST : FIRST_PAIR;

        int iteration = 1;

        for (;; iteration++) {
            // negated -> dont compute values for visited nodes
            // replace -> iterationResult is basically the new inputVector
            Mask mask = DMasks.builder(result, true).withNegated(true).build();
            iterationResult = CommonOpsWithSemiRing_DSCC.mult(inputVector, adjacencyMatrix, iterationResult, semiRing, mask, null, true, gw, gx);

            nodesVisited += iterationResult.nz_length;


            // set inputVector based on newly discovered nodes
            inputVector = iterationResult.copy();

            if (bfsVariation == BfsVariation.LEVEL) {
                int currentIteration = iteration + 1;
                CommonOps_DSCC.apply(iterationResult, x -> currentIteration);
            } else {
                // parents version
                // set value to its own id
                CommonOps_DSCC.applyColumnIdx(inputVector, (colIdx, val) -> colIdx+1, inputVector);
            }

            // combine iterationResult and result
            // TODO: replace with an assign operation (also seen as an add for sparse structures?)
            result = MaskUtil_DSCC.combineOutputs(result, iterationResult, null, null);

            // check for fixPoint
            if ((iterationResult.nz_length == 0) || (nodesVisited == nodeCount) || (iteration >= maxIterations)) {
                break;
            }
        }

        // expect the result to be a row vector / row vectors
        return new BfsSparseResult(result, iteration, semiRing.add.id);
    }


    // dense result vector and sparse queue vector
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

        // first, as ANY is not existing in ejml
        DSemiRing semiRing = bfsVariation == BfsVariation.PARENTS ? DSemiRings.MIN_FIRST : FIRST_PAIR;
        Arrays.fill(result, semiRing.add.id);

        int iteration = 1;

        // negated -> don't compute values for visited nodes
        // replace -> iterationResult is basically the new inputVector
        Mask mask = DMasks.builder(result)
                .withZeroElement(semiRing.add.id)
                .withNumCols(nodeCount)
                .withNegated(true)
                .build();

        for (;; iteration++) {
            nodesVisited += inputVector.nz_length;

            if (bfsVariation == BfsVariation.PARENTS) {
                CommonOps_DArray.assign(result, inputVector);
                // set value to its own id
                CommonOps_DSCC.applyColumnIdx(inputVector, (colIdx, val) -> colIdx+1, inputVector);
            } else {
                // assign scalar for level (inputVector as a mask)
                CommonOps_DArray.assignScalar(result, iteration, inputVector);
            }

            // check for fixPoint
            if ((inputVector.nz_length == 0) || (nodesVisited == nodeCount) || (iteration >= maxIterations)) {
                break;
            }

            iterationResult = CommonOpsWithSemiRing_DSCC.mult(inputVector, adjacencyMatrix, iterationResult, semiRing, mask, null, true, gw, gx);

            // switch references .. less costly then clone
            DMatrixSparseCSC tmp = inputVector;
            inputVector = iterationResult;
            iterationResult = tmp;
        }

        // expect the result to be a row vector / row vectors
        return new BfsDenseDoubleResult(result, iteration - 1, semiRing.add.id);
    }

    public enum BfsVariation {
        PARENTS, LEVEL
    }
}
