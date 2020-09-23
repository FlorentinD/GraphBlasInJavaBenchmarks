package org.github.florentind.graphalgos.pageRank;

import com.github.fabianmurariu.unsafe.*;

import java.nio.Buffer;

import static com.github.fabianmurariu.unsafe.GRBCORE.GrB_ALL;

public class PageRankNative {
    public static final int DEFAULT_MAX_ITERATIONS = 20;
    public static final double DEFAULT_DAMPING_FACTOR = 0.85;
    public static final float DEFAULT_TOLERANCE = 1e-7f;

    /**
     * based on https://github.com/GraphBLAS/LAGraph/blob/master/Source/Algorithm/LAGraph_pagerank2.c
     * and therefore LDBC-Graphalytics conform
     *
     * @param adjacencyMatrix (Input) Graph
     * @param dampingFactor   How often are teleports
     * @param tolerance       Minimum change in scores between iterations
     * @param maxIterations   Maximum number of iterations
     * @return pr-scores (sum of all scores = 1)
     */
    public static PageRankResult compute(Buffer adjacencyMatrix, double dampingFactor, double tolerance, int maxIterations, int concurrency) {
        // assert adj-matrix to be in CSC
        assert GRBCORE.getFormat(adjacencyMatrix) == GRBCORE.GxB_BY_COL;
        GRBCORE.setGlobalInt(GRBCORE.GxB_NTHREADS, concurrency);

        long status;

        long nodeCount = GRBCORE.ncols(adjacencyMatrix);
        // so first iteration is always run
        double resultDiff = 1;

        //
        // Matrix A row sum
        //
        // Stores the outbound degrees of all vertices
        //
        Buffer dOut = GRBCORE.createVector(GRAPHBLAS.longType(), nodeCount);

        // Difference:
        // Workaround as we expect a double matrix and not a boolean like in LAGraph, where reduce with PLUS can be used
        // to get the out degree based on double values (implicit conversion isnt going to work thats why via mxv)
        Buffer tmp_one_array = GRBCORE.createVector(GRAPHBLAS.longType(), nodeCount);
        GRAPHBLAS.assignVectorLong(tmp_one_array, null, null, 1, GrB_ALL, nodeCount, null);
        Buffer plusSecondSemiring = GRBCORE.createSemiring(GRBMONOID.plusMonoidLong(), GRAPHBLAS.secondBinaryOpLong());
        status = GRBOPSMAT.mxv(dOut, null, null, plusSecondSemiring, adjacencyMatrix, tmp_one_array, null);
        assert status == GRBCORE.GrB_SUCCESS;

        Buffer importanceVec = GRBCORE.createVector(GRAPHBLAS.doubleType(), nodeCount);
        Buffer danglingVec = GRBCORE.createVector(GRAPHBLAS.doubleType(), nodeCount);

        Buffer pr = GRBCORE.createVector(GRAPHBLAS.doubleType(), nodeCount);
        Buffer prevResult = GRBCORE.createVector(GRAPHBLAS.doubleType(), nodeCount);
        status = GRAPHBLAS.assignVectorDouble(pr, null, null, 1.0 / nodeCount, GrB_ALL, nodeCount, null);
        assert status == GRBCORE.GrB_SUCCESS;

        Buffer invertedMask = GRBCORE.createDescriptor();
        status = GRBCORE.setDescriptorValue(invertedMask, GRBCORE.GrB_MASK, GRBCORE.GrB_COMP);
        assert status == GRBCORE.GrB_SUCCESS;

        final double teleport = (1 - dampingFactor) / nodeCount;

        int iteration = 0;

        Buffer semiRing = GRBCORE.createSemiring(GRBMONOID.plusMonoidDouble(), GRAPHBLAS.timesBinaryOpDouble());

        for (; iteration < maxIterations && resultDiff > tolerance; iteration++) {
            // !Difference: in C would just swap prevResult and result
            GRBOPSVEC.assign(prevResult, null, GRAPHBLAS.secondBinaryOpDouble(), pr, GrB_ALL, nodeCount, null);

            //
            // Importance calculation
            //

            // Divide previous PageRank with number of outbound edges
            status = GRBOPSMAT.elemWiseMulIntersectBinOp(importanceVec, null, null, GRAPHBLAS.divBinaryOpDouble(), pr, dOut, null);
            assert status == GRBCORE.GrB_SUCCESS;

            // Multiply importance by damping factor
            status = GRAPHBLAS.assignVectorDouble(importanceVec, null, GRAPHBLAS.timesBinaryOpDouble(), dampingFactor, GrB_ALL, nodeCount, null);
            assert status == GRBCORE.GrB_SUCCESS;


            // Calculate total PR of all inbound vertices
            status = GRBOPSMAT.vxm(importanceVec, null, null, semiRing, importanceVec, adjacencyMatrix, null);
            assert status == GRBCORE.GrB_SUCCESS;

            //
            // Dangling calculation
            //

            // Extract all the dangling PR entries from the previous result
            status = GRBOPSVEC.extract(danglingVec, dOut, null, pr, GrB_ALL, nodeCount, invertedMask);
            assert status == GRBCORE.GrB_SUCCESS;

            // Sum the previous PR values of dangling vertices together
            double danglingSum = GRBALG.vectorReduceAllDouble(0.0, null, GRBMONOID.plusMonoidDouble(), danglingVec, null);

            // Multiply by damping factor and 1 / |V|
            danglingSum *= (dampingFactor / nodeCount);


            //
            // PageRank summarization
            // Add teleport, importanceVec, and danglingVec components together
            //
            status = GRAPHBLAS.assignVectorDouble(pr, null, null, (teleport + danglingSum), GrB_ALL, nodeCount, null);
            assert status == GRBCORE.GrB_SUCCESS;

            GRBOPSVEC.elemWiseAddUnionMonoid(pr, null, null, GRBMONOID.plusMonoidDouble(), pr, importanceVec, null);

            // Calculate result difference
            GRBOPSVEC.elemWiseAddUnionBinOp(prevResult, null, null, GRAPHBLAS.minusBinaryOpDouble(), prevResult, pr, null);
            GRBCORE.vectorApply(prevResult, null, null, GRAPHBLAS.absUnaryOpDouble(), prevResult, null);
            resultDiff = GRBALG.vectorReduceAllDouble(0.0, null, GRBMONOID.plusMonoidDouble(), prevResult, null);
        }

        double[] values = new double[Math.toIntExact(nodeCount)];
        long[] indices = new long[Math.toIntExact(nodeCount)];

        GRAPHBLAS.extractVectorTuplesDouble(pr, values, indices);

        GRBCORE.freeVector(dOut);
        GRBCORE.freeVector(tmp_one_array);
        GRBCORE.freeVector(importanceVec);
        GRBCORE.freeVector(danglingVec);
        GRBCORE.freeVector(pr);
        GRBCORE.freeVector(prevResult);
        GRBCORE.freeDescriptor(invertedMask);
        GRBCORE.freeSemiring(plusSecondSemiring);
        GRBCORE.freeSemiring(semiRing);

        return new PageRankResult(values, iteration);
    }
}
