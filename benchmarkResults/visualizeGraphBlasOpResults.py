# visualizing jmh benchmark results

# TODO visualize semring results

import matplotlib.pyplot as plt
import numpy as np
# read csv
import pandas as pd
import seaborn as sns

from benchmarkResults.helper import booleanColorMap

#graphBlasOperation = "mxm"
graphBlasOperation = "reduceColWise"

operatorColumns = {"mxm": "semiRingName", "reduceColWise": "monoidName"}
operatorColumn = operatorColumns[graphBlasOperation]

semiringResults = pd.read_csv("results/matrixOps/{}WithSemiring.csv".format(graphBlasOperation))
maskResults = pd.read_csv("results/matrixOps/{}WithMask.csv".format(graphBlasOperation))
print(maskResults.dtypes)

semiringResults["Library"] = semiringResults.Benchmark.str.split("." + graphBlasOperation).str[-1]
semiringResults["Library"] = np.where((semiringResults.Library.str.len() == 0), "Ejml", semiringResults.Library)

maskResults["Library"] = maskResults.Benchmark.str.split("." + graphBlasOperation).str[-1].replace("WithMask", "",
                                                                                                   regex=True)
maskResults["Library"] = np.where((maskResults.Library.str.len() == 0), "Ejml", maskResults.Library)

print(semiringResults.head(5))

# get base-line for withMask results
operatorsForMask = {"mxm": "Plus;Times", "reduceColWise": "Plus"}
operatorForMask = operatorsForMask[graphBlasOperation]
dimensionForMask = maskResults["dimension"].unique()
baseLineForMask = semiringResults[(semiringResults["dimension"].isin(dimensionForMask)) & (
    semiringResults[operatorColumn].str.endswith(operatorForMask))].copy()
#assert len(baseLineForMask) == 3
baseLineForMask["negatedMask"] = "None"
baseLineForMask["structuralMask"] = "None"
baseLineForMask["avgEntriesPerColumnInMask"] = "None"

mask_parameters = ["negatedMask", "structuralMask"]
scoreUnit = maskResults["Units"].unique()[0]
matrixDim = maskResults["dimension"].unique()
assert len(matrixDim) == 1
matrixDim = matrixDim[0]

for entriesPerMaskColumn in maskResults["avgEntriesPerColumnInMask"].unique():
    for boolVal in [True, False]:
        # bar plot negated/non-negated
        title = "{} with negated Mask with {} entries per mask column \n (matrices dim: {}, structural: {})" \
            .format(graphBlasOperation, entriesPerMaskColumn, matrixDim, boolVal)
        negatedMaskDf = maskResults[(maskResults["structuralMask"] == boolVal) & (
                maskResults["avgEntriesPerColumnInMask"] == entriesPerMaskColumn)]
        negatedMaskDf = negatedMaskDf.append(baseLineForMask)
        negatedPlot = sns.barplot(x="Library", y="Score", hue="negatedMask", hue_order=["None", False, True],
                                  palette=booleanColorMap(), data=negatedMaskDf)
        negatedPlot.set_title(title)
        negatedPlot.set_ylabel("Time in {}".format(scoreUnit))
        negatedPlot.set_xlabel("GraphBLAS library")
        outFile = "out/{}_mask_negated_avgMaskEntries{}_structural{}.jpg".format(graphBlasOperation,entriesPerMaskColumn, boolVal)
        plt.savefig(outFile, bbox_inches='tight')
        plt.show()

        title = "{} with structural Mask with {} entries per mask column \n (matrices dim: {}, negated: {})".format(
            graphBlasOperation, entriesPerMaskColumn, matrixDim, boolVal)
        structuralMaskDf = maskResults[(maskResults["negatedMask"] == boolVal) & (
                maskResults["avgEntriesPerColumnInMask"] == entriesPerMaskColumn)]
        structuralMaskDf = structuralMaskDf.append(baseLineForMask)
        structuralPlot = sns.barplot(x="Library", y="Score", hue="structuralMask", hue_order=["None", True, False],
                                     palette=booleanColorMap(), data=structuralMaskDf)
        structuralPlot.set_title(title)
        structuralPlot.set_ylabel("Time in {}".format(scoreUnit))
        structuralPlot.set_xlabel("GraphBLAS library")
        outFile = "out/{}_mask_strutural_avgMaskEntries{}_negated{}.jpg".format(graphBlasOperation, entriesPerMaskColumn, boolVal)
        plt.savefig(outFile, bbox_inches='tight')
        plt.show()

# import seaborn as sns

title = "MxM with different semirings"

semiringPlot = sns.lineplot(data=semiringResults, x="dimension", y="Score", hue=operatorColumn, style="Library",
                            markers=True)
plt.show()

# fig, ax = plt.subplots()
# barplot = grouped_barplot(variant, "dimension", "Name", "Score", "Error", ax)
# barplot.title(title)

# sns approach fails to easily plot pre-aggregated error
# barplot = sns.barplot(x="nodeCount", y="Score", hue="Name", data=variant)
# barplot.set_ylabel(yLabel)

# plt.savefig("out/mxmWithSemiring.jpg")
# plt.show()
