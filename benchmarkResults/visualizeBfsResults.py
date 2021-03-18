# visualizing jmh benchmark results

# TODO generalize for other results (e.g. config dictionary and methods)

# read csv
import pandas as pd

benchmarkResult = pd.read_csv("results/bfs.csv", skipinitialspace=True)
print(benchmarkResult.dtypes)

benchmarkResult["name"] = benchmarkResult.benchmark

for (prev, replacement) in {"Bfs":"","Benchmark":"", "Parent":"Parent,", "Level":"Level,"}.items():
    benchmarkResult["name"] = benchmarkResult["name"].str.replace(prev, replacement)

benchmarkResult[["variant", "library"]] = benchmarkResult.name.str.split(",", expand=True)

for (prev, replacement) in {"Ejml": "EJML","Pregel": "GDS-Pregel", "Native": "Java-Native"}.items():
    benchmarkResult["library"] = benchmarkResult["library"].str.replace(prev, replacement)

print(benchmarkResult.head(5))

benchmarkResult = benchmarkResult[~benchmarkResult["dataset"].str.contains("Facebook")]
benchmarkResult = benchmarkResult[~benchmarkResult["dataset"].str.contains("LDBC01")]

gb = benchmarkResult.groupby('variant')
bfsVariants = [gb.get_group(x) for x in gb.groups]
#print([v.head(5) for v in bfsVariants])



import matplotlib.pyplot as plt
import seaborn as sns
from benchmarkResults.helper import libColors

allLibs = ["JGraphT", "GDS-Pregel", "EJML", "Java-Native"]
#fig, axes = plt.subplots(1, 2, figsize=(6,3), sharey=True, sharex=True)

for id, variant in enumerate(bfsVariants):
    fig, ax = plt.subplots(figsize=(16, 9))
    plt.yticks(fontsize=22)
    plt.xticks(fontsize=22)
    #plt.setp(ax.get_xticklabels(), rotation=30)

    containedLibs = variant.library.unique()
    hueOrder = [i for i in allLibs if i in containedLibs]
    order=["Slashdot0902", "POKEC", "Patents"]


    baselineDf = variant[variant["library"].str.contains("EJML")]
    baselineDf = baselineDf[["dataset", "median"]]
    baselineDf.rename(columns={"median":"baseline"}, inplace=True)
    baselinedVariant = pd.merge(variant, baselineDf, how="inner", on="dataset")
    baselinedVariant["slowdown"] = baselinedVariant["median"] / baselinedVariant["baseline"]

    singleThreadedDf = baselinedVariant[baselinedVariant["concurrency"] == 1]

    barPlot = sns.barplot(ax=ax, x="dataset", y="slowdown", hue="library", data=singleThreadedDf,
                          hue_order=hueOrder, palette = libColors(), order=order)
    barPlot.set_ylabel("Relative performance", fontsize=24)
    barPlot.set_xlabel("Dataset", fontsize=24)
    #barPlot.set_title("{}-Variant".format(singleThreadedDf['variant'].iloc[0]))
    barPlot.legend(bbox_to_anchor=(0.5, -0.1), loc='lower center', ncol=4, bbox_transform=fig.transFigure,
                   title_fontsize=24,fontsize=22)

    #plt.tight_layout(pad=1)
    #yscale = 'log'
#    barPlot.set_yscale(yscale)
#     if variant.variant.unique()[0] == "Parent":
#         ax[id].get_legend().remove()

    plt.savefig("out/bfs_{}.pdf".format(variant.variant.unique()[0]), bbox_inches='tight')
    plt.show()
