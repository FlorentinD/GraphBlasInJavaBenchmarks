# visualizing jmh benchmark results

# TODO visualize semring results

import matplotlib.pyplot as plt
import numpy as np
# read csv
import pandas as pd
import seaborn as sns

semiringResults = pd.read_csv("results/matrixOps/mxmWithSemiring.csv")
maskResults = pd.read_csv("results/matrixOps/mxmWithMask.csv")
print(maskResults.dtypes)

semiringResults["Name"] = semiringResults.Benchmark.str.split(".").str[-1]
semiringResults["Library"] = np.where(semiringResults.Benchmark.str.contains('Native'), "Native", "EJML")
maskResults["Library"] = np.where(maskResults.Benchmark.str.contains('Native'), "Native", "EJML")

print(semiringResults.head(5))

# get base-line for withMask results
semiRingForMask = "(Plus-Times)"
dimensionForMask = maskResults["dimension"].unique()
baseLineForMask = semiringResults[(semiringResults["dimension"].isin(dimensionForMask)) & (
    semiringResults["semiRingName"].str.endswith(semiRingForMask))].copy()
assert len(baseLineForMask) == 2
baseLineForMask["negatedMask"] = "None"
baseLineForMask["structuralMask"] = "None"
baseLineForMask["avgEntriesPerColumnInMask"] = "None"

mask_parameters = ["negatedMask", "structuralMask"]

for entriesPerMaskColumn in maskResults["avgEntriesPerColumnInMask"].unique():
    # bar plot negated/non-negated
    title = "MxM with negated Mask with {} entries per mask column".format(entriesPerMaskColumn)
    negatedMaskDf = maskResults[(maskResults["structuralMask"]) & (maskResults["avgEntriesPerColumnInMask"] == entriesPerMaskColumn)]
    negatedMaskDf = negatedMaskDf.append(baseLineForMask)
    negatedPlot = sns.barplot(x="Library", y="Score", hue="negatedMask", data=negatedMaskDf)
    negatedPlot.set_title(title)
    plt.show()

    title = "MxM with structural Mask with {} entries per mask column".format(entriesPerMaskColumn)
    structuralMaskDf = maskResults[(maskResults["negatedMask"]) & (maskResults["avgEntriesPerColumnInMask"] == entriesPerMaskColumn)]
    structuralMaskDf = structuralMaskDf.append(baseLineForMask)
    structuralPlot = sns.barplot(x="Library", y="Score", hue="structuralMask", data=structuralMaskDf)
    structuralPlot.set_title(title)
    plt.show()

# import seaborn as sns

title = "MxM with different semirings"

# lineplot(data=semiringResults, x="dimension", y="Score", hue="semiRingName", style="Library", err_style="bars")

# fig, ax = plt.subplots()
# barplot = grouped_barplot(variant, "dimension", "Name", "Score", "Error", ax)
# barplot.title(title)

# sns approach fails to easily plot pre-aggregated error
# barplot = sns.barplot(x="nodeCount", y="Score", hue="Name", data=variant)
# barplot.set_ylabel(yLabel)

#plt.savefig("out/mxmWithSemiring.jpg")
#plt.show()
