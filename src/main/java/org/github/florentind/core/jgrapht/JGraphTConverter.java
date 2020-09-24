package org.github.florentind.core.jgrapht;

import org.jgrapht.Graph;
import org.jgrapht.alg.util.Pair;
import org.jgrapht.alg.util.Triple;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedGraph;
import org.jgrapht.opt.graph.sparse.SparseIntDirectedWeightedGraph;
import org.jgrapht.opt.graph.sparse.SparseIntUndirectedGraph;
import org.jgrapht.opt.graph.sparse.SparseIntUndirectedWeightedGraph;

import java.util.ArrayList;
import java.util.List;

import static org.github.florentind.core.ejml.EjmlRelationships.DEFAULT_RELATIONSHIP_PROPERTY;

public class JGraphTConverter {

    /**
     * based on https://arxiv.org/pdf/1904.08355.pdf section graph-backends
     */
    public static Graph convert(org.neo4j.graphalgo.api.Graph gdsGraph) {
        if (gdsGraph.hasRelationshipProperty()) {
            List<Triple<Integer, Integer, Double>> edges = new ArrayList<>();

            gdsGraph.forEachNode(id -> {
                gdsGraph.forEachRelationship(id, DEFAULT_RELATIONSHIP_PROPERTY, (src, trg, weight) -> {
                    if(!(gdsGraph.isUndirected() && src >= trg)) {
                        edges.add(new Triple<>(Math.toIntExact(src), Math.toIntExact(trg), weight));
                    }
                    return true;
                });
                return true;
            });

            if (gdsGraph.isUndirected()) {
                return new SparseIntUndirectedWeightedGraph(Math.toIntExact(gdsGraph.nodeCount()), edges);
            } else {
                return new SparseIntDirectedWeightedGraph(Math.toIntExact(gdsGraph.nodeCount()), edges);
            }
        }
        else {
            // unweighted case
            List<Pair<Integer, Integer>> edges = new ArrayList<>();
            gdsGraph.forEachNode(id -> {
                gdsGraph.forEachRelationship(id, (src, trg) -> {
                    if(!(gdsGraph.isUndirected() && src >= trg)) {
                        edges.add(new Pair<>(Math.toIntExact(src), Math.toIntExact(trg)));
                    }
                    return true;
                });
                return true;
            });

            if (gdsGraph.isUndirected()) {
                return new SparseIntUndirectedGraph(Math.toIntExact(gdsGraph.nodeCount()), edges);
            } else {
                return new SparseIntDirectedGraph(Math.toIntExact(gdsGraph.nodeCount()), edges);
            }
        }
    }
}
