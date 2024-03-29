import pandas as pd
import os, re
from hw5.items import not_available

dataframes = []
out_path = "./csv_stats/consistency"

directory = './csv_stats/dataset'
for filename in os.listdir(directory):
    f = os.path.join(directory, filename)
    # checking if it is a file
    if os.path.isfile(f):
        df = pd.read_csv(f)
        df.Name = os.path.splitext(filename)[0]
        dataframes.append(df)

numerical_r = r"^\$?[0-9]+([.,]|[0-9])*\$?(\ [MBTKmbtk])?$"
def is_numerical(string: str):
    return re.match(numerical_r, string)

fields = lambda: {"numerical": 0, "textual": 0, "null": 0}
evaluate = { df.Name: { col: fields() for col in df.columns[1:] } for df in dataframes }
for df in dataframes:

    for idx in df.index:
        for col in df.columns[1:]:
            if df[col][idx] == not_available:
                evaluate[df.Name][col]["null"] += 1
            elif is_numerical(str(df[col][idx])):
                evaluate[df.Name][col]["numerical"] += 1
            else:
                evaluate[df.Name][col]["textual"] += 1

if not os.path.exists(out_path):
  os.mkdir(out_path)

for dataset in evaluate:
    df = pd.DataFrame()
    for column in evaluate[dataset]:

        tmp = pd.DataFrame(evaluate[dataset][column], index=[column])
        df = pd.concat([df,  tmp])

    df.to_csv(f"./csv_stats/consistency/{dataset}.csv") 