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
baseLineForMask = baseLineForMask[~baseLineForMask["library"].str.contains("inlined")]
# assert len(baseLineForMask) == 3
baseLineForMask["negated"] = "None"
baseLineForMask["structural"] = "None"
baseLineForMaskSubset = baseLineForMask[["library", "median"]].copy()
baseLineForMaskSubset.rename(columns={"median": "baseline"}, inplace=True)

mask_parameters = ["negated", "structural"]

# assuming default: e.g. are non-negated and non-structural

# bar plot negated/non-negated
negatedDf = maskResults[maskResults["structural"] == False]
negatedDf = negatedDf.append(baseLineForMask)
negatedDf = pd.merge(negatedDf, baseLineForMaskSubset, how="inner", on="library")
negatedDf["speedup"] = negatedDf["baseline"] / negatedDf["median"]

fig, ax = plt.subplots()
negatedPlot = sns.barplot(x="library", y="speedup", hue="negated", hue_order=["None", False, True],
                          palette=booleanColorMap(), data=negatedDf)
# negatedPlot.title(title)
negatedPlot.set_ylabel("Speedup", fontsize=12)
negatedPlot.set_xlabel("GraphBLAS library", fontsize=12)
ax.legend(title='Mask negated')
#ax.set_ylim([0, maxValue])
negatedPlot.legend(bbox_to_anchor=(0.5, -0.1), loc='lower center', ncol=3, bbox_transform=fig.transFigure)
outFile = "out/mxm_mask_negated.pdf"
plt.savefig(outFile, bbox_inches='tight')
plt.show()

# title = "{} with structural Mask with {} entries per mask column \n (matrices dim: {}, negated: {})".format(graphBlasOperation, entriesPerMaskColumn, matrixDim)
structuralDf = maskResults[(maskResults["negated"] == False)]
structuralDf = structuralDf.append(baseLineForMask)
structuralDf = pd.merge(structuralDf, baseLineForMaskSubset, how="inner", on="library")
structuralDf["speedup"] = structuralDf["baseline"] / structuralDf["median"]


fig, ax = plt.subplots()
structuralPlot = sns.barplot(x="library", y="speedup", hue="structural", hue_order=["None", True, False],
                             palette=booleanColorMap(), data=structuralDf)
# structuralPlot.title(title)
ax.legend(title='Mask structural')
# ax.set_ylim([0, maxValue])
structuralPlot.set_ylabel("Speedup", fontsize=12)
structuralPlot.set_xlabel("GraphBLAS library", fontsize=12)
structuralPlot.legend(bbox_to_anchor=(0.5, -0.1), loc='lower center', ncol=3, bbox_transform=fig.transFigure)
outFile = "out/mxm_mask_strutural.pdf"
plt.savefig(outFile, bbox_inches='tight')
plt.show()

semiringResults = semiringResults[semiringResults["concurrency"] == 1]

# TODO what is the baseline here? .. PLUS,TIMES semiring? (but then we throw away the comparison to prev EJML)
#semirings
baselineDf = semiringResults[semiringResults["library"].str.endswith("EJML")].copy()
baselineDf = baselineDf[["semiring", "median"]]
baselineDf.rename(columns={"median":"baseline"}, inplace=True)
baselinedVariant = pd.merge(semiringResults, baselineDf, how="inner", on="semiring")
baselinedVariant["speedup"] = baselinedVariant["baseline"] / baselinedVariant["median"]

fig, ax = plt.subplots(figsize=(6, 3))
plt.setp(ax.get_xticklabels(), rotation=30)
semiringPlot = sns.barplot(data=baselinedVariant, x="semiring", y="speedup", hue="library", hue_order=["EJML", "Java-Native", "EJML(inlined)"])
semiringPlot.set_ylabel("Speedup", fontsize=12)
semiringPlot.set_xlabel("GraphBLAS Library", fontsize=12)
semiringPlot.legend(bbox_to_anchor=(0.5, -0.45), loc='lower center', ncol=2, bbox_transform=fig.transFigure)
plt.savefig("out/mxmWithSemiring.pdf", bbox_inches='tight')
plt.show()
