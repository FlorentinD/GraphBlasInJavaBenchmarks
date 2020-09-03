# visualizing jmh benchmark results

# TODO generalize for other results (e.g. config dictionary and methods)

# read csv
import pandas as pd

benchmarkResult = pd.read_csv("bfsResults.csv")
print(benchmarkResult.dtypes)

benchmarkResult["Name"] = benchmarkResult.Benchmark.str.split(".").str[1]
benchmarkResult["BfsVariant"] = benchmarkResult.Name.str.split("Bfs").str[1]
benchmarkResult["Name"] = "(" + benchmarkResult.Name.str.split("Bfs").str[0] + "," + benchmarkResult.concurrency.apply(
    str) + ")"

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
from benchmarkResults.helper import grouped_barplot


for variant in bfsVariants:
    # get meta info like units, mode, avg-degree ...
    # TODO: get actual run iterations somehow as an info (know for bfs ca. 37 for 300k nodes graph
    title = "{}-Bfs on a random power-law graph \n with an average degree of {} using the {} of {}".format(
        variant.BfsVariant.iloc[0], variant.avgDegree.iloc[0],
        variant.Mode.iloc[0], variant.Cnt.iloc[0]
    )

    fig, ax = plt.subplots()
    barplot = grouped_barplot(variant, "nodeCount", "Name", "Score", "Error", ax)
    barplot.title(title)

    # sns approach fails to easily plot pre-aggregated error
    # barplot = sns.barplot(x="nodeCount", y="Score", hue="Name", data=variant)
    # barplot.set_ylabel(yLabel)

    plt.savefig("bfs_{}.jpg".format(variant['BfsVariant'].iloc[0]))
    plt.show()
