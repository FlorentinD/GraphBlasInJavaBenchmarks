# visualizing jmh benchmark results

# TODO generalize for other results (e.g. config dictionary and methods)

# read csv
import pandas as pd

benchmarkResult = pd.read_csv("results/bfs/bfsResultsServer.csv")
print(benchmarkResult.dtypes)

benchmarkResult["Name"] = benchmarkResult.Benchmark.str.split(".").str[-1]
benchmarkResult[["Library","BfsVariant"]] = benchmarkResult.Name.str.split("Bfs", expand=True)

for (prev, replacement) in {"ejml": "ejml-", "DenseSparse": "Dense-Sparse", "pregel": "gds-pregel"}.items():
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
from benchmarkResults.helper import grouped_barplot, libColors

allLibs = ["jni", "gds-pregel", "ejml", "ejml-Sparse", "ejml-Dense-Sparse",  "ejml-Dense", "jGraphT"]
for variant in bfsVariants:
    # get meta info like units, mode, avg-degree ...
    containedLibs = variant.Library.unique()
    hueOrder = [i for i in allLibs if i in containedLibs]



    # sns approach fails to easily plot pre-aggregated error
    linePlot = sns.lineplot(x="nodeCount", y="Score", hue="Library", style="concurrency", data=variant,
                            hue_order=hueOrder, palette = libColors(), markers=True)
    linePlot.set_ylabel("Time in {}".format(variant.Units.unique()[0]))
    linePlot.set_xlabel("Number of vertices x 10⁶")
    linePlot.legend(bbox_to_anchor=(1, 1), loc='upper left', ncol=1)
    plt.tight_layout(pad=1)
    yscale = 'log'
    linePlot.set_yscale(yscale)
    #linePlot.set_title(variant.BfsVariant.unique()[0])

    # fig, ax = plt.subplots()
    # barplot = grouped_barplot(variant, "nodeCount", "Name", "Score", "Error", ax)
    # barplot.title(title)
    #
    plt.savefig("out/bfs_{}_yscale_{}.jpg".format(variant['BfsVariant'].iloc[0], yscale))
    plt.show()
