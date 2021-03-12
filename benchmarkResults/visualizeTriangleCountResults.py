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

    baselineDf = df[df["library"].str.contains("EJML")]
    baselineDf = baselineDf[["dataset", "median"]]
    baselineDf.rename(columns={"median": "baseline"}, inplace=True)
    baselinedVariant = pd.merge(df, baselineDf, how="inner", on="dataset")
    baselinedVariant["slowdown"] = baselinedVariant["median"] / baselinedVariant["baseline"]
    singleThreadedDf = baselinedVariant[baselinedVariant["concurrency"] == 1]


    allLibs = ["EJML", "Java-Native", "JGraphT", "GDS-Pregel"]
    order=["Facebook", "Slashdot0902", "POKEC", "Patents"]
    hue_order = [i for i in allLibs if i in singleThreadedDf.library.unique()]
    barPlot = sns.barplot(x="dataset", y="slowdown", hue="library", palette = libColors()
                          , data=singleThreadedDf, hue_order=hue_order, order=order)
    barPlot.set_ylabel("Slowdown", fontsize=12)
    barPlot.set_xlabel("Dataset", fontsize=12)
    barPlot.legend(bbox_to_anchor=(0.5, -0.4), loc='lower center', ncol=4, bbox_transform=fig.transFigure)

    if variant == "global":
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