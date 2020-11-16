# Evaluation

## Extended operations

* focus on sparse operations
* semirings 
    * 2 operations:  
        * reduceColumnWise (simple operation .. just `+` operator of semiring) **TODO**
        * mxm (harder operation)
    * matrices: 2 random matrices with uniform distribution and dimension (0.1M, 0.5M, 1M) 
    * 4 semirings: arithmetic (+, *), boolean (or, and), (or, pair) expected to be fastest, (min, max)
    * compare against: prev. ejml version (hard-coded arithmetic semiring) and native version (suite-sparse)

**TODO**
* mask:
    * operations: reduceColumnWise (low overhead in general), mxm (high overhead if most entries are set)
    * fixed matrix of 10^6x10^6 with avg of 4 entries per column?  
    * scale: number of set entries in the mask
    * compare against: native version (suite-sparse) as prev. ejml had no masks

** TODO  e.g. clean-up ** 
* ? sparse vxm vs dense mxv (would be only to show BFS explanation is coherent?!)
    * fixed semiring, no mask
    * expect for sparse vector mxv to be faster
    * for dense vectors vxm should be faster
   
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
 


