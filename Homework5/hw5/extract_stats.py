import pandas as pd
import os

def get_uncomplete_rows(dfs, dataframe_to_complete):
    for df in dfs:
        for ind in df.index:
            for col in df.columns:
                if df[col][ind] == 'None' or df[col][ind] == None:
                    if dataframe_to_complete.get(df.Name).get('uncompleterows'):
                        dataframe_to_complete[df.Name]['uncompleterows'] += 1
                    else:
                        dataframe_to_complete.get(df.Name).update({'uncompleterows': 1})
                    break
    return dataframe_to_complete

def get_uncomplete_columns(dfs, dataframe_to_complete):
    for df in dfs:
        for col in df.columns:
            for ind in df.index:
                if df[col][ind] == 'None' or df[col][ind] == None:
                    if dataframe_to_complete.get(df.Name).get('uncompletecolumns'):
                        dataframe_to_complete[df.Name]['uncompletecolumns'] += 1
                    else:
                        dataframe_to_complete.get(df.Name).update({'uncompletecolumns': 1})
                    break
    return dataframe_to_complete

def get_null_values(dfs, dataframe_to_complete):
    for df in dfs:
        for col in df.columns:
            for ind in df.index:
                if df[col][ind] == 'None' or df[col][ind] == None:
                    if dataframe_to_complete.get(df.Name).get('nnan'):
                        dataframe_to_complete[df.Name]['nnan'] += 1
                    else:
                        dataframe_to_complete.get(df.Name).update({'nnan': 1})
    return dataframe_to_complete

def get_null_values_for_column(dfs, dataframe_to_complete):
    for df in dfs:
        col_df = {}
        first_column = True
        for col in df.columns:
            if first_column:
                first_column = False
                continue
            nones = 0
            for ind in df.index:
                if df[col][ind] == 'None' or df[col][ind] == None:
                    nones += 1
            if nones != 0:
                col_df.update({col: nones}) 
        dataframe_to_complete.get(df.Name).update({'nanforcol': col_df})
    return dataframe_to_complete

def get_null_values_for_row(dfs, dataframe_to_complete):
    for df in dfs:
        row_df = {}
        first_row = True
        for ind in df.index:
            if first_row:
                first_row = False
                continue
            nones = 0
            for col in df.columns:
                if df[col][ind] == 'None' or df[col][ind] == None:
                    nones += 1
            if nones != 0:
                row_df.update({ind: nones}) 
        dataframe_to_complete.get(df.Name).update({'nanforrow': row_df})
    return dataframe_to_complete

def get_stats(dfs):
    data_to_complete = {}
    for df in dfs:
        data_to_complete.update({df.Name: {'numbercolumns': len(df.columns), 'numberrows': len(df.index)}})
    data_to_complete = get_uncomplete_columns(dfs, data_to_complete)
    data_to_complete = get_uncomplete_rows(dfs, data_to_complete)
    data_to_complete = get_null_values(dfs, data_to_complete)
    data_to_complete = get_null_values_for_column(dfs, data_to_complete)
    data_to_complete = get_null_values_for_row(dfs, data_to_complete)
    for key in data_to_complete.keys():
        data_to_complete[key].update({'nnanforcol': get_number_nans(data_to_complete[key]['nanforcol'])})
        data_to_complete[key].update({'nnanforrow': get_number_nans(data_to_complete[key]['nanforrow'])})
    return data_to_complete

def get_number_nans(df):
    nan_to_elem = {}
    for value in df.values():
        if nan_to_elem.get(value):
            nan_to_elem[value] += 1
        else:
            nan_to_elem.update({value: 1})
    return nan_to_elem

def save_to_csv(dic):
    df = pd.DataFrame.from_dict(dic)
    #save rows and columns
    df.iloc[:2].to_csv('./csv_per_stats/stats/row_col.csv')
    df.iloc[4].to_csv('./csv_per_stats/stats/nan.csv')
    df.iloc[5].to_csv('./csv_per_stats/stats/nan_col.csv')
    df.iloc[6].to_csv('./csv_per_stats/stats/nan_row.csv')
    df.iloc[7].to_csv('./csv_per_stats/stats/nnan_col.csv')
    df.iloc[8].to_csv('./csv_per_stats/stats/nnan_row.csv')
#TODO: numero di valori diversi per colonna



dataframes = []
directory = './dataset'
for filename in os.listdir(directory):
    f = os.path.join(directory, filename)
    # checking if it is a file
    if os.path.isfile(f):
        df = pd.read_csv(f)
        df.Name =  os.path.splitext(filename)[0]
        dataframes.append(df)

df = get_stats(dataframes)
save_to_csv(df)
pd.DataFrame.from_dict(df).to_csv('./csv_per_stats/stats/all_stats.csv')