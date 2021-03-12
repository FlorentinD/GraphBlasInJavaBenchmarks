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

for (prev, replacement) in {"PageRank": "", "Benchmark": "", "Pregel": "GDS-Pregel", "Native": "Java-Native",
                            "Ejml": "EJML"}.items():
    benchmarkResult["library"] = benchmarkResult["library"].str.replace(prev, replacement)

baselineDf = benchmarkResult[benchmarkResult["library"].str.contains("EJML")]
baselineDf = baselineDf[["dataset", "median"]]
baselineDf.rename(columns={"median": "baseline"}, inplace=True)
baselinedVariant = pd.merge(benchmarkResult, baselineDf, how="inner", on="dataset")
baselinedVariant["slowdown"] = baselinedVariant["median"] / baselinedVariant["baseline"]
singleThreadedDf = baselinedVariant[baselinedVariant["concurrency"] == 1]

hue_order = ["JGraphT", "GDS-Pregel", "EJML", "Java-Native"]
order=["Facebook", "Slashdot0902", "POKEC", "Patents"]
barPlot = sns.barplot(ax=ax, data=singleThreadedDf, x="dataset", y="slowdown", hue="library", palette=libColors(), order=order, hue_order=hue_order)
barPlot.set_ylabel("Slowdown", fontsize=12)
barPlot.set_xlabel("Dataset", fontsize=12)
#barPlot.set_yscale('log')
barPlot.legend(bbox_to_anchor=(0.5, -0.1), loc='lower center', ncol=4, bbox_transform=fig.transFigure)
outFile = "out/pageRanks.pdf"
plt.savefig(outFile, bbox_inches='tight')
# nodeCountPlot.set_yscale('log')
plt.show()
