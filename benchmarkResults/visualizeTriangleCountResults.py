# visualizing jmh benchmark results

# TODO generalize for other results (e.g. config dictionary and methods)

# read csv
import pandas as pd

benchmarkResult = pd.read_csv("results/triangleCount/triangleCountResultServer.csv")
print(benchmarkResult.dtypes)

benchmarkResult["Library-Variant"] = benchmarkResult.Benchmark.str.split(".").str[-1]
benchmarkResult = benchmarkResult.rename(columns={"concurrency": "Concurrency"})
benchmarkResult["Library-Variant"] = benchmarkResult["Library-Variant"].str.title()

for (prev, replacement) in {"nodewise": "-VertexWise", "global": "-Global", "Pregel": "GDS-Pregel", "Jni": "Java-Native", "Jgrapht": "JGraphT", "Ejml": "EJML", "Gds": "GDS"}.items():
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
fig, ax = plt.subplots(figsize=(6,3))
linePlot = sns.lineplot(x="nodeCount", y="Score", hue="Library-Variant", palette = libColors(), style="Concurrency", data=benchmarkResult,
                        markers=True, legend="brief")
linePlot.set_ylabel("Runtime in {}".format(getUnit(benchmarkResult)), fontsize=12)
linePlot.set_xlabel("Number of vertices x 10⁶", fontsize=12)
linePlot.legend(bbox_to_anchor=(0.5, -0.4), loc='lower center', ncol=3, bbox_transform=fig.transFigure)
linePlot.set_yscale('log')
#plt.tight_layout(pad=1)
plt.savefig("out/triangleCount.pdf", bbox_inches='tight')
plt.show()

# fig, ax = plt.subplots()
#
# barplot = grouped_barplot(benchmarkResult, "nodeCount", "Name", "Score", "Error", ax)
# barplot.title(title)
# ax.set_yscale('log')
#

