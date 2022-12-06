import pandas as pd
import os, re
from hw5.items import not_available

dataframes = []
directory = './dataset'
for filename in os.listdir(directory):
    f = os.path.join(directory, filename)
    # checking if it is a file
    if os.path.isfile(f):
        df = pd.read_csv(f)
        df.Name = os.path.splitext(filename)[0]
        dataframes.append(df)

numerical_r = r"^\$?[0-9]+([.,]|[0-9])*\$?(\ [MBTKmbtk])?$"
def is_nuerical(string: str):
    return re.match(numerical_r, string)

fields = lambda: {"num": 0, "tex": 0, "null": 0}
evaluate = { df.Name: { col: fields() for col in df.columns[1:] } for df in dataframes }
for df in dataframes:

    for idx in df.index:
        for col in df.columns[1:]:
            if df[col][idx] == not_available:
                evaluate[df.Name][col]["null"] += 1
            else:
                if is_nuerical(str(df[col][idx])):
                    evaluate[df.Name][col]["num"] += 1
                else:
                    evaluate[df.Name][col]["tex"] += 1
                

os.mkdir("./consistency")
for dataset in evaluate:
    df = pd.DataFrame()
    for column in evaluate[dataset]:

        tmp = pd.DataFrame(evaluate[dataset][column], index=[column])
        df = pd.concat([df,  tmp])

    df.to_csv(f"./consistency/{dataset}.csv") 