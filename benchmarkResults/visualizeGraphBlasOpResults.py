import matplotlib.pyplot as plt
from benchmarkResults.helper import grouped_barplot, getUnit, booleanColorMap
import numpy as np
# read csv
import pandas as pd
import seaborn as sns

semiringResults = pd.read_csv("results/semirings.csv", skipinitialspace=True)
maskResults = pd.read_csv("results/masks.csv", skipinitialspace=True)
dfs = [maskResults, semiringResults]
print(maskResults.dtypes)

for df in dfs:
    df["library"] = df.benchmark
    for (prev, replacement) in {"MxM": "", "Benchmark": "", "WithSemiring": "", "WithMask": "", "Native": "Java-Native",
                                "Ejml": "EJML"}.items():
        df["library"] = df["library"].str.replace(prev, replacement)

print(semiringResults.head(5))

# get base-line for withMask results
baseLineForMask = semiringResults[semiringResults["semiring"].str.endswith("(PLUS;TIMES)")].copy()
baseLineForMask = baseLineForMask[baseLineForMask["concurrency"] == 1]
# assert len(baseLineForMask) == 3
baseLineForMask["negated"] = "None"
baseLineForMask["structural"] = "None"

mask_parameters = ["negated", "structural"]

# assuming default: e.g. are non-negated and non-structural

# bar plot negated/non-negated
negatedDf = maskResults[maskResults["structural"] == False]
negatedDf = negatedDf.append(baseLineForMask)
fig, ax = plt.subplots()
negatedPlot = sns.barplot(x="library", y="median", hue="negated", hue_order=["None", False, True],
                          palette=booleanColorMap(), data=negatedDf)
# negatedPlot.title(title)
negatedPlot.set_ylabel("Runtime in ms", fontsize=12)
negatedPlot.set_xlabel("GraphBLAS library", fontsize=12)
ax.legend(title='Mask negated')
#ax.set_ylim([0, maxValue])
outFile = "out/mxm_mask_negated.pdf"
plt.savefig(outFile, bbox_inches='tight')
plt.show()

# title = "{} with structural Mask with {} entries per mask column \n (matrices dim: {}, negated: {})".format(graphBlasOperation, entriesPerMaskColumn, matrixDim)
structuralDf = maskResults[(maskResults["negated"] == False)]
structuralDf = structuralDf.append(baseLineForMask)
fig, ax = plt.subplots()
structuralPlot = sns.barplot(x="library", y="median", hue="structural", hue_order=["None", True, False],
                             palette=booleanColorMap(), data=structuralDf)
# structuralPlot.title(title)
ax.legend(title='Mask structural')
# ax.set_ylim([0, maxValue])
structuralPlot.set_ylabel("Runtime in ms", fontsize=12)
structuralPlot.set_xlabel("GraphBLAS library", fontsize=12)
outFile = "out/mxm_mask_strutural.pdf"
plt.savefig(outFile, bbox_inches='tight')
plt.show()

semiringResults = semiringResults[semiringResults["concurrency"] == 1]

#semirings

fig, ax = plt.subplots(figsize=(6, 3))
semiringPlot = sns.barplot(data=semiringResults, x="library", y="median", hue="semiring")
semiringPlot.set_ylabel("Runtime in ms", fontsize=12)
semiringPlot.set_xlabel("GraphBLAS Library", fontsize=12)
semiringPlot.legend(bbox_to_anchor=(0.5, -0.45), loc='lower center', ncol=2, bbox_transform=fig.transFigure)
plt.savefig("out/mxmWithSemiring.pdf", bbox_inches='tight')
plt.show()
