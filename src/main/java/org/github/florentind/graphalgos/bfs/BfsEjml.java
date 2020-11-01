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

    // TODO try using a sparse vector
    public BfsSparseResult computeSparse(DMatrixSparseCSC adjacencyMatrix, BfsVariation bfsVariation, int[] startNodes, int maxIterations) {
        int nodeCount = adjacencyMatrix.numCols;
        DMatrixSparseCSC result = new DMatrixSparseCSC(startNodes.length, nodeCount);
        DMatrixSparseCSC inputVector = result.createLike();

        // init result vector
        for (int startNode : startNodes) {
            if (bfsVariation == BfsVariation.PARENTS) {
                inputVector.set(0, startNode, startNode + 1);
            } else {
                inputVector.set(0, startNode, 1);
            }
        }

        // work space for saving iterationResult and combinedResult
        DMatrixSparseCSC iterationResult = result.createLike();

        // for reusing memory
        IGrowArray gw = new IGrowArray();
        DGrowArray gx = new DGrowArray();
        DMatrixSparseCSC tmp;

        int visitedNodes = 0;
        int nodesInQueue = startNodes.length;

        // first, as ANY is not existing in ejml
        DSemiRing semiRing = bfsVariation == BfsVariation.PARENTS ? DSemiRings.MIN_FIRST : FIRST_PAIR;

        int iteration = 1;

        for (; ; iteration++) {
            if (bfsVariation == BfsVariation.LEVEL) {
                // TODO: use assignScalar here too? (need mask iterator for that)
                //      -> avoid costly combineResults
                int currentIteration = iteration;
                CommonOps_DSCC.apply(inputVector, x -> currentIteration);
            }

            // assign inputVector entries to result (ideally use assign/assignScalar instead of a simple add)
            // using "iterationResult" as a workspace
            // TODO: replace with assign operation
            MaskUtil_DSCC.add(result, inputVector, iterationResult, null, null, gw, gx);
            tmp = result;
            result = iterationResult;
            iterationResult = tmp;

            visitedNodes += nodesInQueue;
            // check for fixPoint
            if (nodesInQueue == 0 || visitedNodes == nodeCount || iteration > maxIterations) break;

            if (bfsVariation == BfsVariation.PARENTS) {
                // set value to its own id
                CommonOps_DSCC.applyColumnIdx(inputVector, (colIdx, val) -> colIdx + 1, inputVector);
            }

            // negated -> dont compute values for visited nodes
            // replace -> iterationResult is basically the new inputVector
            Mask mask = DMasks.builder(result, true).withNegated(true).build();
            iterationResult = CommonOpsWithSemiRing_DSCC.mult(inputVector, adjacencyMatrix, iterationResult, semiRing, mask, null, true, gw, gx);


            // set inputVector based on newly discovered nodes
            tmp = inputVector;
            inputVector = iterationResult;
            iterationResult = tmp;

            nodesInQueue = inputVector.nz_length;
        }

        // expect the result to be a row vector / row vectors
        return new BfsSparseResult(result, iteration - 1, semiRing.add.id);
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

        for (; ; iteration++) {
            nodesVisited += inputVector.nz_length;

            if (bfsVariation == BfsVariation.PARENTS) {
                CommonOps_DArray.assign(result, inputVector);
                // set value to its own id
                CommonOps_DSCC.applyColumnIdx(inputVector, (colIdx, val) -> colIdx + 1, inputVector);
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
