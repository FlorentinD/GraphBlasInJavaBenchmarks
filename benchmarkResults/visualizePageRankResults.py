# visualizing jmh benchmark results

# TODO generalize for other results (e.g. config dictionary and methods)

# read csv
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from benchmarkResults.helper import getUnit, libColors

#orientation = "Natural"
orientation = "Undirected"

fig, axes = plt.subplots(1, 2, figsize=(6,3), sharey=True, sharex=True)

for id,weighted in enumerate([True, False]):
    benchmarkResult = pd.read_csv(
        "results/{}.csv".format("weightedPageRank/weightedPageRankResultsServer" if weighted else "pageRank/pageRankResultsServer"))

    benchmarkResult = benchmarkResult[benchmarkResult.orientation == orientation]
    benchmarkResult = benchmarkResult.rename(columns={"concurrency": "Concurrency"})

    print(benchmarkResult.dtypes)
    print(benchmarkResult.head(5))

    benchmarkResult["Library"] = benchmarkResult.Benchmark.str.split(".").str[-1]

    for (prev, replacement) in {"pregel": "GDS-Pregel", "jni": "Java-Native", "jGraphT": "JGraphT", "ejml": "EJML", "gds": "GDS"}.items():
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


    # Plot iteration scale (not working due to changes)

    # iterationsPlot = sns.lineplot(data=iterationScalingResult, x="Iterations", y="Score",
    #                               hue="Library", palette = libColors(), style="concurrency", markers=True)
    # iterationsPlot.set_ylabel("Runtime in {}".format(getUnit(iterationScalingResult)), fontsize=12)
    # iterationsPlot.set_yscale('log')
    # iterationsPlot.legend(bbox_to_anchor=(1, 1), loc='upper left', ncol=1)
    # outFile = "out/{}_{}_iterationScale.pdf".format("weightedPageRank" if weighted else "pageRank", orientation)
    # plt.tight_layout(pad=1)
    # plt.savefig(outFile, bbox_inches='tight')
    # plt.show()

    # Plot nodeCount scale

    nodeCountPlot = sns.lineplot(ax=axes[id], data=nodeCountScalingResults, x="NodeCount", y="Score",
                                 hue="Library", palette = libColors(),style="Concurrency", markers=True)
    nodeCountPlot.set_ylabel("Runtime in {}".format(getUnit(nodeCountScalingResults)), fontsize=12)
    nodeCountPlot.set_xlabel("Number of vertices x 10⁶", fontsize=12)
    variant = "Weighted" if weighted else "Unweighted"
    nodeCountPlot.set_title("{}-Variant".format(variant))
    nodeCountPlot.legend(bbox_to_anchor=(0.5, -0.3), loc='lower center', ncol=3, bbox_transform=fig.transFigure)
    if not weighted:
        axes[id].get_legend().remove()

outFile = "out/pageRanks_{}_nodeScale.pdf".format(orientation)
plt.savefig(outFile, bbox_inches='tight')
#nodeCountPlot.set_yscale('log')
plt.show()
