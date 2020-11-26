# Evaluation

- theoretical (e.g. result expectation) and practical analysis (results)

## Extended matrix operations

* focus on sparse operations

* benchmark parameters:
    * 1 warm-up fork, 2 measurement forks
    * 2 warm-up iterations per fork
    * 5 iterations per fork (hence avg of 10?)
    
* ? jvm flag +-SuperWords (seeing if its vectorized ... f.i. SuiteSparse does no vectorization)

### Semirings 
* 2 operations:  
    * reduceColumnWise (nearly zero structural overhead) -- include: ".*MxMWithSemiring.*"
    * mxm (same matrix) -- include: ".*ReduceColumnWiseWithSemiring.*"
* matrix: random matrices in CSC with uniform distribution and dimension (0.1M, 0.5M, 1M) 
* 4 semirings: arithmetic (+, *), boolean (or, and), (or, pair) expected to be fastest, (min, max)
* compare against: prev. ejml version (hard-coded arithmetic semiring) and native version (suite-sparse)

### Masks
* operations: 
   * reduceColumnWise
   * mxm 
* fixed matrix size of 0.1Mx0.1M for mxm , 0.5Mx0.5M for reduceColumnWise
* scale: 
   * number of set entries in the mask (5/50 per column in mask)
   * negated/non-negated mask (-> f.i. numCol-5/50 are now set --> should be much faster)
   * structural/value mask (expecting the structural to be faster)
* compare against: native version (suite-sparse)


### ? vxm vs mxv ?
<!-- ** TODO  e.g. clean-up benchmarks ** -->
* would be just to underline reasoning taken from push/pull paper
* fixed semiring of plus-times, no mask
* expect for sparse vector mxv to be faster
* for dense vectors vxm should be faster


### Loading graphs into matrices
* Graph/Matrix creation based on existing graph: Graph from GDS to matrix (native vs ejml)
    * graph: weighted/unweighted (native can save as boolean in unweighted)
    * scale edges .. fixed vertexCount of 1M and avgDegree of 2/4/8 
    * ejml (insert into triple format -> convert to CSC format)
    * native (batch insert/"buildMatrixFromTuples") ... expecting jni overhead (moving arrays into C, other reason: buildMatrix handles duplicates)
    * ? JGraphT (would be just out of curiosity as also based on CSR matrices)

## Graph-Algorithms

- Graphs: generated via random graph generator in GDS 
    - handle multiple relationships per (src, trg) by reducing to one

### BFS
* compare: ejml, GDS-Pregel, native (SuiteSparse) and JGraphT (only level variant)

* scale: 
    * graph-size (0.1M, 0.5,1M, 5M nodes)
    * using an undirected graph as the POWER-LAW graph would be highly disconnected otherwise (high chance to have a node with degree of 0)
    * degree-distribution: POWER_LAW

* For GraphBLAS ... also add transposing the adjacency overhead (needed for CSC) 

* 2 variants: 
    * LEVEL (here 3 ejml versions (queue vector, result vector): sparse-sparse, sparse-dense, dense-dense)
    * PARENT

### PageRank
* all libs implement this (JGraphT, GDS, native (SuiteSparse), GDS-Pregel, Ejml)

* fixed: damping-factor = 0.85, tolerance = 1e-32 (so low that it doesn't terminate earlier)

* scale: 
    * graph-size (0.1M, 0.5,1M, 5M nodes)
    * maxIterations (5, 10, 20) with fixed 1M nodes

* 2 version: unweighted, weighted


### Triangle-Count
* JGraphT (only global), GDS/GDS-Pregel (always computed nodeWise), native (SuiteSparse) and ejml (both)

* scale: 
    * graph-size (10K, 0.1M, 0.5M, 1M) ... except JGraphT where 0.1M already takes 3 minutes, e.g. timeout for larger graphs

* 2 versions: global, nodeWise 
 


