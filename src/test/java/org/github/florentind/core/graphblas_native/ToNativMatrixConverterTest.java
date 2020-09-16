package org.github.florentind.core.graphblas_native;

import com.github.fabianmurariu.unsafe.GRAPHBLAS;
import com.github.fabianmurariu.unsafe.GRBCORE;
import org.ejml.data.DMatrixSparseCSC;
import org.github.florentind.core.ejml.EjmlRelationships;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.github.florentind.core.grapblas_native.NativeMatrixToString;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.neo4j.graphalgo.beta.generator.RandomGraphGenerator;
import org.neo4j.graphalgo.beta.generator.RelationshipDistribution;
import org.neo4j.graphalgo.core.Aggregation;

import java.nio.Buffer;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ToNativMatrixConverterTest {

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void convertFromMatrix(boolean by_col) {
        GRBCORE.initNonBlocking();

        int nodeCount = 3;
        var ejmlMatrix = new DMatrixSparseCSC(nodeCount, nodeCount);
        ejmlMatrix.set(0, 0, 42);
        ejmlMatrix.set(2, 1, 1337);
        ejmlMatrix.set(1, 2, 3.0);


        Buffer nativeMatrix = ToNativeMatrixConverter.convert(ejmlMatrix, by_col);

        assertEquals(by_col, (GRBCORE.getFormat(nativeMatrix) == GRBCORE.GxB_BY_COL));

        System.out.println(NativeMatrixToString.doubleMatrixToString(nativeMatrix, nodeCount));
        assertEquals(ejmlMatrix.numCols, GRBCORE.ncols(nativeMatrix));
        assertEquals(ejmlMatrix.nz_length, GRBCORE.nvalsMatrix(nativeMatrix));
    }

    @Test
    void convertFromGraph() {
        GRBCORE.initNonBlocking();

        var graph = RandomGraphGenerator.builder()
                .nodeCount(10)
                .averageDegree(3)
                .aggregation(Aggregation.DEFAULT)
                .relationshipDistribution(RelationshipDistribution.POWER_LAW)
                .build()
                .generate();

        Buffer nativeMatrix = ToNativeMatrixConverter.convert(graph, true);

        assertEquals(graph.relationshipCount(), GRBCORE.nvalsMatrix(nativeMatrix));

        graph.forEachNode(node -> {
            graph.forEachRelationship(node, EjmlRelationships.DEFAULT_RELATIONSHIP_PROPERTY, (src, trg, weight) -> {
                assertEquals(weight, GRAPHBLAS.getMatrixElementDouble(nativeMatrix, src, trg)[0]);
                return true;
            });
            return true;
        });
    }
}
