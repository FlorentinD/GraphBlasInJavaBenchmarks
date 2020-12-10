# visualizing jmh benchmark results

# TODO generalize for other results (e.g. config dictionary and methods)

# read csv
import pandas as pd

# weighted source: results/weightedPageRank/weightedPageRankResult.csv
weighted = False
#orientation = "Natural"
orientation = "Undirected"

benchmarkResult = pd.read_csv(
    "results/{}.csv".format("weightedPageRank/weightedPageRankResultsServer" if weighted else "pageRank/pageRankResultsServer"))

benchmarkResult = benchmarkResult[benchmarkResult.orientation == orientation]

print(benchmarkResult.dtypes)
print(benchmarkResult.head(5))

benchmarkResult["Library"] = benchmarkResult.Benchmark.str.split(".").str[-1]

for (prev, replacement) in {"NodeWise": "-VertexWise", "Global": "-Global", "pregel": "gds-pregel", "jni": "java-native"}.items():
    benchmarkResult["Library"] = benchmarkResult["Library"].str.replace(prev, replacement)


benchmarkResult[["NodeCount", "Iterations"]] = benchmarkResult.nodeCountIterationCombinations.str.split(";", expand=True)
# convert strings 0.1M to 100_000
benchmarkResult["NodeCount"] = benchmarkResult.NodeCount.apply(lambda x: float(x[:-1]))
benchmarkResult["Iterations"] = benchmarkResult.Iterations.apply(lambda x: int(x))

nodeCountScalingResults = benchmarkResult[benchmarkResult["Iterations"] == 20].copy()
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
from benchmarkResults.helper import grouped_barplot, getUnit, libColors

# plot iteration scaling results
# Idea: x-axis = "iterations" , y-axis = score , hue = library, style = concurrency
# TODO: also show error?!


iterationsPlot = sns.lineplot(data=iterationScalingResult, x="Iterations", y="Score",
                              hue="Library", palette = libColors(), style="concurrency", markers=True)
iterationsPlot.set_ylabel("Runtime in {}".format(getUnit(iterationScalingResult)), fontsize=12)
iterationsPlot.set_yscale('log')
iterationsPlot.legend(bbox_to_anchor=(1, 1), loc='upper left', ncol=1)
outFile = "out/{}_{}_iterationScale.pdf".format("weightedPageRank" if weighted else "pageRank", orientation)
plt.tight_layout(pad=1)
plt.savefig(outFile, bbox_inches='tight')
plt.show()

fig, ax = plt.subplots()
nodeCountPlot = sns.lineplot(data=nodeCountScalingResults, x="NodeCount", y="Score",
                             hue="Library", palette = libColors(),style="concurrency", markers=True)
nodeCountPlot.set_ylabel("Runtime in {}".format(getUnit(nodeCountScalingResults)), fontsize=12)
nodeCountPlot.set_xlabel("Number of vertices x 10⁶", fontsize=12)
nodeCountPlot.legend(bbox_to_anchor=(1, 1), loc='upper left', ncol=1)
outFile = "out/{}_{}_nodeScale.pdf".format("weightedPageRank" if weighted else "pageRank", orientation)
plt.tight_layout(pad=1.5)
if not weighted:
    ax.get_legend().remove()

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
