# visualizing jmh benchmark results

# TODO visualize semring results

import matplotlib.pyplot as plt
from benchmarkResults.helper import grouped_barplot, getUnit, booleanColorMap
import numpy as np
# read csv
import pandas as pd
import seaborn as sns

graphBlasOperation = "mxm"
#graphBlasOperation = "reduceColWise"

operatorColumns = {"mxm": "Semiring", "reduceColWise": "Monoid"}
operatorColumn = operatorColumns[graphBlasOperation]

semiringResults = pd.read_csv("results/matrixOps/{}WithSemiring.csv".format(graphBlasOperation))
semiringResults = semiringResults.rename(columns={"semiRingName":"Semiring", "monoidName": "Monoid"})
maskResults = pd.read_csv("results/matrixOps/{}WithMask.csv".format(graphBlasOperation))
dfs = [maskResults, semiringResults]
print(maskResults.dtypes)


semiringResults["Library"] = semiringResults.Benchmark.str.split("." + graphBlasOperation).str[-1]
maskResults["Library"] = maskResults.Benchmark.str.split("." + graphBlasOperation).str[-1].replace("WithMask", "",
                                                                                                   regex=True)
for df in dfs:
    df["Library"] = np.where((df.Library.str.len() == 0), "Ejml", df.Library)
    df["Library"] = df["Library"].str.replace("Native", "Java-Native (SuiteSparse)")
    df["dimension"] = df["dimension"] / 1_000_000

maskResults["Library"] = np.where((maskResults.Library.str.len() == 0), "Ejml", maskResults.Library)

if (graphBlasOperation == "reduceColWise"):
    maskResults = maskResults[(maskResults.denseMask == False)]

print(semiringResults.head(5))

# get base-line for withMask results
operatorsForMask = {"mxm": "Plus;Times", "reduceColWise": "Plus"}
operatorForMask = operatorsForMask[graphBlasOperation]
dimensionForMask = maskResults["dimension"].unique()
baseLineForMask = semiringResults[(semiringResults["dimension"].isin(dimensionForMask)) & (
    semiringResults[operatorColumn].str.endswith(operatorForMask))].copy()
baseLineForMask = baseLineForMask[baseLineForMask["concurrency"] == 1]
#assert len(baseLineForMask) == 3
baseLineForMask["negatedMask"] = "None"
baseLineForMask["structuralMask"] = "None"
baseLineForMask["avgEntriesPerColumnInMask"] = "None"

mask_parameters = ["negatedMask", "structuralMask"]
scoreUnit = maskResults["Units"].unique()[0]
matrixDim = maskResults["dimension"].unique()
assert len(matrixDim) == 1
matrixDim = matrixDim[0]

maskResults = maskResults[maskResults["concurrency"] == 1]

# assuming default: e.g. are non-negated and non-structural
for entriesPerMaskColumn in maskResults["avgEntriesPerColumnInMask"].unique():
        maxValue = int(pd.Series.max(maskResults[(maskResults["avgEntriesPerColumnInMask"] == entriesPerMaskColumn)].Score) * 1.3)
        # bar plot negated/non-negated
        #title = "{} with negated Mask with {} entries per mask column \n (matrices dim: {})".format(graphBlasOperation, entriesPerMaskColumn, matrixDim)
        negatedMaskDf = maskResults[(maskResults["structuralMask"] == False) & (
                maskResults["avgEntriesPerColumnInMask"] == entriesPerMaskColumn)]
        negatedMaskDf = negatedMaskDf.append(baseLineForMask)
        fig, ax = plt.subplots()
        # negatedPlot = sns.barplot(x="Library", y="Score", hue="negatedMask", hue_order=["None", False, True],
        #                           palette=booleanColorMap(), data=negatedMaskDf)
        negatedPlot = grouped_barplot(negatedMaskDf, "Library", "negatedMask", "Score", "Error", ax, booleanColorMap(), False)
        #negatedPlot.title(title)
        negatedPlot.ylabel("Runtime in {}".format(getUnit(negatedMaskDf)), fontsize=12)
        negatedPlot.xlabel("GraphBLAS library", fontsize=12)
        ax.legend(title='Mask negated')
        ax.set_ylim([0, maxValue])
        outFile = "out/{}_mask_negated_avgMaskEntries{}.pdf".format(graphBlasOperation,entriesPerMaskColumn)
        plt.savefig(outFile, bbox_inches='tight')
        plt.show()

        #title = "{} with structural Mask with {} entries per mask column \n (matrices dim: {}, negated: {})".format(graphBlasOperation, entriesPerMaskColumn, matrixDim)
        structuralMaskDf = maskResults[(maskResults["negatedMask"] == False) & (
                maskResults["avgEntriesPerColumnInMask"] == entriesPerMaskColumn)]
        structuralMaskDf = structuralMaskDf.append(baseLineForMask)
        # structuralPlot = sns.barplot(x="Library", y="Score", hue="structuralMask", hue_order=["None", True, False],
        #                              palette=booleanColorMap(), data=structuralMaskDf)
        fig, ax = plt.subplots()
        structuralPlot = grouped_barplot(structuralMaskDf, "Library", "structuralMask", "Score", "Error", ax, booleanColorMap(), False)
        #structuralPlot.title(title)
        ax.legend(title='Mask structural')
        ax.set_ylim([0, maxValue])
        structuralPlot.ylabel("Runtime in {}".format(getUnit(structuralMaskDf)), fontsize=12)
        structuralPlot.xlabel("GraphBLAS library", fontsize=12)
        outFile = "out/{}_mask_strutural_avgMaskEntries{}.pdf".format(graphBlasOperation, entriesPerMaskColumn)
        plt.savefig(outFile, bbox_inches='tight')
        plt.show()

# import seaborn as sns



semiringResults = semiringResults[semiringResults["concurrency"] == 1]

if (graphBlasOperation == "mxm"):
    semiringResults["Score"] = semiringResults["Score"] / 1_000
    semiringResults["Units"] = "s/op"

semiringPlot = sns.lineplot(data=semiringResults, x="dimension", y="Score", hue=operatorColumn, style="Library",
                            markers=True)
semiringPlot.set_ylabel("Runtime in {}".format(getUnit(semiringResults)), fontsize=12)
semiringPlot.set_xlabel("Matrix-Dimension x 10⁶", fontsize=12)
plt.savefig("out/{}WithSemiring.pdf".format(graphBlasOperation))
plt.show()

# fig, ax = plt.subplots()
# barplot = grouped_barplot(variant, "dimension", "Name", "Score", "Error", ax)
# barplot.title(title)

# sns approach fails to easily plot pre-aggregated error
# barplot = sns.barplot(x="nodeCount", y="Score", hue="Name", data=variant)
# barplot.set_ylabel(yLabel)

# plt.savefig("out/mxmWithSemiring.jpg")
# plt.show()
