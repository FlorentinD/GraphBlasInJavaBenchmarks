package org.github.florentind.graphalgos.pageRank;

import com.github.fabianmurariu.unsafe.*;

import java.nio.Buffer;

import static com.github.fabianmurariu.unsafe.GRBCORE.GrB_ALL;
import static org.github.florentind.core.grapblas_native.NativeHelper.checkStatusCode;

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
       checkStatusCode(GRBOPSMAT.mxv(dOut, null, null, plusSecondSemiring, adjacencyMatrix, tmp_one_array, null));

        Buffer importanceVec = GRBCORE.createVector(GRAPHBLAS.doubleType(), nodeCount);
        Buffer danglingVec = GRBCORE.createVector(GRAPHBLAS.doubleType(), nodeCount);

        Buffer pr = GRBCORE.createVector(GRAPHBLAS.doubleType(), nodeCount);
        Buffer prevResult = GRBCORE.createVector(GRAPHBLAS.doubleType(), nodeCount);
        checkStatusCode(GRAPHBLAS.assignVectorDouble(pr, null, null, 1.0 / nodeCount, GrB_ALL, nodeCount, null));

        Buffer invertedMask = GRBCORE.createDescriptor();
        checkStatusCode(GRBCORE.setDescriptorValue(invertedMask, GRBCORE.GrB_MASK, GRBCORE.GrB_COMP));

        final double teleport = (1 - dampingFactor) / nodeCount;

        int iteration = 0;

        Buffer plusFirstSemiring = GRBCORE.createSemiring(GRBMONOID.plusMonoidDouble(), GRAPHBLAS.firstBinaryOpDouble());

        for (; iteration < maxIterations && resultDiff > tolerance; iteration++) {
            // !Difference: in C would just swap prevResult and result
            GRBOPSVEC.assign(prevResult, null, GRAPHBLAS.secondBinaryOpDouble(), pr, GrB_ALL, nodeCount, null);

            //
            // Importance calculation
            //

            // Divide previous PageRank with number of outbound edges
            checkStatusCode(
                    GRBOPSMAT.elemWiseMulIntersectBinOp(
                            importanceVec, null, null, GRAPHBLAS.divBinaryOpDouble(),
                            pr, dOut, null)
            );

            // Multiply importance by damping factor
            checkStatusCode(
                    GRAPHBLAS.assignVectorDouble(
                            importanceVec, null, GRAPHBLAS.timesBinaryOpDouble(),
                            dampingFactor, GrB_ALL, nodeCount, null)
            );



            // Calculate total PR of all inbound vertices
            checkStatusCode(GRBOPSMAT.vxm(importanceVec, null, null, plusFirstSemiring, importanceVec, adjacencyMatrix, null));

            //
            // Dangling calculation
            //

            // Extract all the dangling PR entries from the previous result
            checkStatusCode(GRBOPSVEC.extract(danglingVec, dOut, null, pr, GrB_ALL, nodeCount, invertedMask));

            // Sum the previous PR values of dangling vertices together
            double danglingSum = GRBALG.vectorReduceAllDouble(0.0, null, GRBMONOID.plusMonoidDouble(), danglingVec, null);

            // Multiply by damping factor and 1 / |V|
            danglingSum *= (dampingFactor / nodeCount);


            //
            // PageRank summarization
            // Add teleport, importanceVec, and danglingVec components together
            //
            checkStatusCode(GRAPHBLAS.assignVectorDouble(pr, null, null, (teleport + danglingSum), GrB_ALL, nodeCount, null));

            checkStatusCode(
                    GRBOPSVEC.elemWiseAddUnionMonoid(pr, null, null, GRBMONOID.plusMonoidDouble(), pr, importanceVec, null));

            // Calculate result difference
            checkStatusCode(
                    GRBOPSVEC.elemWiseAddUnionBinOp(
                            prevResult, null, null, GRAPHBLAS.minusBinaryOpDouble(), prevResult, pr, null)
            );
            checkStatusCode(
                    GRBCORE.vectorApply(prevResult, null, null, GRAPHBLAS.absUnaryOpDouble(), prevResult, null)
            );
            resultDiff = GRBALG.vectorReduceAllDouble(0.0, null, GRBMONOID.plusMonoidDouble(), prevResult, null);
        }

        double[] values = new double[Math.toIntExact(nodeCount)];
        long[] indices = new long[Math.toIntExact(nodeCount)];

        checkStatusCode(GRAPHBLAS.extractVectorTuplesDouble(pr, values, indices));

        GRBCORE.freeVector(dOut);
        GRBCORE.freeVector(tmp_one_array);
        GRBCORE.freeVector(importanceVec);
        GRBCORE.freeVector(danglingVec);
        GRBCORE.freeVector(pr);
        GRBCORE.freeVector(prevResult);
        GRBCORE.freeDescriptor(invertedMask);
        GRBCORE.freeSemiring(plusSecondSemiring);
        GRBCORE.freeSemiring(plusFirstSemiring);

        return new PageRankResult(values, iteration);
    }


    // difference to unweighted:
    //  no trick for getting outdegree needed
    //  no division through outdegree but normalising weights
    public static PageRankResult computeWeighted(Buffer adjacencyMatrix, double dampingFactor, double tolerance, int maxIterations, int concurrency) {
        // assert adj-matrix to be in CSC
        assert GRBCORE.getFormat(adjacencyMatrix) == GRBCORE.GxB_BY_COL;
        GRBCORE.setGlobalInt(GRBCORE.GxB_NTHREADS, concurrency);

        long nodeCount = GRBCORE.ncols(adjacencyMatrix);
        // so first iteration is always run
        double resultDiff = 1;

        //
        // Matrix A row sum
        //
        // normalize weights
        // TODO: extract into helper function (to measure impact of normalization)
        //
        Buffer sumOutWeights = GRBCORE.createVector(GRAPHBLAS.doubleType(), nodeCount);
        // normalize weights
        checkStatusCode(GRBOPSMAT.matrixReduceBinOp(sumOutWeights, null, null, GRAPHBLAS.plusBinaryOpDouble(), adjacencyMatrix, null));
        // set diagonal matrix
        Buffer sumOutWeightsDia = GRBCORE.createMatrix(GRAPHBLAS.doubleType(), nodeCount, nodeCount);

        // Ideally could use eWiseMult vector + matrix (broadcast)
        // as sumMatrix / adjMatrix to scale rowwise .. actual operator needs to do prev-weight / weightSum
        checkStatusCode(
                GRBCORE.vectorApply(sumOutWeights, null, null, GRAPHBLAS.mulInvUnaryOpDouble(), sumOutWeights, null)
        );
        int sumCount = Math.toIntExact(GRBCORE.nvalsVector(sumOutWeights));
        double[] sums = new double[sumCount];
        long[] sumIndices = new long[sumCount];
        checkStatusCode(GRAPHBLAS.extractVectorTuplesDouble(sumOutWeights, sums, sumIndices));
        // TODO: better way to set diagonal in matrix based on a vector?
        checkStatusCode(
                GRAPHBLAS.buildMatrixFromTuplesDouble(sumOutWeightsDia, sumIndices,
                        sumIndices, sums, sumCount, GRAPHBLAS.firstBinaryOpDouble())
        );

        // any, as based diagonal only produces computation done per entry
        Buffer anyDivSemiRing = GRBCORE.createSemiring(GRBMONOID.anyMonoidDouble(), GRAPHBLAS.timesBinaryOpDouble());
        checkStatusCode(
                GRBOPSMAT.mxm(sumOutWeightsDia, null, null, anyDivSemiRing, sumOutWeightsDia, adjacencyMatrix, null));
        adjacencyMatrix = sumOutWeightsDia;

        Buffer importanceVec = GRBCORE.createVector(GRAPHBLAS.doubleType(), nodeCount);
        Buffer danglingVec = GRBCORE.createVector(GRAPHBLAS.doubleType(), nodeCount);

        Buffer pr = GRBCORE.createVector(GRAPHBLAS.doubleType(), nodeCount);
        Buffer prevResult = GRBCORE.createVector(GRAPHBLAS.doubleType(), nodeCount);
        checkStatusCode(GRAPHBLAS.assignVectorDouble(pr, null, null, 1.0 / nodeCount, GrB_ALL, nodeCount, null));

        Buffer invertedMask = GRBCORE.createDescriptor();
        checkStatusCode(GRBCORE.setDescriptorValue(invertedMask, GRBCORE.GrB_MASK, GRBCORE.GrB_COMP));

        final double teleport = (1 - dampingFactor) / nodeCount;

        int iteration = 0;

        Buffer plusTimesSemiring = GRBCORE.createSemiring(GRBMONOID.plusMonoidDouble(), GRAPHBLAS.timesBinaryOpDouble());

        for (; iteration < maxIterations && resultDiff > tolerance; iteration++) {
            // !Difference: in C would just swap prevResult and result
            checkStatusCode(
                    GRBOPSVEC.assign(prevResult, null, GRAPHBLAS.secondBinaryOpDouble(), pr, GrB_ALL, nodeCount, null)
            );

            //
            // Importance calculation
            //


            // unweighted would divide here by degree
            checkStatusCode(
                    GRBOPSVEC.assign(importanceVec, null, GRAPHBLAS.secondBinaryOpDouble(), pr, GrB_ALL, nodeCount, null)
            );

            // Multiply importance by damping factor
            checkStatusCode(
                    GRAPHBLAS.assignVectorDouble(importanceVec, null, GRAPHBLAS.timesBinaryOpDouble(), dampingFactor, GrB_ALL, nodeCount, null)
            );

            // Calculate total PR of all inbound vertices
            // using plusFirst, as nz adj. matrix values are always 1
            checkStatusCode(
                    GRBOPSMAT.vxm(importanceVec, null, null, plusTimesSemiring, importanceVec, adjacencyMatrix, null)
            );


            //
            // Dangling calculation
            //

            // Extract all the dangling PR entries from the previous result
            checkStatusCode(
                    GRBOPSVEC.extract(danglingVec, sumOutWeights, null, pr, GrB_ALL, nodeCount, invertedMask)
            );

            // Sum the previous PR values of dangling vertices together
            double danglingSum = GRBALG.vectorReduceAllDouble(0.0, null, GRBMONOID.plusMonoidDouble(), danglingVec, null);

            // Multiply by damping factor and 1 / |V|
            danglingSum *= (dampingFactor / nodeCount);


            //
            // PageRank summarization
            // Add teleport, importanceVec, and danglingVec components together
            //
            checkStatusCode(
                    GRAPHBLAS.assignVectorDouble(pr, null, null, (teleport + danglingSum), GrB_ALL, nodeCount, null)
            );

            checkStatusCode(
                    GRBOPSVEC.elemWiseAddUnionMonoid(pr, null, null, GRBMONOID.plusMonoidDouble(), pr, importanceVec, null)
            );

            // Calculate result difference
            checkStatusCode(
                GRBOPSVEC.elemWiseAddUnionBinOp(prevResult, null, null, GRAPHBLAS.minusBinaryOpDouble(), prevResult, pr, null)
            );

            checkStatusCode(
                    GRBCORE.vectorApply(prevResult, null, null, GRAPHBLAS.absUnaryOpDouble(), prevResult, null)
            );
            resultDiff = GRBALG.vectorReduceAllDouble(0.0, null, GRBMONOID.plusMonoidDouble(), prevResult, null);
        }

        double[] values = new double[Math.toIntExact(nodeCount)];
        long[] indices = new long[Math.toIntExact(nodeCount)];

        checkStatusCode(GRAPHBLAS.extractVectorTuplesDouble(pr, values, indices));

        GRBCORE.freeVector(sumOutWeights);
        GRBCORE.freeVector(importanceVec);
        GRBCORE.freeVector(danglingVec);
        GRBCORE.freeVector(pr);
        GRBCORE.freeVector(prevResult);
        GRBCORE.freeDescriptor(invertedMask);
        GRBCORE.freeSemiring(anyDivSemiRing);
        GRBCORE.freeSemiring(plusTimesSemiring);
        // contains the normalized matrix and can be freed
        GRBCORE.freeMatrix(sumOutWeightsDia);

        return new PageRankResult(values, iteration);
    }

}
