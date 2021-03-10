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

gb = benchmarkResult.groupby('variant')
bfsVariants = [gb.get_group(x) for x in gb.groups]
#print([v.head(5) for v in bfsVariants])



import matplotlib.pyplot as plt
import seaborn as sns
from benchmarkResults.helper import libColors

allLibs = ["JGraphT", "GDS-Pregel", "EJML", "Java-Native"]
fig, axes = plt.subplots(1, 2, figsize=(6,3), sharey=True, sharex=True)

for ax in axes:
    plt.setp(ax.get_xticklabels(), rotation=30)

for id, variant in enumerate(bfsVariants):
    # get meta info like units, mode, avg-degree ...
    containedLibs = variant.library.unique()
    hueOrder = [i for i in allLibs if i in containedLibs]

    # TODO: multithreaded variant important?
    singleThreadedDf = variant[variant["concurrency"] == 1]

    baselineDf = singleThreadedDf[singleThreadedDf["library"].str.contains("EJML")]
    for dataset in variant.dataset.unique():
        ejmlBaseLine = baselineDf[baselineDf["dataset"] == dataset].iloc[0]["median"]
        datasetDf = singleThreadedDf[singleThreadedDf["dataset"] == dataset]
        # TODO get speed up into singleThreadedDf (also this is slowdown)
        datasetDf["speedup"] = datasetDf["median"] / ejmlBaseLine

    barPlot = sns.barplot(ax=axes[id], x="dataset", y="median", hue="library", data=singleThreadedDf,
                          hue_order=hueOrder, palette = libColors())
    barPlot.set_ylabel("Runtime in ms", fontsize=12)
    barPlot.set_xlabel("Dataset", fontsize=12)
    barPlot.set_title("{}-Variant".format(singleThreadedDf['variant'].iloc[0]))
    barPlot.legend(bbox_to_anchor=(0.5, -0.4), loc='lower center', ncol=3, bbox_transform=fig.transFigure)

    #plt.tight_layout(pad=1)
    yscale = 'log'
    barPlot.set_yscale(yscale)
    if variant.variant.unique()[0] == "Parent":
        axes[id].get_legend().remove()

plt.savefig("out/bfs_yscale_{}.pdf".format(yscale), bbox_inches='tight')
plt.show()
