import matplotlib.pyplot as plt
import numpy as np
import math

def libColors():
    return {
        "EJML": "tab:blue",
        "EJML-Global": "tab:purple",
        "EJML-VertexWise": "tab:blue",
        "EJML-Sparse": "tab:purple",
        "EJML-Dense": "tab:blue",
        "EJML-Dense-Sparse": "tab:cyan",
        "JGraphT": "tab:brown",
        "JGraphT-Global": "tab:brown",
        "GDS": "tab:olive",
        "GDS-VertexWise": "tab:olive",
        "Java-Native":"tab:red",
        "Java-Native (SuiteSparse)":"tab:red",
        "Java-Native-Global": "tab:orange",
        "Java-Native-VertexWise": "tab:red",
        "GDS-Pregel": "tab:green",
        "GDS-Pregel-VertexWise": "tab:green"
    }

def booleanColorMap():
    return {
        True: "tab:green",
        False: "tab:orange",
        "None": "tab:grey"
    }

def getUnit(benchmarkDf):
    return benchmarkDf.Units.unique()[0].split("/")[0]

# for plotting the error
# https://stackoverflow.com/questions/42017049/seaborn-how-to-add-error-bars-on-a-grouped-barplot
# + small bug fix if only one sub-categorie exists
def grouped_barplot(df, categorie, hueColumn, valueColumn, err, ax, colorMap, showValues):
    u = df[categorie].unique()
    x = np.arange(len(u))
    subx = df[hueColumn].unique()
    offsets = (np.arange(len(subx)) - np.arange(len(subx)).mean()) / (len(subx) + 1.)
    if len(subx) > 1:
        width = np.diff(offsets).mean()
    else:
        width = 0.8

    bars = []
    for i, gr in enumerate(subx):
        dfg = df[df[hueColumn] == gr]
        bars.append(
            plt.bar(x + offsets[i],
                    dfg[valueColumn].values,
                    width=width,
                    label=gr,
                    yerr=dfg[err].values,
                    color=colorMap[gr],
                    ecolor="gray")
        )

    # print values above bar
    if showValues:
        for bar in bars:
            autolabel(bar, ax)

    plt.xlabel(categorie)
    plt.ylabel("Runtime in {}".format(getUnit(df)))
    plt.xticks(x, u)
    plt.legend(loc="upper left")
    return plt

def autolabel(bars, ax):
    """Attach a text label above each bar in *rects*, displaying its height."""
    for bar in bars:
        height = 0
        if not math.isnan(bar.get_height()):
            height = round(bar.get_height())

        ax.annotate('{}'.format(height),
                    xy=(bar.get_x() + bar.get_width() / 2, height),
                    xytext=(0, 3),  # 3 points vertical offset
                    textcoords="offset points",
                    ha='center', va='bottom')
