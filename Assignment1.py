import pandas as pd
import numpy as np

df = pd.read_json('Artists.json')
df['Decade'] = np.floor(df['BeginDate'] / 10) * 10
df = df.drop(columns=['ConstituentID', 'DisplayName', 'ArtistBio', 'BeginDate', 'EndDate', 'Wiki QID', 'ULAN'])
df = df.loc[~(df['Decade'] == 0.0)]

# Grouped by Nationality and Decade
# df = df.groupby(['Nationality', 'Decade']).size().reset_index(name='Count')

# Grouped by Nationality and Gender
# df = df.groupby(['Nationality', 'Gender']).size().reset_index(name='Count')

# Grouped by Gender and Decade
# df = df.groupby(['Gender', 'Decade']).size().reset_index(name='Count')

# Grouped by all 3
df = df.groupby(['Nationality', 'Gender', 'Decade']).size().reset_index(name='Count')

df = df.loc[(df['Count'] > 20)]
df['Decade'] = df['Decade'].astype(int)
df = df.applymap(str)

