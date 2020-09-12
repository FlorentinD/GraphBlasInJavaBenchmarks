package org.github.florentind.graphalgos.bfs;

import com.github.fabianmurariu.unsafe.*;

import java.nio.Buffer;
import java.util.Arrays;

import static org.github.florentind.core.grapblas_native.NativeMatrixToString.doubleMatrixToString;
import static org.github.florentind.core.grapblas_native.NativeVectorToString.booleanVectorToString;
import static org.github.florentind.core.grapblas_native.NativeVectorToString.integerVectorToString;

/**
 * implementation based on graphblas-java-native ops e.g. C magic
 */
public class BfsNative {
    // TODO  variable concurrency value
    // TODO: add tolerance parameter
    /**
     *  BFS based on based on https://github.com/DrTimothyAldenDavis/SuiteSparse/blob/master/GraphBLAS/Demo/Source/bfs5m.c
     *  @param adjacencyMatrix adjacency matrix in CSC format
     */
    public BfsResult computeLevel(Buffer adjacencyMatrix, int startNode, int maxIterations) {
        // assert adj-matrix to be in CSC
        assert GRBCORE.getFormat(adjacencyMatrix) == 1;

        // TODO GxB_DESCRIPTOR_NTHREADS use to set number of threads (is a global option -> can be set via GxB_set .. not mapped yet)
        int concurrency = 1;
        long status;
        long nodeCount = GRBCORE.nrows(adjacencyMatrix);

        // result vector
        Buffer resultVector = GRBCORE.createVector(GRAPHBLAS.intType(), nodeCount);
        // make result vector dense
        status = GRAPHBLAS.assignVectorInt(resultVector, null, null, 0, null, nodeCount, null);
        assert status == GRBCORE.GrB_SUCCESS;
        // finish pending work on v
        GRBCORE.nvalsVector(resultVector);

        // queue vector
        Buffer queueVector = GRBCORE.createVector(GRAPHBLAS.booleanType(), nodeCount);
        // init node vector
        GRAPHBLAS.setVectorElementBoolean(queueVector, startNode, true);

        Buffer semiRing = GRBCORE.createSemiring(GRBMONOID.lorMonoid(), GRAPHBLAS.landBinaryOp());

        Buffer desc = GRBCORE.createDescriptor();
        // invert the mask
        GRBCORE.setDescriptorValue(desc, GRBCORE.GrB_MASK ,GRBCORE.GrB_COMP);
        // clear q first
        GRBCORE.setDescriptorValue(desc, GRBCORE.GrB_OUTP ,GRBCORE.GrB_REPLACE);


        boolean successor = true;
        int level = 1;

        // BFS-traversal
        for (; successor && level < maxIterations; level++) {
            // v<q> = level, using vector assign with q as the mask
            // no option to use GrB_ALL -> but ni = nodeCount leads to it being used
            status = GRAPHBLAS.assignVectorInt(resultVector, queueVector, null, level, null, nodeCount, null);
            assert status == GRBCORE.GrB_SUCCESS;

            // q<¬v> = q lor.land matrix
            status = GRBOPSMAT.vxm(queueVector, resultVector, null, semiRing, queueVector, adjacencyMatrix, desc);
            assert status == GRBCORE.GrB_SUCCESS;

//            System.out.println("queueVector " + booleanVectorToString(queueVector, Math.toIntExact(nodeCount)));
//            System.out.println("resultVector " + integerVectorToString(resultVector, Math.toIntExact(nodeCount)));

            // successor = ||(q)
            successor = GRBALG.vectorReduceAllBoolean(false, null, GRBMONOID.lorMonoid(), queueVector, null);

//            System.out.println("successor = " + successor);
        }

        // output vector
        int[] values = new int[Math.toIntExact(nodeCount)];
        long[] indices = new long[Math.toIntExact(nodeCount)];

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
