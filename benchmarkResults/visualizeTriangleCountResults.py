# visualizing jmh benchmark results

# TODO generalize for other results (e.g. config dictionary and methods)

# read csv
import pandas as pd

benchmarkResult = pd.read_csv("results/triangleCount/triangleCountResultServer.csv")
print(benchmarkResult.dtypes)

benchmarkResult["Library-Variant"] = benchmarkResult.Benchmark.str.split(".").str[-1]

for (prev, replacement) in {"NodeWise": "-VertexWise", "Global": "-Global", "pregel": "gds-pregel", "jni": "java-native"}.items():
    benchmarkResult["Library-Variant"] = benchmarkResult["Library-Variant"].str.replace(prev, replacement)

benchmarkResult["nodeCount"] = benchmarkResult.nodeCount / (10 ** 6)

print(benchmarkResult.head(5))


# create diagrams
#   x-axis = nodeCount
#   variant = (benchmarkName, concurrency)
#   also plot error ..
#   use unit column and to generate y-axis label

# import seaborn as sns
import matplotlib.pyplot as plt
import seaborn as sns
from benchmarkResults.helper import grouped_barplot, libColors, getUnit

# get meta info like units, mode, avg-degree ...
title = "TriangleCount \n on a undirected random power-law graph with an average degree of {} \n using the {} of {}".format(
        benchmarkResult.avgDegree.iloc[0],
        benchmarkResult.Mode.iloc[0], benchmarkResult.Cnt.iloc[0]
    )

# sns approach fails to easily plot pre-aggregated error
linePlot = sns.lineplot(x="nodeCount", y="Score", hue="Library-Variant", palette = libColors(), style="concurrency", data=benchmarkResult,
                        markers=True, legend="brief")
linePlot.set_ylabel("Runtime in {}".format(getUnit(benchmarkResult)), fontsize=12)
linePlot.set_xlabel("Number of vertices x 10⁶", fontsize=12)
linePlot.legend(bbox_to_anchor=(1, 1), loc='upper left', ncol=1)
linePlot.set_yscale('log')
plt.tight_layout(pad=1)
plt.savefig("out/triangleCount.pdf", bbox_inches='tight')
plt.show()

# linePlot = sns.lineplot(x="nodeCount", y="Score", hue="Library-Variant", palette = libColors(), style="concurrency",
#                         data=benchmarkResult[~benchmarkResult["Library-Variant"].str.startswith("jGraphT")],
#                         markers=True, legend="brief")
# linePlot.set_ylabel("Time in {}".format(benchmarkResult.Units.unique()[0]))
# linePlot.set_xlabel("NodeCount x 10⁶")
# linePlot.legend(bbox_to_anchor=(1, 1), loc='upper left', ncol=1)
# linePlot.set_yscale('linear')
# plt.ylim(0, 6000)
# plt.savefig("out/triangleCount_withoutJGraphT.jpg", bbox_inches='tight')
# plt.show()


# fig, ax = plt.subplots()
#
# barplot = grouped_barplot(benchmarkResult, "nodeCount", "Name", "Score", "Error", ax)
# barplot.title(title)
# ax.set_yscale('log')
#

