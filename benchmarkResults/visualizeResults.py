# visualizing jmh benchmark results


# read csv
import pandas as pd

benchmarkResult = pd.read_csv("bfsResults.txt")

print(benchmarkResult.dtypes)


# create diagrams
#   x-axis = nodeCount
#   variant = (benchmarkName, concurrency)
#   also plot error ..
#   use unit column and to generate y-axis label
