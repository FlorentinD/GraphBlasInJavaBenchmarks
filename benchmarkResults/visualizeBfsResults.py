# visualizing jmh benchmark results

# TODO generalize for other results (e.g. config dictionary and methods)

# read csv
import pandas as pd

benchmarkResult = pd.read_csv("results/bfs/bfsResultsServer.csv")
print(benchmarkResult.dtypes)

benchmarkResult["Name"] = benchmarkResult.Benchmark.str.split(".").str[-1]
benchmarkResult[["Library","BfsVariant"]] = benchmarkResult.Name.str.split("Bfs", expand=True)
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
from benchmarkResults.helper import grouped_barplot


allLibs = ["jni", "pregel", "ejml", "ejmlSparse", "ejmlDenseSparse",  "ejmlDense", "jGraphT"]
for variant in bfsVariants:
    # get meta info like units, mode, avg-degree ...
    containedLibs = variant.Library.unique()
    hueOrder = [i for i in allLibs if i in containedLibs]



    # sns approach fails to easily plot pre-aggregated error
    linePlot = sns.lineplot(x="nodeCount", y="Score", hue="Library", style="concurrency", data=variant,
                            hue_order=hueOrder, markers=True, legend="brief")
    linePlot.set_ylabel("Time in {}".format(variant.Units.unique()[0]))
    linePlot.set_xlabel("NodeCount x 10⁶")
    #linePlot.set_yscale('linear')
    linePlot.set_title(variant.BfsVariant.unique()[0])

    # fig, ax = plt.subplots()
    # barplot = grouped_barplot(variant, "nodeCount", "Name", "Score", "Error", ax)
    # barplot.title(title)
    #
    # plt.savefig("out/bfs_{}.jpg".format(variant['BfsVariant'].iloc[0]))
    plt.show()
