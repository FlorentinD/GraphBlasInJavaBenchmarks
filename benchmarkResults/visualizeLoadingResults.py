# visualizing jmh benchmark results

# TODO generalize for other results (e.g. config dictionary and methods)

# read csv
import pandas as pd

# fixed nodeCount -> put in figure title
# weighted/un-weighted -> style, libary -> color, avg-degree -> x Axis
benchmarkResult = pd.read_csv("results/loading/loadingResultsServer.csv")
print(benchmarkResult.dtypes)

benchmarkResult["Library"] = benchmarkResult.Benchmark.str.split(".load").str[-1]

for (prev, replacement) in {"Jni": "Java-Native", "Graph": "", "Ejml": "EJML"}.items():
    benchmarkResult["Library"] = benchmarkResult["Library"].str.replace(prev, replacement)

benchmarkResult["Score"] = benchmarkResult["Score"] / 1000.0
benchmarkResult["Units"] = "s/op"

print(benchmarkResult.head(5))


import matplotlib.pyplot as plt
import seaborn as sns
from benchmarkResults.helper import libColors, getUnit

fig, ax = plt.subplots(figsize=(6,3))
linePlot = sns.lineplot(x="avgDegree", y="Score", hue="Library", palette = libColors(), style="weighted", data=benchmarkResult,
                        markers=True, legend="brief")
linePlot.set_ylabel("Runtime in {}".format(getUnit(benchmarkResult)), fontsize=12)
linePlot.set_xlabel("Average vertex degree", fontsize=12)
linePlot.legend(bbox_to_anchor=(0.5, -0.15), loc='lower center', ncol=2, bbox_transform=fig.transFigure)
#linePlot.set_yscale('log')
plt.tight_layout(pad=1)
plt.savefig("out/loadGraph.pdf", bbox_inches='tight')
plt.show()

