package org.github.florentind.core.grapblas_native;

import com.github.fabianmurariu.unsafe.GRBCORE;

public class NativeHelper {
    public static void checkStatusCode(long status) {
        if (status != GRBCORE.GrB_SUCCESS) {
            throw new IllegalStateException("Operation didn't succeed " + status);
        }
    }
}
