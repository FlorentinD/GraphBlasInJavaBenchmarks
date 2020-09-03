# visualizing jmh benchmark results

# TODO generalize for other results (e.g. config dictionary and methods)

# read csv
import pandas as pd

benchmarkResult = pd.read_csv("pageRankResults.csv")
print(benchmarkResult.dtypes)

benchmarkResult["Name"] = benchmarkResult.Benchmark.str.split(".").str[2]
benchmarkResult["Name"] = "(" + benchmarkResult.Name.str.split("P").str[0] + "," + benchmarkResult.concurrency.apply(
    str) + ")"

print(benchmarkResult.head(5))


# create diagrams
#   x-axis = nodeCount
#   variant = (benchmarkName, concurrency)
#   also plot error ..
#   use unit column and to generate y-axis label

# import seaborn as sns
import matplotlib.pyplot as plt
from benchmarkResults.helper import grouped_barplot

# get meta info like units, mode, avg-degree ...
# TODO: get actual run iterations somehow as an info (know for bfs ca. 37 for 300k nodes graph
title = "PageRank with a random power-law graph \n with an average degree of {} using the {} of {}".format(
        benchmarkResult.avgDegree.iloc[0],
        benchmarkResult.Mode.iloc[0], benchmarkResult.Cnt.iloc[0]
    )

fig, ax = plt.subplots()

barplot = grouped_barplot(benchmarkResult, "nodeCount", "Name", "Score", "Error", ax)
barplot.title(title)

plt.savefig("pageRank.jpg")
plt.show()
