import pandas as pd

benchmarkResult = pd.read_csv("results/triangleCount.csv", skipinitialspace=True)
print(benchmarkResult.dtypes)

for (prev, replacement) in {"Benchmark":"", "TriangleCount": "", "VertexWise": "VertexWise,", "Global": "Global,"}.items():
    benchmarkResult["benchmark"] = benchmarkResult["benchmark"].str.replace(prev, replacement)

for (prev, replacement) in {"Pregel": "GDS-Pregel", "Native": "Java-Native", "Ejml": "EJML"}.items():
    benchmarkResult["benchmark"] = benchmarkResult["benchmark"].str.replace(prev, replacement)

benchmarkResult[["variant","library"]] = benchmarkResult["benchmark"].str.split(",", expand=True)
print(benchmarkResult.head(5))


globalDF = benchmarkResult[benchmarkResult['variant'].str.contains("Global")]
vertexWiseDF = benchmarkResult[benchmarkResult['variant'].str.contains("VertexWise")]

tcDfs = {"global": globalDF, "vertexwise":vertexWiseDF}

import matplotlib.pyplot as plt
import seaborn as sns
from benchmarkResults.helper import libColors, getUnit

for (variant, df) in tcDfs.items():
    fig, ax = plt.subplots(figsize=(6,3))
    plt.setp(ax.get_xticklabels(), rotation=30)

    singleThreadedDf = df[df["concurrency"] == 1]
    barPlot = sns.barplot(x="dataset", y="median", hue="library", palette = libColors()
                          , data=singleThreadedDf)
    barPlot.set_ylabel("Runtime in ms", fontsize=12)
    barPlot.set_xlabel("Dataset", fontsize=12)
    barPlot.legend(bbox_to_anchor=(0.5, -0.4), loc='lower center', ncol=4, bbox_transform=fig.transFigure)
    barPlot.set_yscale('log')
    plt.savefig("out/triangleCount_{}.pdf".format(variant), bbox_inches='tight')
    plt.show()

    # todo multithreaded, show speedups



# fig, ax = plt.subplots()
#
# barplot = grouped_barplot(benchmarkResult, "nodeCount", "Name", "Score", "Error", ax)
# barplot.title(title)
# ax.set_yscale('log')
#