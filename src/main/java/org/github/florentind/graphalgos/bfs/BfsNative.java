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

    // TODO: add tolerance parameter
    /**
     *  BFS based on based on https://github.com/DrTimothyAldenDavis/SuiteSparse/blob/master/GraphBLAS/Demo/Source/bfs5m.c
     *  @param adjacencyMatrix adjacency matrix in CSC format
     */
    public BfsResult computeLevel(Buffer adjacencyMatrix, int startNode, int maxIterations) {
        // assert adj-matrix to be in CSC
        assert GRBCORE.getFormat(adjacencyMatrix) == 1;

        // GxB_DESCRIPTOR_NTHREADS use to set number of threads (is a global option -> can be set via GxB_set)
        int concurrency = 1;

        long nodeCount = GRBCORE.nrows(adjacencyMatrix);

        System.out.println("Matrix: \n" + doubleMatrixToString(adjacencyMatrix, Math.toIntExact(nodeCount)));

        // result vector
        Buffer resultVector = GRBCORE.createVector(GRAPHBLAS.intType(), nodeCount);

        // TODO make result vector dense (optimization?)
        // GrB_Vector_assign_INT32 (v, NULL, NULL, 0, GrB_ALL, n, NULL)
        // for non-blocking mode:  GrB_Vector_nvals (&n, v) ;             // finish pending work on v

        // queue vector
        Buffer queueVector = GRBCORE.createVector(GRAPHBLAS.booleanType(), nodeCount);
        // init node vector
        GRAPHBLAS.setVectorElementBoolean(queueVector, startNode, true);

        Buffer semiRing = GRBCORE.createSemiring(GRBMONOID.lorMonoid(), GRAPHBLAS.landBinaryOp());

        Buffer desc = GRBCORE.createDescriptor();
        // TODO find a way to set: GRBCORE.GxB_DESCRIPTOR_NTHREADS concurrency
        // according to user guide it should be via the descriptor field GxB_DESCRIPTOR_NTHREADS = GxB_NTHREADS
        // see https://github.com/DrTimothyAldenDavis/SuiteSparse/blob/master/GraphBLAS/Source/GxB_Desc_set.c
        // GRBCORE.setDescriptorValue(desc, GRBCORE.GxB_DESCRIPTOR_NTHREADS , concurrency);



        // invert the mask
        GRBCORE.setDescriptorValue(desc, GRBCORE.GrB_MASK ,GRBCORE.GrB_COMP);
        // clear q first
        GRBCORE.setDescriptorValue(desc, GRBCORE.GrB_OUTP ,GRBCORE.GrB_REPLACE);


        boolean successor = true;

        int level = 0;

        // BFS-traversal
        for (; successor && level < maxIterations; level++) {
            // TODO translate vector-assign  .. resultVector<q> = level
            // v<q> = level, using vector assign with q as the mask
            // GrB_Vector_assign_INT32 (v, q, NULL, level, GrB_ALL, n, NULL) ;

            // TODO translate GrB_Vector_clear
            // clear queue Vector .. eq GrB_Vector_clear(queueVector);
            // q<¬v> = q lor.land matrix
            int status = GRBOPSMAT.vxm(queueVector, resultVector, null, semiRing, queueVector, adjacencyMatrix, desc);
            assert status == GRBCORE.GrB_SUCCESS;

            System.out.println("queueVector " + booleanVectorToString(queueVector, Math.toIntExact(nodeCount)));
            System.out.println("resultVector " + integerVectorToString(resultVector, Math.toIntExact(nodeCount)));

            // successor = ||(q)
            successor = GRBALG.vectorReduceAllBoolean(false, null, GRBMONOID.lorMonoid(), queueVector, null);

            System.out.println("successor = " + successor);
        }

        // output vector
        // TODO just use values if we know its a dense vector?
        int[] values = new int[Math.toIntExact(nodeCount)];
        long[] indices = new long[Math.toIntExact(nodeCount)];

        GRAPHBLAS.extractVectorTuplesInt(resultVector, values, indices);

        System.out.println("Id, Level");
        for (int i = 0; i < nodeCount; i++) {
            System.out.printf("%d , %d%n", indices[i],values[i]);
        }



        // free c-allocated stuff
        GRBCORE.freeVector(queueVector);
        GRBCORE.freeVector(resultVector);
        GRBCORE.freeDescriptor(desc);
        GRBCORE.freeSemiring(semiRing);

        return new NativeSparseBfsResult(values, indices, level);
    }

    public class NativeSparseBfsResult implements BfsResult {
        private final int[] values;
        private final long[] indices;
        private final int iterations;
        private final int notFoundValue;

        public NativeSparseBfsResult(int[] values, long[] indices, int iterations) {
            this.values = values;
            this.indices = indices;
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
            int index = Arrays.binarySearch(indices, nodeId);
            if (index > 0) {
                return values[index];
            } else{
                return notFoundValue;
            }
        }
    }
}
