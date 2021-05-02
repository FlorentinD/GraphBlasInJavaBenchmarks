#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include "GraphBLAS.h"

void check_status(GrB_Info status)
{
    if (!(status == GrB_SUCCESS || status == GrB_NO_VALUE))
    {
        fprintf(stderr, "GraphBLAS error: %d \n", status);
    }
}

int compare_doubles(const void *a, const void *b)
{
    double *x = (double *)a;
    double *y = (double *)b;
    if (*x < *y)
        return -1;
    else if (*x > *y)
        return 1;
    return 0;
}

// load an unweighted matrix
// expected firstline: nodeCount relCount
GrB_Matrix *load_csv(FILE *fp)
{
    GrB_Matrix *adj_matrix;
    GrB_Index nodeCount, relCount, src, trg;

    char *line;
    size_t len = 0;

    getline(&line, &len, fp);
    sscanf(line, "%" SCNu64 " %" SCNu64, &nodeCount, &relCount);
    printf("nodes: %ld, rels: %ld \n", nodeCount, relCount);

    check_status(GrB_Matrix_new(adj_matrix, GrB_BOOL, nodeCount, nodeCount));

    for (int64_t readLines = 0; readLines < relCount; readLines++)
    {
        getline(&line, &len, fp);
        sscanf(line, "%" SCNu64 " %" SCNu64, &src, &trg);
        check_status(GrB_Matrix_setElement_BOOL(*adj_matrix, true, src, trg));
    }

    free(line);

    check_status(GrB_Matrix_nvals(&relCount, *adj_matrix));
    printf("loaded %ld rels \n", relCount);

    return adj_matrix;
}

int64_t computeTotalSandia(const GrB_Matrix A)
{
    GrB_Matrix L;
    GrB_Matrix C;
    GrB_Index nodeCount;
    GrB_Semiring plusPairSemiring = GxB_PLUS_PAIR_INT64;
    GrB_Monoid monoid = GrB_PLUS_MONOID_INT64;
    int64_t triangles;

    // compute lower triangle
    check_status(GrB_Matrix_nrows(&nodeCount, A));
    // FIX thrown segmentation fault ...
    check_status(GrB_Matrix_new(&L, GrB_BOOL, nodeCount, nodeCount));
    if (L == NULL)
        return -1;
    check_status(GxB_select(L, NULL, NULL, GxB_TRIL, A, NULL, NULL));

    check_status(GrB_Matrix_new(&C, GrB_INT64, nodeCount, nodeCount));
    check_status(GrB_mxm(C, L, NULL, plusPairSemiring, L, L, NULL));
    check_status(GrB_reduce(&triangles, NULL, monoid, C, NULL));

    check_status(GrB_Matrix_free(&L));
    check_status(GrB_Matrix_free(&C));

    return triangles;
}

void benchmarkGlobalTc(const GrB_Matrix adj)
{
    // same as in java benchmarks
    size_t warm_ups = 5;
    size_t iterations = 10;

    double durations[iterations];

    for (size_t i = 0; i < warm_ups; i++)
    {
        printf("Warmup %ld/%ld \n", i, warm_ups);
        computeTotalSandia(adj);
    }

    for (size_t i = 0; i < iterations; i++)
    {
        clock_t start = clock();
        computeTotalSandia(adj);
        clock_t end = clock();
        double duration = (double)(end - start) * 1000.0 / CLOCKS_PER_SEC;
        printf("Iteration: %ld, Runtime: %f ms\n", i, duration);
        durations[i] = duration;
    }

    qsort(durations, iterations, sizeof(*durations), compare_doubles);

    printf("Median: %f \n", durations[iterations/2]);
}

int main(int argc, char **argv)
{
    unsigned int version;
    unsigned int subversion;
    GrB_getVersion(&version, &subversion);
    printf("GraphBLAS C-API: %d.%d\n", version, subversion);

    GrB_init(GrB_NONBLOCKING);

    // EJML only supports CSC matrices
    GxB_Global_Option_set(GxB_FORMAT, GxB_BY_COL);

    // only single-threaded considered here
    GxB_Global_Option_set(GxB_NTHREADS, 1);

    FILE *fp;
    char *file;
    if (argc == 1)
    {
        file = "../datasets/facebook_undirected.csv";
    }
    else
    {
        file = argv[1];
    }
    fp = fopen(file, "r");

    if (fp == NULL)
    {
        perror("Error while opening the file.\n");
        exit(EXIT_FAILURE);
    }

    printf("Load graph at %s\n", file);
    GrB_Matrix *adj = load_csv(fp);

    benchmarkGlobalTc(*adj);

    GrB_Matrix_free(adj);
    GrB_finalize();
}