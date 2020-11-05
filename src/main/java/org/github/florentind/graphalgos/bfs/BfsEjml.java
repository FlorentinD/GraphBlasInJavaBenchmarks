package org.github.florentind.graphalgos.bfs;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.DVectorSparse;
import org.ejml.data.IGrowArray;
import org.ejml.masks.DMasks;
import org.ejml.masks.Mask;
import org.ejml.masks.PrimitiveDMask;
import org.ejml.ops.*;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.CommonVectorOps_DSCC;
import org.ejml.sparse.csc.MaskUtil_DSCC;
import org.ejml.sparse.csc.mult.MatrixSparseVectorMultWithSemiRing_DSCC;
import org.ejml.sparse.csc.mult.MatrixVectorMultWithSemiRing_DSCC;
import org.github.florentind.core.ejml.EjmlUtil;

import java.util.Arrays;

import static org.github.florentind.core.ejml.EjmlUtil.FIRST_PAIR;

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

        double[] inputVector = result.clone();

        if (bfsVariation == BfsVariation.PARENTS) {
            inputVector[startNode] = startNode + 1;
        } else {
            inputVector[startNode] = 1;
        }

        double[] iterationResult = new double[nodeCount];

        int visitedNodes = 0;
        int nodesInQueue = 1;
        int iteration = 1;

        // negated -> dont compute values for visited nodes
        // replace -> iterationResult is basically the new inputVector
        PrimitiveDMask mask = DMasks.builder(result).withZeroElement(semiRing.add.id).withNegated(true).build();

        for (; ; iteration++) {
            if (bfsVariation == BfsVariation.LEVEL) {
                PrimitiveDMask resultMask = DMasks.builder(inputVector).withZeroElement(semiRing.add.id).build();
                CommonOps_DArray.assignScalar(result, iteration, resultMask);
            } else {
                // parents version
                // TODO also use an assign/add?
                result = MaskUtil_DSCC.combineOutputs(result, inputVector, mask, null, true);
            }

            visitedNodes += nodesInQueue;
            if (nodesInQueue == 0 || visitedNodes == nodeCount || iteration > maxIterations) break;

            if (bfsVariation == BfsVariation.PARENTS) {
                CommonOps_DArray.applyIdx(inputVector, inputVector, (idx, val) -> (val != semiRing.add.id) ? idx + 1 : val);
            }

            iterationResult = MatrixVectorMultWithSemiRing_DSCC.mult(inputVector, adjacencyMatrix, iterationResult, semiRing, mask, null, true);

            // switch pointers as iterationResult is the inputVector for next iteration
            double[] tmp = iterationResult;
            iterationResult = inputVector;
            inputVector = tmp;

            // add newly visited nodes
            nodesInQueue = (int) CommonOps_DArray.reduceScalar(inputVector, 0, (acc, v) -> (v != semiRing.add.id) ? ++acc : acc);
        }

        return new BfsDenseDoubleResult(result, iteration - 1, semiRing.add.id);
    }

    public BfsSparseResult computeSparse(DMatrixSparseCSC adjacencyMatrix, BfsVariation bfsVariation, int startNode, int maxIterations) {
        int nodeCount = adjacencyMatrix.numCols;
        DVectorSparse result = new DVectorSparse(nodeCount, nodeCount);
        DVectorSparse inputVector = result.createLike();

        // init result vector
        if (bfsVariation == BfsVariation.PARENTS) {
            inputVector.set(startNode, startNode + 1);
        } else {
            inputVector.set(startNode, 1);
        }


        // work space for saving iterationResult and combinedResult
        DVectorSparse iterationResult = result.createLike();

        // for reusing memory
        IGrowArray gw = new IGrowArray();
        DVectorSparse tmp;

        int visitedNodes = 0;
        int nodesInQueue = 1;

        // first, as ANY is not existing in ejml
        DSemiRing semiRing = bfsVariation == BfsVariation.PARENTS ? DSemiRings.MIN_FIRST : FIRST_PAIR;

        int iteration = 1;

        for (; ; iteration++) {
            if (bfsVariation == BfsVariation.LEVEL) {
                CommonVectorOps_DSCC.assignScalar(result, iteration, inputVector, gw);
            } else {
                // assign inputVector entries to result (assign is here basically the same as add)
                CommonVectorOps_DSCC.add(result, inputVector, EjmlUtil.SECOND_OP, gw);
            }

            visitedNodes += nodesInQueue;
            // check for fixPoint
            if (nodesInQueue == 0 || visitedNodes == nodeCount || iteration > maxIterations) break;

            if (bfsVariation == BfsVariation.PARENTS) {
                // set value to its own id
                CommonOps_DSCC.applyIdx(inputVector, (rowIdx, val) -> rowIdx + 1, inputVector);
            }

            // negated -> dont compute values for visited nodes
            // replace -> iterationResult is basically the new inputVector
            Mask mask = DMasks.builder(result, true).withNegated(true).build();
            iterationResult = MatrixSparseVectorMultWithSemiRing_DSCC.mult(inputVector, adjacencyMatrix, iterationResult, semiRing, mask, null, true, gw);

            // set inputVector based on newly discovered nodes
            tmp = inputVector;
            inputVector = iterationResult;
            iterationResult = tmp;

            nodesInQueue = inputVector.nz_length();
        }

        // expect the result to be a row vector / row vectors
        return new BfsSparseResult(result, iteration - 1, semiRing.add.id);
    }


    // dense result vector and sparse queue vector
    public BfsDenseDoubleResult computeDenseSparse(DMatrixSparseCSC adjacencyMatrix, BfsVariation bfsVariation, int startNode, int maxIterations) {
        int nodeCount = adjacencyMatrix.numCols;
        double[] result = new double[nodeCount];
        DVectorSparse inputVector = new DVectorSparse(nodeCount, nodeCount);

        // init result vector
        if (bfsVariation == BfsVariation.PARENTS) {
            inputVector.set(startNode, startNode + 1);
        } else {
            inputVector.set(startNode, 1);
        }

        // for reusing memory
        IGrowArray gw = new IGrowArray();
        DVectorSparse iterationResult = null;

        int nodesVisited = 0;

        // first, as ANY is not existing in ejml
        DSemiRing semiRing = bfsVariation == BfsVariation.PARENTS ? DSemiRings.MIN_FIRST : FIRST_PAIR;
        Arrays.fill(result, semiRing.add.id);

        int iteration = 1;

        // negated -> don't compute values for visited nodes
        // replace -> iterationResult is basically the new inputVector
        Mask mask = DMasks.builder(result)
                .withZeroElement(semiRing.add.id)
                .withNumCols(1)
                .withNegated(true)
                .build();

        for (; ; iteration++) {
            nodesVisited += inputVector.nz_length();

            if (bfsVariation == BfsVariation.PARENTS) {
                CommonOps_DArray.assign(result, inputVector.oneDimMatrix);
                // set value to its own id
                CommonOps_DSCC.applyIdx(inputVector, (colIdx, val) -> colIdx + 1, inputVector);
            } else {
                // assign scalar for level (inputVector as a mask)
                CommonOps_DArray.assignScalar(result, iteration, inputVector.oneDimMatrix);
            }

            // check for fixPoint
            if ((inputVector.nz_length() == 0) || (nodesVisited == nodeCount) || (iteration >= maxIterations)) {
                break;
            }

            iterationResult = MatrixSparseVectorMultWithSemiRing_DSCC.mult(inputVector, adjacencyMatrix, iterationResult, semiRing, mask, null, true, gw);

            // switch references .. less costly then clone
            DVectorSparse tmp = inputVector;
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
