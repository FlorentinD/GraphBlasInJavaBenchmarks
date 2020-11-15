package org.github.florentind.core.graphblas_native;

import com.github.fabianmurariu.unsafe.GRAPHBLAS;
import com.github.fabianmurariu.unsafe.GRBCORE;
import org.ejml.data.DMatrixSparseCSC;
import org.github.florentind.core.ejml.EjmlRelationships;
import org.github.florentind.core.grapblas_native.NativeMatrixToString;
import org.github.florentind.core.grapblas_native.ToNativeMatrixConverter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.neo4j.graphalgo.beta.generator.PropertyProducer;
import org.neo4j.graphalgo.beta.generator.RandomGraphGenerator;
import org.neo4j.graphalgo.beta.generator.RelationshipDistribution;
import org.neo4j.graphalgo.core.Aggregation;

import java.nio.Buffer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ToNativMatrixConverterTest {

    @BeforeAll
    static void setup() {
        GRBCORE.initNonBlocking();
    }

    private static Stream<Arguments> matrixFormatAndWeighted() {
        Stream.Builder<Arguments> builder = Stream.builder();
        boolean[] values = {true, false};
        for (int isCsc = 0; isCsc < 2; isCsc++) {
            for (int weighted = 0; weighted < 2; weighted++) {
                builder.accept(Arguments.of(values[isCsc], values[weighted]));
            }
        }
        return builder.build();
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void convertFromMatrix(boolean by_col) {
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

    @ParameterizedTest
    @MethodSource("matrixFormatAndWeighted")
    void convertFromGraph(boolean by_col, boolean weighted) {
        var graphBuilder = RandomGraphGenerator.builder()
                .nodeCount(10)
                .averageDegree(3)
                .aggregation(Aggregation.SINGLE)
                .relationshipDistribution(RelationshipDistribution.POWER_LAW);

        if (weighted) {
            graphBuilder.relationshipPropertyProducer(PropertyProducer.fixed("weight", 42.42));
        }

        var graph = graphBuilder
                .build()
                .generate();

        Buffer nativeMatrix = ToNativeMatrixConverter.convert(graph, by_col);

        assertEquals(graph.relationshipCount(), GRBCORE.nvalsMatrix(nativeMatrix));

        graph.forEachNode(node -> {
            graph.forEachRelationship(node, EjmlRelationships.DEFAULT_RELATIONSHIP_PROPERTY, (src, trg, weight) -> {
                assertEquals(weight, GRAPHBLAS.getMatrixElementDouble(nativeMatrix, src, trg)[0]);
                return true;
            });
            return true;
        });
    }

    @ParameterizedTest
    @MethodSource("matrixFormatAndWeighted")
    void convertEdgeWiseFromGraph(boolean by_col, boolean weighted) {
        var graphBuilder = RandomGraphGenerator.builder()
                .nodeCount(10)
                .averageDegree(3)
                .aggregation(Aggregation.SINGLE)
                .relationshipDistribution(RelationshipDistribution.POWER_LAW);

        if (weighted) {
            graphBuilder.relationshipPropertyProducer(PropertyProducer.fixed("weight", 42.42));
        }

        var graph = graphBuilder
                .build()
                .generate();

        Buffer nativeMatrix = ToNativeMatrixConverter.convertEdgeWise(graph, by_col);

        assertEquals(graph.relationshipCount(), GRBCORE.nvalsMatrix(nativeMatrix));

        graph.forEachNode(node -> {
            graph.forEachRelationship(node, EjmlRelationships.DEFAULT_RELATIONSHIP_PROPERTY, (src, trg, weight) -> {
                assertEquals(weight, GRAPHBLAS.getMatrixElementDouble(nativeMatrix, src, trg)[0]);
                return true;
            });
            return true;
        });
    }

    @AfterAll
    static void teardown() {
        GRBCORE.grbFinalize();
    }
}
