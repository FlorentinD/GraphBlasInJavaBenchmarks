
package org.github.florentind.bench.ejmlOps.semiring;

import org.ejml.data.DMatrixSparseCSC;
import org.ejml.data.DVectorSparse;
import org.ejml.ops.DSemiRings;
import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.mult.MatrixSparseVectorMultWithSemiRing_DSCC;
import org.ejml.sparse.csc.mult.MatrixVectorMult_DSCC;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.infra.Blackhole;


public class MxVMultBenchmark extends MxVMultBaseBenchmark {
    DVectorSparse sparseInputVector;
    DMatrixSparseCSC sparse1DimMatrix;

    @Override
    @Setup
    public void setup() throws Throwable {
        super.setup();
        sparseInputVector = new DVectorSparse(denseInputVector, 0);
        sparse1DimMatrix = CommonOps_DSCC.transpose(sparseInputVector.oneDimMatrix, null, null);
    }

//    @Benchmark
//    public void mxv(Blackhole bh) {
//        MatrixVectorMult_DSCC.mult(matrix, denseInputVector, 0, output, 0);
//    }

    @Benchmark
    public void vxm(Blackhole bh) {
        MatrixVectorMult_DSCC.mult(denseInputVector, 0, matrix, output, 0);
    }

    @Benchmark
    public void sparse_vxm(Blackhole bh) {
        bh.consume(MatrixSparseVectorMultWithSemiRing_DSCC.mult(sparseInputVector, matrix, null, DSemiRings.PLUS_TIMES));
    }

    @Benchmark
    public void sparse_1rowMatxm(Blackhole bh) {
        bh.consume(CommonOps_DSCC.mult(sparse1DimMatrix, matrix, null));
    }
}
