package org.github.florentind.graphalgos.bfs;

import java.nio.Buffer;

import static com.github.fabianmurariu.unsafe.GRAPHBLAS.*;
import static com.github.fabianmurariu.unsafe.GRBCORE.*;
import static com.github.fabianmurariu.unsafe.GRBMONOID.anyMonoidDouble;
import static com.github.fabianmurariu.unsafe.GRBMONOID.minMonoidInt;
import static com.github.fabianmurariu.unsafe.GRBOPSMAT.vxm;
import static com.github.fabianmurariu.unsafe.GRBOPSVEC.assign;
import static org.github.florentind.core.grapblas_native.NativeHelper.checkStatusCode;

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
        assert getFormat(adjacencyMatrix) == GxB_BY_COL;

        checkStatusCode(setGlobalInt(GxB_NTHREADS, concurrency));

        long nodeCount = nrows(adjacencyMatrix);

        // result vector
        Buffer resultVector = createVector(intType(), nodeCount);
        // make result vector dense
        checkStatusCode(assignVectorInt(resultVector, null, null, 0, GrB_ALL, nodeCount, null));
        // finish pending work on v
        nvalsVector(resultVector);

        // queue vector
        Buffer queueVector = createVector(booleanType(), nodeCount);
        // init node vector
        setVectorElementBoolean(queueVector, startNode, true);

        // ! difference to ejml version: here exists an any monoid as well as a pair op
        //              any is non-deterministic -> not possible in ejml semi-rings
        //              any + pair -> determenistic as it will always return 1 if one pair is found
        Buffer semiRing = createSemiring(anyMonoidDouble(), pairBinaryOpDouble());

        Buffer multDesc = createDescriptor();
        // invert the mask
        checkStatusCode(setDescriptorValue(multDesc, GrB_MASK , GrB_COMP));
        // clear q first
        checkStatusCode(setDescriptorValue(multDesc, GrB_OUTP , GrB_REPLACE));

        Buffer assignDesc = createDescriptor();
        checkStatusCode(setDescriptorValue(assignDesc, GrB_MASK, GrB_STRUCTURE));

        int level = 1;
        // nodeCount
        int nodesVisited = 0;
        long nodesInQueue = 1;

        // BFS-traversal
        for (; ; level++) {
            // v<q> = level, using vector assign with q as the mask
            // no option to use GrB_ALL -> but ni = nodeCount leads to it being used
            checkStatusCode(
                    assignVectorInt(resultVector, queueVector, null, level, GrB_ALL, nodeCount, assignDesc)
            );

            nodesVisited += nodesInQueue ;
            // check for fixPoint
            if (nodesInQueue == 0 || nodesVisited == nodeCount || level > maxIterations) break ;

            // q<¬v> = q lor.land matrix
            checkStatusCode(vxm(queueVector, resultVector, null, semiRing, queueVector, adjacencyMatrix, multDesc));

            nodesInQueue = nvalsVector(queueVector);
        }

        // output vector
        int[] values = new int[Math.toIntExact(nodeCount)];
        long[] indices = new long[Math.toIntExact(nodeCount)];

        // make sure everything got written
        vectorWait(resultVector);
        checkStatusCode(extractVectorTuplesInt(resultVector, values, indices));


        // free c-allocated stuff
        freeVector(queueVector);
        freeVector(resultVector);
        freeDescriptor(multDesc);
        freeDescriptor(assignDesc);
        freeSemiring(semiRing);

        // just using values as we know its a dense vector
        return new BfsDenseIntegerResult(values, level - 1, 0);
    }

}
