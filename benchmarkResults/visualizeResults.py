# visualizing jmh benchmark results


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
import numpy as np


# for plotting the error
# https://stackoverflow.com/questions/42017049/seaborn-how-to-add-error-bars-on-a-grouped-barplot
# + small bug fix if only one sub-categorie exists
def grouped_barplot(df, categorie, hueColumn, valueColumn, err):
    u = df[categorie].unique()
    x = np.arange(len(u))
    subx = df[hueColumn].unique()
    offsets = (np.arange(len(subx)) - np.arange(len(subx)).mean()) / (len(subx) + 1.)
    if (len(subx) > 1) :
        width = np.diff(offsets).mean()
    else:
        width = 0.8
    for i, gr in enumerate(subx):
        dfg = df[df[hueColumn] == gr]
        plt.bar(x + offsets[i],
                dfg[valueColumn].values,
                width=width,
                label=gr,
                yerr=dfg[err].values)
    plt.xlabel(categorie)
    plt.ylabel("Time in {}".format(variant.Units.iloc[0]))
    plt.xticks(x, u)
    plt.legend(loc="upper left")
    return plt


for variant in bfsVariants:
    # get meta info like units, mode, avg-degree ...
    # TODO: get actual run iterations somehow as an info (know for bfs ca. 37 for 300k nodes graph
    title = "{}-Bfs with a random power-law graph \n of {} nodes and an avg-degree of {} using the {} of {}".format(
        variant.BfsVariant.iloc[0], variant.nodeCount.iloc[0],
        variant.avgDegree.iloc[0], variant.Mode.iloc[0],
        variant.Cnt.iloc[0]
    )

    barplot = grouped_barplot(variant, "nodeCount", "Name", "Score", "Error")
    barplot.title(title)

    # sns approach fails to easily plot pre-aggregated error
    # barplot = sns.barplot(x="nodeCount", y="Score", hue="Name", data=variant)
    # barplot.set_ylabel(yLabel)

    plt.savefig("bfs_{}.jpg".format(variant['BfsVariant'].iloc[0]))
    plt.show()
