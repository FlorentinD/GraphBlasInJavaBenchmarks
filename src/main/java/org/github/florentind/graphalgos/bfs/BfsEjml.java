package org.github.florentind.graphalgos.bfs;

import org.ejml.data.DGrowArray;
import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.IGrowArray;
import org.ejml.masks.DMasks;
import org.ejml.masks.Mask;
import org.ejml.masks.PrimitiveDMask;
import org.ejml.ops.CommonOps_DArray;
import org.ejml.ops.DSemiRing;
import org.ejml.ops.DSemiRings;
import org.ejml.sparse.csc.CommonOpsWithSemiRing_DSCC;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.MaskUtil_DSCC;
import org.ejml.sparse.csc.mult.MatrixVectorMultWithSemiRing_DSCC;

import java.util.Arrays;

// variants: boolean/parents/level/multi-bfs  + sparse/dense result vector
public class BfsEjml {
    // TODO: version that uses the same operators as used in graphblas (probably using dense vectors .. as easier)
    //          f.i. fixPoint recognized using reduce with OR-monoid

    // TODO use DSemiRing FIRST_AND instead of OR_AND (first has 1 as a default?) .. OR_FIRST as its just a boolean value thats interesting here
    // .. hope for short circuit OR (or else write in eval part)

    private static DSemiRing getSemiRing(BfsVariation variation) {
        return variation == BfsVariation.PARENTS ? DSemiRings.MIN_FIRST : DSemiRings.OR_AND;
    }

    // TODO: is the tmp iterationResult really necessary? (just the inputVector could be enough)


    // TODO: use dense matrix instead of pure primitive array to support MSBFS
    public BfsDenseResult computeDense(DMatrixSparseCSC adjacencyMatrix, BfsVariation bfsVariation, int startNode, int maxIterations) {
        DSemiRing semiRing = getSemiRing(bfsVariation);
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

            result = MaskUtil_DSCC.combineOutputs(result, iterationResult, mask, null);

            isFixPoint = (visitedNodes == prevVisitedNodes) || (visitedNodes == adjacencyMatrix.numCols);
        }

        return new BfsDenseResult(result, iteration - 1, semiRing.add.id);
    }

    public BfsSparseResult computeSparse(DMatrixSparseCSC adjacencyMatrix, BfsVariation bfsVariation, int[] startNodes, int maxIterations) {
        // TODO: use transposed result matrix as startNodes.length << adjacencyMatrix.length
        //         need to transpose result of VxM before combining
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

        int visitedNodes = startNodes.length;
        int prevVisitedNodes;

        DSemiRing semiRing = getSemiRing(bfsVariation);

        boolean isFixPoint = false;
        int iteration = 1;

        for (; (iteration <= maxIterations) && !isFixPoint; iteration++) {
            // negated -> dont compute values for visited nodes
            // replace -> iterationResult is basically the new inputVector
            Mask mask = DMasks.builder(result, true).withNegated(true).withReplace(true).build();
            iterationResult = CommonOpsWithSemiRing_DSCC.mult(inputVector, adjacencyMatrix, iterationResult, semiRing, mask, null, gw, gx);
            prevVisitedNodes = visitedNodes;

            if (mask.replace) {
                visitedNodes += iterationResult.nz_length;
            } else {
                visitedNodes = iterationResult.nz_length;
            }

            // set inputVector based on newly discovered nodes
            // TODO: do this via an `assign` that supports a mask
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

            // combine iterationResult and result (basically poor mans `mask.replace=false`)
            result = MaskUtil_DSCC.combineOutputs(result, iterationResult, null, null);

            // TODO dont combine result if its known to be a fixPoint?
            isFixPoint = (visitedNodes == prevVisitedNodes) || (visitedNodes == adjacencyMatrix.numCols);
        }

        // expect the result to be a row vector / row vectors
        return new BfsSparseResult(result, iteration - 1, semiRing.add.id);
    }


    public enum BfsVariation {
        BOOLEAN, PARENTS, LEVEL
    }

    // TODO move into extra file
    public class BfsSparseResult implements BfsResult {
        private final DMatrixSparseCSC result;
        private final double notFoundValue;
        private final int iterations;

        public BfsSparseResult(DMatrixSparseCSC result, int iterations, double fallBackValue) {
            this.result = result;
            this.iterations = iterations;
            notFoundValue = fallBackValue;
        }

        @Override
        public int iterations() {
            return this.iterations;
        }

        @Override
        public int nodesVisited() {
            return this.result.getNonZeroLength();
        }

        @Override
        public double get(int nodeId) {
            return result.get(0, nodeId, notFoundValue);
        }

        public DMatrixSparseCSC result() {
            return this.result;
        }
    }

    public class BfsDenseResult implements BfsResult {
        private final double[] result;
        private final double notFoundValue;
        private final int iterations;

        public BfsDenseResult(double[] result, int iterations, double notFoundValue) {
            this.result = result;
            this.iterations = iterations;
            this.notFoundValue = notFoundValue;
        }

        @Override
        public int iterations() {
            return this.iterations;
        }

        @Override
        public int nodesVisited() {
            int visited = 0;

            for (double v : result) {
                if (v != notFoundValue) visited++;
            }

            return visited;
        }

        @Override
        public double get(int nodeId) {
            return result[nodeId];
        }

        public double[] result() {
            return this.result;
        }
    }
}
