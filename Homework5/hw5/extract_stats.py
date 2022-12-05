import pandas as pd
import os

dataframes = []
directory = './dataset'
for filename in os.listdir(directory):
    f = os.path.join(directory, filename)
    # checking if it is a file
    if os.path.isfile(f):
        df = pd.read_csv(f)
        df.Name =  os.path.splitext(filename)[0]
        dataframes.append(df)
print(dataframes)

dataframe_to_complete = {}

for df in dataframes:
    for ind in df.index:
        for col in df.columns:
            if df[col][ind] == 'None' or df[col][ind] == None:
                if dataframe_to_complete.get(df.Name):
                    dataframe_to_complete[df.Name] += 1
                else:
                    dataframe_to_complete.update({df.Name: 1})
                break
print(dataframe_to_complete)