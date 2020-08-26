# visualizing jmh benchmark results


# read csv
import pandas as pd

benchmarkResult = pd.read_csv("bfsResults.txt")
print(benchmarkResult.dtypes)

benchmarkResult["Name"] = benchmarkResult.Benchmark.str.split(".").str[1]

# idea: could add a sparse/dense result column to the df

# TODO: regard
#       title maxIterations, mode, cnt, avg-degree
#       y-label units
#       ? error-rate

# create diagrams
#   x-axis = nodeCount
#   variant = (benchmarkName, concurrency)
#   also plot error ..
#   use unit column and to generate y-axis label

import seaborn as sns
import matplotlib.pyplot as plt

sns.barplot(x="nodeCount", y="Score", hue="Name", data=benchmarkResult, orient="v")
plt.savefig("bfs.jpg")
plt.show()
