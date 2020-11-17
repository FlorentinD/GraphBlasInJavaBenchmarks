# Evaluation

## Extended matrix operations

* focus on sparse operations

### Semirings 
* 2 operations:  
    * reduceColumnWise
    * mxm (same matrix)
* matrix: random matrices with uniform distribution and dimension (0.1M, 0.5M, 1M) 
* 4 semirings: arithmetic (+, *), boolean (or, and), (or, pair) expected to be fastest, (min, max)
* compare against: prev. ejml version (hard-coded arithmetic semiring) and native version (suite-sparse)

### Masks
* operations: 
   * reduceColumnWise
   * mxm 
* fixed matrix size of 0.1Mx0.1M for mxm , 0.5Mx0.5M for reduceColumnWise
* scale: 
   * number of set entries in the mask (5/50 per column in mask)
   * negated/non-negated mask (-> f.i. numCol-2 are now set)
   * structural/value mask 
* compare against: native version (suite-sparse)


### ? vxm vs mxv ?
<!-- ** TODO  e.g. clean-up benchmarks ** -->
* would be just to underline assumption taken from push/pull paper
* fixed semiring of plus-times, no mask
* expect for sparse vector mxv to be faster
* for dense vectors vxm should be faster


### Loading graphs into matrices   
* Graph/Matrix creation based on existing graph: Graph from GDS to matrix (native vs ejml)
    * weighted graph
    * scale edges .. fixed vertexCount of 1M and avgDegree of 2/4/8 
    * ejml (insert into triple format -> convert to CSC format)
    * native (batch insert) ... expecting jni overhead
    

## Graph-Algorithms

- Graphs: generated via graph generator in GDS 
    - handle multiple relationships per (src, trg) by reducing to one

### BFS
* compare: ejml, GDS-Pregel and native (SuiteSparse) as other libraries do not implement this general case

* scale: 
    * graph-size (0.1M, 0.5,1M, 5M nodes)
    * using an undirected graph as otherwise the POWER-LAW graph would be highly disconnected (high chance to have a node with degree of 0)
    * degree-distribution: POWER_LAW, UNIFORM 

* For GraphBLAS ... also add transposing the adjacency overhead (needed for CSC) 

* 2 variants: 
    * LEVEL (here 3 ejml versions (queue vector, result vector): sparse-sparse, sparse-dense, dense-dense)
    * PARENT
    
* results divided by ran iterations (power-law graph f.i. 7 iterations vs uniform graph needs 42 iterations)

### PageRank
* other libs JGraphT, GDS, native (SuiteSparse), GDS-Pregel

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
 


