package org.github.florentind.graphalgos.pageRank;

import java.nio.Buffer;

import static com.github.fabianmurariu.unsafe.GRAPHBLAS.*;
import static com.github.fabianmurariu.unsafe.GRBALG.vectorReduceAllDouble;
import static com.github.fabianmurariu.unsafe.GRBCORE.*;
import static com.github.fabianmurariu.unsafe.GRBMONOID.*;
import static com.github.fabianmurariu.unsafe.GRBOPSMAT.*;
import static com.github.fabianmurariu.unsafe.GRBOPSVEC.assign;
import static com.github.fabianmurariu.unsafe.GRBOPSVEC.extract;
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

        setGlobalInt(GxB_NTHREADS, concurrency);

        long nodeCount = ncols(adjacencyMatrix);
        // so first iteration is always run
        double resultDiff = 1;

        //
        // Matrix A row sum
        //
        // Stores the outbound degrees of all vertices
        //
        Buffer dOut = createVector(longType(), nodeCount);

        // Difference:
        // Workaround as we expect a double matrix and not a boolean like in LAGraph, where reduce with PLUS can be used
        // to get the out degree based on double values (implicit conversion isnt going to work thats why via mxv)
        Buffer tmp_one_array = createVector(longType(), nodeCount);
        assignVectorLong(tmp_one_array, null, null, 1, GrB_ALL, nodeCount, null);
        Buffer plusSecondSemiring = createSemiring(plusMonoidLong(), secondBinaryOpLong());
       checkStatusCode(mxv(dOut, null, null, plusSecondSemiring, adjacencyMatrix, tmp_one_array, null));

        Buffer importanceVec = createVector(doubleType(), nodeCount);
        Buffer danglingVec = createVector(doubleType(), nodeCount);

        Buffer pr = createVector(doubleType(), nodeCount);
        Buffer prevResult = createVector(doubleType(), nodeCount);
        checkStatusCode(assignVectorDouble(pr, null, null, 1.0 / nodeCount, GrB_ALL, nodeCount, null));

        Buffer invertedMask = createDescriptor();
        checkStatusCode(setDescriptorValue(invertedMask, GrB_MASK, GrB_COMP));
        checkStatusCode(setDescriptorValue(invertedMask, GrB_MASK, GrB_STRUCTURE));

        final double teleport = (1 - dampingFactor) / nodeCount;

        int iteration = 0;

        Buffer plusFirstSemiring = createSemiring(plusMonoidDouble(), firstBinaryOpDouble());

        for (; iteration < maxIterations && resultDiff > tolerance; iteration++) {
            // ?? !Difference: in C would just swap prevResult and result
            assign(prevResult, null, secondBinaryOpDouble(), pr, GrB_ALL, nodeCount, null);

            //
            // Importance calculation
            //

            // Divide previous PageRank with number of outbound edges
            checkStatusCode(
                    elemWiseMulIntersectBinOp(importanceVec, null, null, divBinaryOpDouble(), pr, dOut, null)
            );

            // Multiply importance by damping factor
            checkStatusCode(
                    assignVectorDouble(importanceVec, null, timesBinaryOpDouble(), dampingFactor, GrB_ALL, nodeCount, null)
            );



            // Calculate total PR of all inbound vertices
            checkStatusCode(vxm(importanceVec, null, null, plusFirstSemiring, importanceVec, adjacencyMatrix, null));

            //
            // Dangling calculation
            //

            // Extract all the dangling PR entries from the previous result
            checkStatusCode(extract(danglingVec, dOut, null, pr, GrB_ALL, nodeCount, invertedMask));

            // Sum the previous PR values of dangling vertices together
            double danglingSum = vectorReduceAllDouble(0.0, null, plusMonoidDouble(), danglingVec, null);

            // Multiply by damping factor and 1 / |V|
            danglingSum *= (dampingFactor / nodeCount);


            //
            // PageRank summarization
            // Add teleport, importanceVec, and danglingVec components together
            //
            checkStatusCode(assignVectorDouble(pr, null, null, (teleport + danglingSum), GrB_ALL, nodeCount, null));

            checkStatusCode(
                    elemWiseAddUnionMonoid(pr, null, null, plusMonoidDouble(), pr, importanceVec, null));

            // Calculate result difference
            checkStatusCode(elemWiseAddUnionBinOp(prevResult, null, null, minusBinaryOpDouble(), prevResult, pr, null));
            checkStatusCode(vectorApply(prevResult, null, null, absUnaryOpDouble(), prevResult, null));
            resultDiff = vectorReduceAllDouble(0.0, null, plusMonoidDouble(), prevResult, null);
        }

        double[] values = new double[Math.toIntExact(nodeCount)];
        long[] indices = new long[Math.toIntExact(nodeCount)];

        checkStatusCode(extractVectorTuplesDouble(pr, values, indices));

        freeVector(dOut);
        freeVector(tmp_one_array);
        freeVector(importanceVec);
        freeVector(danglingVec);
        freeVector(pr);
        freeVector(prevResult);
        freeDescriptor(invertedMask);
        freeSemiring(plusSecondSemiring);
        freeSemiring(plusFirstSemiring);

        return new PageRankResult(values, iteration);
    }


    // difference to unweighted:
    //  no trick for getting outdegree needed
    //  no division through outdegree but normalising weights
    public static PageRankResult computeWeighted(Buffer adjacencyMatrix, double dampingFactor, double tolerance, int maxIterations, int concurrency) {

        setGlobalInt(GxB_NTHREADS, concurrency);

        long nodeCount = ncols(adjacencyMatrix);
        // so first iteration is always run
        double resultDiff = 1;

        //
        // Matrix A row sum
        //
        // normalize weights
        // TODO: extract into helper function (to measure impact of normalization)
        //
        Buffer sumOutWeights = createVector(doubleType(), nodeCount);
        // normalize weights
        checkStatusCode(matrixReduceBinOp(sumOutWeights, null, null, plusBinaryOpDouble(), adjacencyMatrix, null));
        // set diagonal matrix
        Buffer sumOutWeightsDia = createMatrix(doubleType(), nodeCount, nodeCount);

        // Ideally could use eWiseMult vector + matrix (broadcast)
        // as sumMatrix / adjMatrix to scale rowwise .. actual operator needs to do prev-weight / weightSum
        checkStatusCode(vectorApply(sumOutWeights, null, null, mulInvUnaryOpDouble(), sumOutWeights, null));
        int sumCount = Math.toIntExact(nvalsVector(sumOutWeights));
        double[] sums = new double[sumCount];
        long[] sumIndices = new long[sumCount];
        checkStatusCode(extractVectorTuplesDouble(sumOutWeights, sums, sumIndices));
        // TODO: better way to set diagonal in matrix based on a vector?
        checkStatusCode(buildMatrixFromTuplesDouble(sumOutWeightsDia, sumIndices, sumIndices, sums, sumCount, firstBinaryOpDouble()));

        // any, as based diagonal only produces computation done per entry
        Buffer anyDivSemiRing = createSemiring(anyMonoidDouble(), timesBinaryOpDouble());
        checkStatusCode(
                mxm(sumOutWeightsDia, null, null, anyDivSemiRing, sumOutWeightsDia, adjacencyMatrix, null));
        adjacencyMatrix = sumOutWeightsDia;

        Buffer importanceVec = createVector(doubleType(), nodeCount);
        Buffer danglingVec = createVector(doubleType(), nodeCount);

        Buffer pr = createVector(doubleType(), nodeCount);
        Buffer prevResult = createVector(doubleType(), nodeCount);
        checkStatusCode(assignVectorDouble(pr, null, null, 1.0 / nodeCount, GrB_ALL, nodeCount, null));

        Buffer invertedMask = createDescriptor();
        checkStatusCode(setDescriptorValue(invertedMask, GrB_MASK, GrB_COMP));
        checkStatusCode(setDescriptorValue(invertedMask, GrB_MASK, GrB_STRUCTURE));

        final double teleport = (1 - dampingFactor) / nodeCount;

        int iteration = 0;

        Buffer plusTimesSemiring = createSemiring(plusMonoidDouble(), timesBinaryOpDouble());

        for (; iteration < maxIterations && resultDiff > tolerance; iteration++) {
            // !Difference: in C would just swap prevResult and result
            checkStatusCode(assign(prevResult, null, secondBinaryOpDouble(), pr, GrB_ALL, nodeCount, null));

            //
            // Importance calculation
            //


            // unweighted would divide here by degree
            checkStatusCode(assign(importanceVec, null, secondBinaryOpDouble(), pr, GrB_ALL, nodeCount, null));

            // Multiply importance by damping factor
            checkStatusCode(assignVectorDouble(importanceVec, null, timesBinaryOpDouble(), dampingFactor, GrB_ALL, nodeCount, null));

            // Calculate total PR of all inbound vertices
            // using plusFirst, as nz adj. matrix values are always 1
            checkStatusCode(
                    vxm(importanceVec, null, null, plusTimesSemiring, importanceVec, adjacencyMatrix, null)
            );


            //
            // Dangling calculation
            //

            // Extract all the dangling PR entries from the previous result
            checkStatusCode(extract(danglingVec, sumOutWeights, null, pr, GrB_ALL, nodeCount, invertedMask));

            // Sum the previous PR values of dangling vertices together
            double danglingSum = vectorReduceAllDouble(0.0, null, plusMonoidDouble(), danglingVec, null);

            // Multiply by damping factor and 1 / |V|
            danglingSum *= (dampingFactor / nodeCount);


            //
            // PageRank summarization
            // Add teleport, importanceVec, and danglingVec components together
            //
            checkStatusCode(assignVectorDouble(pr, null, null, (teleport + danglingSum), GrB_ALL, nodeCount, null));

            checkStatusCode(elemWiseAddUnionMonoid(pr, null, null, plusMonoidDouble(), pr, importanceVec, null));

            // Calculate result difference
            checkStatusCode(elemWiseAddUnionBinOp(prevResult, null, null, minusBinaryOpDouble(), prevResult, pr, null));
            checkStatusCode(vectorApply(prevResult, null, null, absUnaryOpDouble(), prevResult, null));
            resultDiff = vectorReduceAllDouble(0.0, null, plusMonoidDouble(), prevResult, null);
        }

        double[] values = new double[Math.toIntExact(nodeCount)];
        long[] indices = new long[Math.toIntExact(nodeCount)];

        vectorWait(pr);
        checkStatusCode(extractVectorTuplesDouble(pr, values, indices));

        freeVector(sumOutWeights);
        freeVector(importanceVec);
        freeVector(danglingVec);
        freeVector(pr);
        freeVector(prevResult);
        freeDescriptor(invertedMask);
        freeSemiring(anyDivSemiRing);
        freeSemiring(plusTimesSemiring);
        // contains the normalized matrix and can be freed
        freeMatrix(sumOutWeightsDia);

        return new PageRankResult(values, iteration);
    }

}
