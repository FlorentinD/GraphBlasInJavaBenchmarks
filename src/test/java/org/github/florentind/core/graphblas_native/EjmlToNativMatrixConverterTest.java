package org.github.florentind.core.graphblas_native;

import com.github.fabianmurariu.unsafe.GRBCORE;
import org.ejml.data.DMatrixSparseCSC;
import org.github.florentind.core.grapblas_native.EjmlToNativeMatrixConverter;
import org.github.florentind.core.grapblas_native.NativeMatrixToString;
import org.junit.jupiter.api.Test;

import java.nio.Buffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EjmlToNativMatrixConverterTest {

    @Test
    void convert() {
        // TODO: find out, why this is essential (otherwise Panic-Error e.g. non-recoverable)
        GRBCORE.initNonBlocking();

        int nodeCount = 3;
        var ejmlMatrix = new DMatrixSparseCSC(nodeCount, nodeCount);
        ejmlMatrix.set(0, 0, 42);
        ejmlMatrix.set(2, 1, 1337);
        ejmlMatrix.set(1, 2, 3.0);


        Buffer nativeMatrix = EjmlToNativeMatrixConverter.convert(ejmlMatrix);

        System.out.println(NativeMatrixToString.doubleMatrixToString(nativeMatrix, nodeCount));
        assertEquals(ejmlMatrix.numCols, GRBCORE.ncols(nativeMatrix));
        assertEquals(ejmlMatrix.nz_length, GRBCORE.nvalsMatrix(nativeMatrix));
    }
}
