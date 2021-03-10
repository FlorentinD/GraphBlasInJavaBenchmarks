# read csv
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from benchmarkResults.helper import getUnit, libColors

fig, ax = plt.subplots()

benchmarkResult = pd.read_csv("results/pagerank.csv", skipinitialspace=True)

print(benchmarkResult.dtypes)
print(benchmarkResult.head(5))

benchmarkResult["library"] = benchmarkResult.benchmark

for (prev, replacement) in {"PageRank":"", "Benchmark":"", "Pregel": "GDS-Pregel", "Native": "Java-Native", "Ejml": "EJML"}.items():
    benchmarkResult["library"] = benchmarkResult["library"].str.replace(prev, replacement)

singleThreadedDf = benchmarkResult[benchmarkResult["concurrency"] == 1]
barPlot = sns.barplot(ax=ax, data=singleThreadedDf, x="dataset", y="median", hue="library", palette=libColors())
barPlot.set_ylabel("Runtime in ms", fontsize=12)
barPlot.set_xlabel("Dataset", fontsize=12)
barPlot.set_yscale('log')
barPlot.legend(bbox_to_anchor=(0.5, -0.3), loc='lower center', ncol=3, bbox_transform=fig.transFigure)
outFile = "out/pageRanks.pdf"
plt.savefig(outFile, bbox_inches='tight')
# nodeCountPlot.set_yscale('log')
plt.show()
