# Saving graphs using EJMLs sparse matrices

## Differences to other formats

* it stores uncompressed values (f.i. allowing good in-place modifications)
* there are only double and float matrices -> also adjacency matrix uses ints instead of longs (double values are either rel weights or 1)
* adjacency lists are stored in a sparse matrix in Compressed Sparse Column format (instead of CSR)
* limited by primitive arrays and thus integer based array access -> graphs cannot exceed a node count of 2.1 * 10^9
* multi graphs are limited to only one edge per node pair for each relationship type (need to be aggregated)

* Union-Graphs can be created by `elementWiseAdd` of the matrices
* Intersections can be achieved via `elementWiseMult()` 



## Idea

* in-memory graph store format
* using DMatrixSparseCSC for storing relationship properties and adj. lists per rel type instead `org.neo4j.graphalgo.api.Relationships.Topology`
* as mainly incoming relationships are accessed, we save a transposed adjacency matrix (as csc is indexed by column)
* nodeProperties stored as in `org.neo4j.graphalgo.core.loading.CSRGraphStore` 
    * an option to get a primitive array representation might be good for node filtering ops .. but for now out of scope)
* for matrix creations use DMatrixSparseTriplet and then convert to DMatrixSparseCSC

* disregarding node-filters for the moment