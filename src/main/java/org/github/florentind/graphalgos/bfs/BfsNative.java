package org.github.florentind.graphalgos.bfs;

import com.github.fabianmurariu.unsafe.*;

import java.nio.Buffer;

import static org.github.florentind.core.grapblas_native.NativeVectorToString.booleanVectorToString;
import static org.github.florentind.core.grapblas_native.NativeVectorToString.integerVectorToString;

/**
 * implementation based on graphblas-java-native ops e.g. C magic
 */
public class BfsNative {
// TODO: add parent version
    /**
     *  BFS based on based on https://github.com/DrTimothyAldenDavis/SuiteSparse/blob/master/GraphBLAS/Demo/Source/bfs5m.c
     *  @param adjacencyMatrix adjacency matrix in CSC format
     */
    public BfsResult computeLevel(Buffer adjacencyMatrix, int startNode, int maxIterations, int concurrency) {
        // assert adj-matrix to be in CSC
        assert GRBCORE.getFormat(adjacencyMatrix) == GRBCORE.GxB_BY_COL;

        GRBCORE.setGlobalInt(GRBCORE.GxB_NTHREADS, concurrency);

        long status;
        long nodeCount = GRBCORE.nrows(adjacencyMatrix);

        // result vector
        Buffer resultVector = GRBCORE.createVector(GRAPHBLAS.intType(), nodeCount);
        // make result vector dense
        status = GRAPHBLAS.assignVectorInt(resultVector, null, null, 0, GRBCORE.GrB_ALL, nodeCount, null);
        assert status == GRBCORE.GrB_SUCCESS;
        // finish pending work on v
        GRBCORE.nvalsVector(resultVector);

        // queue vector
        Buffer queueVector = GRBCORE.createVector(GRAPHBLAS.booleanType(), nodeCount);
        // init node vector
        GRAPHBLAS.setVectorElementBoolean(queueVector, startNode, true);

        // ! difference to ejml version: here exists an any monoid as well as a pair op
        //              any is non-deterministic -> not possible in ejml semi-rings
        //              any + pair -> determenistic as it will always return 1 if one pair is found
        Buffer semiRing = GRBCORE.createSemiring(GRBMONOID.anyMonoidDouble(), GRAPHBLAS.pairBinaryOpDouble());

        Buffer desc = GRBCORE.createDescriptor();
        // invert the mask
        GRBCORE.setDescriptorValue(desc, GRBCORE.GrB_MASK ,GRBCORE.GrB_COMP);
        // clear q first
        GRBCORE.setDescriptorValue(desc, GRBCORE.GrB_OUTP ,GRBCORE.GrB_REPLACE);


        int level = 1;
        // nodeCount
        int nodesVisited = 0;
        long nodesInQueue = 1;

        // BFS-traversal
        for (; level < maxIterations; level++) {
            // v<q> = level, using vector assign with q as the mask
            // no option to use GrB_ALL -> but ni = nodeCount leads to it being used
            status = GRAPHBLAS.assignVectorInt(resultVector, queueVector, null, level, GRBCORE.GrB_ALL, nodeCount, null);
            assert status == GRBCORE.GrB_SUCCESS;

//            System.out.println("queueVector " + booleanVectorToString(queueVector, Math.toIntExact(nodeCount)));
//            System.out.println("resultVector " + integerVectorToString(resultVector, Math.toIntExact(nodeCount)));

            nodesVisited += nodesInQueue ;
            // check for fixPoint
            if (nodesInQueue == 0 || nodesVisited == nodeCount || level >= maxIterations) break ;

            // q<¬v> = q lor.land matrix
            status = GRBOPSMAT.vxm(queueVector, resultVector, null, semiRing, queueVector, adjacencyMatrix, desc);
            assert status == GRBCORE.GrB_SUCCESS;

            nodesInQueue = GRBCORE.nvalsVector(queueVector);
        }

        // output vector
        int[] values = new int[Math.toIntExact(nodeCount)];
        long[] indices = new long[Math.toIntExact(nodeCount)];

        // make sure everything got written
        GRBCORE.vectorWait(resultVector);
        GRAPHBLAS.extractVectorTuplesInt(resultVector, values, indices);


        // free c-allocated stuff
        GRBCORE.freeVector(queueVector);
        GRBCORE.freeVector(resultVector);
        GRBCORE.freeDescriptor(desc);
        GRBCORE.freeSemiring(semiRing);

        // just using values as we know its a dense vector
        return new NativeBfsResult(values, level - 1);
    }

    public class NativeBfsResult implements BfsResult {
        private final int[] values;
        private final int iterations;
        private final int notFoundValue;

        public NativeBfsResult(int[] values, int iterations) {
            this.values = values;
            this.iterations = iterations;
            // for now only LEVEL variant exists
            this.notFoundValue = 0;
        }

        @Override
        public int iterations() {
            return this.iterations;
        }

        @Override
        public int nodesVisited() {
            // works as the result vector is sparse
            return this.values.length;
        }

        @Override
        public double get(int nodeId) {
            return values[nodeId];
        }
    }
}
