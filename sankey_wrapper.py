
"""
sankey.py: A reusable library for sankey visualizations
"""

import plotly.graph_objects as go
import pandas as pd


def _code_mapping(df, src, targ):
    """ Substitutes Dataframe df's src and targ columns with codes """

    # Get distinct labels
    labels = sorted(list(set(list(df[src]) + list(df[targ]))))

    # Get integer codes
    codes = list(range(len(labels)))

    # Create label to code mapping
    lc_map = dict(zip(labels, codes))

    # Substitute names for codes in dataframe
    df = df.replace({src: lc_map, targ: lc_map})

    return df, labels


def make_sankey(df, cols, vals=None, **kwargs):
    """ Create a sankey diagram linking src values to
         target values with thickness vals.

         Takes in a DataFrame df and a list cols and
         outputs a sankey diagram"""

    if len(cols) >= 2:
        stacked = pd.DataFrame()
        # stacks the columns
        for col in cols:
            if cols.index(col) + 1 == len(cols):  # accounts for last column in list
                new_df = df[[col, cols.pop(0), df.columns[-1]]]
            else:
                new_df = df[[col, cols[cols.index(col) + 1], df.columns[-1]]]
                new_df.columns = ['src', 'targ', 'vals']
                stacked = pd.concat([stacked, new_df], axis=0)

        # assigns values variable
        if vals:
            values = stacked[vals]
        else:
            values = [1] * len(stacked)

        # calls _code_mapping and generates diagram
        stacked, labels = _code_mapping(stacked, 'src', 'targ')
        link = {'source': stacked['src'], 'target': stacked['targ'], 'value': values}
        pad = kwargs.get('pad', 50)

        node = {'label': labels, 'pad': pad}
        sk = go.Sankey(link=link, node=node)
        fig = go.Figure(sk)
        fig.show()

    # accounts for when there are not enough columns to create a valid diagram
    else:
        print("The dataframe needs at least two columns.")
