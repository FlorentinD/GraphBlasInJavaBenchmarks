# visualizing jmh benchmark results

# TODO generalize for other results (e.g. config dictionary and methods)

# read csv
import pandas as pd

benchmarkResult = pd.read_csv("results/bfs/bfsResultsServer.csv")
print(benchmarkResult.dtypes)

benchmarkResult["Name"] = benchmarkResult.Benchmark.str.split(".").str[-1]
benchmarkResult[["Library","BfsVariant"]] = benchmarkResult.Name.str.split("Bfs", expand=True)
benchmarkResult = benchmarkResult.rename(columns={"concurrency": "Concurrency"})

for (prev, replacement) in {"ejml": "EJML-", "DenseSparse": "Dense-Sparse", "pregel": "GDS-Pregel", "jni": "Java-Native", "jGraphT": "JGraphT"}.items():
    benchmarkResult["Library"] = benchmarkResult["Library"].str.replace(prev, replacement)

benchmarkResult["nodeCount"] = benchmarkResult.nodeCount / (10 ** 6)

print(benchmarkResult.head(5))

gb = benchmarkResult.groupby('BfsVariant')
bfsVariants = [gb.get_group(x) for x in gb.groups]
# print([v.head(5) for v in bfsVariants])


# create diagrams
#   x-axis = nodeCount
#   variant = (benchmarkName, concurrency)
#   also plot error ..
#   use unit column and to generate y-axis label

# import seaborn as sns
import matplotlib.pyplot as plt
import seaborn as sns
from benchmarkResults.helper import grouped_barplot, libColors, getUnit

allLibs = ["Java-Native", "GDS-Pregel", "EJML", "EJML-Sparse", "EJML-Dense-Sparse",  "EJML-Dense", "JGraphT"]
fig, axes = plt.subplots(1, 2, figsize=(6,3), sharey=True, sharex=True)

for id, variant in enumerate(bfsVariants):
    # get meta info like units, mode, avg-degree ...
    containedLibs = variant.Library.unique()
    hueOrder = [i for i in allLibs if i in containedLibs]

    # sns approach fails to easily plot pre-aggregated error
    linePlot = sns.lineplot(ax=axes[id], x="nodeCount", y="Score", hue="Library", style="Concurrency", data=variant[variant.Library.isin(allLibs)],
                            hue_order=hueOrder, palette = libColors(), markers=True)
    linePlot.set_ylabel("Runtime in {}".format(getUnit(variant)), fontsize=12)
    linePlot.set_xlabel("Number of vertices x 10⁶", fontsize=12)
    linePlot.set_title("{}-Variant".format(variant['BfsVariant'].iloc[0]))
    linePlot.legend(bbox_to_anchor=(0.5, -0.4), loc='lower center', ncol=3, bbox_transform=fig.transFigure)

    #plt.tight_layout(pad=1)
    yscale = 'log'
    linePlot.set_yscale(yscale)
    if id != 0:
        axes[id].get_legend().remove()

    #linePlot.set_title(variant.BfsVariant.unique()[0])

    # fig, ax = plt.subplots()
    # barplot = grouped_barplot(variant, "nodeCount", "Name", "Score", "Error", ax)
    # barplot.title(title)
    #
plt.savefig("out/bfs_yscale_{}.pdf".format(yscale), bbox_inches='tight')
plt.show()
