# visualizing jmh benchmark results

# TODO generalize for other results (e.g. config dictionary and methods)

# read csv
import pandas as pd

# weighted source: results/weightedPageRank/weightedPageRankResult.csv
weighted = True

benchmarkResult = pd.read_csv(
    "results/{}.csv".format("weightedPageRank/weightedPageRankResultsServer" if weighted else "pageRank/pageRankResultsServer"))

print(benchmarkResult.dtypes)
print(benchmarkResult.head(5))

benchmarkResult["Library"] = benchmarkResult.Benchmark.str.split(".").str[-1]
benchmarkResult["Name"] = "(" + benchmarkResult.Library + "," + benchmarkResult.concurrency.apply(
    str) + ")"

benchmarkResult[["NodeCount", "Iterations"]] = benchmarkResult.nodeCountIterationCombinations.str.split(";", expand=True)
# convert strings 0.1M to 100_000
benchmarkResult["NodeCount"] = benchmarkResult.NodeCount.apply(lambda x: float(x[:-1]))
benchmarkResult["Iterations"] = benchmarkResult.Iterations.apply(lambda x: int(x))

nodeCountScalingResults = benchmarkResult[benchmarkResult["Iterations"] == 20]
nodeCountScalingResults["Score"] = benchmarkResult["Score"] / 1000.0
nodeCountScalingResults["Units"] = "s/op"

iterationScalingResult = benchmarkResult[benchmarkResult["NodeCount"] == 1]

#assert(len(benchmarkResult) + len(benchmarkResult.Name.unique())  == len(nodeCountScalingResults) + len(iterationScalingResult))
print("{} entries for nodeCounts".format(len(nodeCountScalingResults)))
print("{} entries for iterations".format(len(iterationScalingResult)))


# create diagrams
#   x-axis = nodeCount
#   variant = (benchmarkName, concurrency)
#   also plot error ..
#   use unit column and to generate y-axis label

# import seaborn as sns
import matplotlib.pyplot as plt
import seaborn as sns
from benchmarkResults.helper import grouped_barplot

# plot iteration scaling results
# Idea: x-axis = "iterations" , y-axis = score , hue = library, style = concurrency
# TODO: also show error?!

iterationsPlot = sns.lineplot(data=iterationScalingResult, x="Iterations", y="Score", hue="Library", style="concurrency", markers=True)
iterationsPlot.set_ylabel("Time in {}".format(iterationScalingResult.Units.unique()[0]))
iterationsPlot.set_yscale('log')
outFile = "out/{}_iterationScale.jpg".format("weightedPageRank" if weighted else "pageRank")
plt.savefig(outFile, bbox_inches='tight')
plt.show()


nodeCountPlot = sns.lineplot(data=nodeCountScalingResults, x="NodeCount", y="Score", hue="Library", style="concurrency", markers=True)
nodeCountPlot.set_ylabel("Time in {}".format(nodeCountScalingResults.Units.unique()[0]))
nodeCountPlot.set_xlabel("NodeCount x 10⁶")
outFile = "out/{}_nodeScale.jpg".format("weightedPageRank" if weighted else "pageRank")
plt.savefig(outFile, bbox_inches='tight')
#nodeCountPlot.set_yscale('log')
plt.show()


# get meta info like units, mode, avg-degree ...
# title = "{} (damping: {}, maxIterations: {}) \n on a random power-law graph with an average degree of {} \n using the {} of {}".format(
#         "Weighted PageRank" if weighted else "PageRank",
#         benchmarkResult.dampingFactor.iloc[0],
#         benchmarkResult.maxIterations.iloc[0],
#         benchmarkResult.avgDegree.iloc[0],
#         benchmarkResult.Mode.iloc[0], benchmarkResult.Cnt.iloc[0]
#     )
#
# fig, ax = plt.subplots()
#
# barplot = grouped_barplot(benchmarkResult, "nodeCount", "Name", "Score", "Error", ax)
# barplot.title(title)
#
# #outFile = "out/{}.jpg".format("weightedPageRank" if weighted else "pageRank")
# #plt.savefig(outFile, bbox_inches='tight')
# plt.show()
