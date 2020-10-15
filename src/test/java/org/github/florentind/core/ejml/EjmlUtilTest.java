package org.github.florentind.core.ejml;

import org.ejml.sparse.csc.CommonOps_DSCC;
import org.ejml.sparse.csc.RandomMatrices_DSCC;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class EjmlUtilTest {

    @Test
    void normalize() {
        var matrix = RandomMatrices_DSCC.generateUniform(10, 10, 5, 0, 5, new Random(42));

        EjmlUtil.normalizeColumnWise(matrix);

        var actualColumSums = CommonOps_DSCC.reduceColumnWise(matrix, 0, Double::sum, null).data;

        for (double actualColumSum : actualColumSums) {
            assertTrue((Math.round(actualColumSum * 100) - 100 == 0) || actualColumSum == 0, "value was " + actualColumSum);
        }
    }
}
