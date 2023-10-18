import plotly.graph_objects as go
import plotly.express as px
from dash import Dash, dcc, html, Input, Output
import pandas as pd

# load and label the dataset
sun = pd.read_csv('SN_m_tot_V2.0.csv', delimiter=';')
sun.columns = ['year', 'month', 'date_fraction', 'mm_total', 'mm_std', 'num_obs', 'provisional']

app = Dash(__name__)

# define the layout
app.layout = html.Div([
    html.H4('Monitoring and Analyzing Solar Activity using Dash',  # header
            style={'textAlign': 'center'}),
    html.P('Year Range'),
    dcc.RangeSlider(min=sun['year'].min(),  # year slider
                    max=sun['year'].max(),
                    step=10,
                    id="slider",
                    marks={i: '{}'.format(i) for i in range(sun['year'].min(), sun['year'].max(), 10)},),
    html.P('Observation Period (Months)'),
    dcc.Dropdown(id="dropdown",  # observation period dropdown
                 options=[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 18, 18, 19, 20, 21, 22, 23, 24],
                 value=12,
                 clearable=False),
    dcc.Graph(id="years"),
    html.Div([html.P('Cycle Period (Years)'),
              dcc.Slider(min=0,  # cycle period slider
                         max=20,
                         step=1,
                         value=11,
                         id="slider_2")],
             style={'width': '49%', 'display': 'inline-block'}),
    html.Div([html.P('Real-time Image of the Sun (Imaging Filter)'),
             dcc.Slider(min=0,  # imaging filter slider
                        max=5,
                        step=1,
                        id="slider_3",
                        marks={
                             0: 'EIT 304',
                             1: 'EIT 171',
                             2: 'EIT 195',
                             3: 'EIT 284',
                             4: 'SDO/HMI Cont',
                             5: 'SDO/HMI Mag'},
                        value=4)],
             style={'width': '49%', 'display': 'inline-block'}),
    dcc.Graph(id="cycle",
              figure={'layout': {'height': 600, 'width': 600}},
              style={'float': 'left', 'margin': 'auto'}),
    html.Img(style={'height': '30%', 'width': '30%', 'float': 'right', 'margin': 'auto'},
             id='image')
])

# define how the dashboard reacts to control changes


@app.callback(
     Output("years", "figure"),
     Input("slider", "value"),
     Input("dropdown", "value"),
)
def make_graph(year_range, obs):

    """ Visualize sunspot counts and smoothed average over a
    user selected range of years and observation periods.
    year_range: list of 2 numbers
    obs: number
    output: figure"""

    sun['smooth'] = sun['mm_total'].rolling(obs).mean()
    fig = go.Figure()
    fig.add_trace(go.Scatter(x=sun['date_fraction'],
                             y=sun['mm_total'],
                             name='Monthly'))
    fig.add_trace(go.Scatter(x=sun['date_fraction'],
                             y=sun['smooth'],
                             mode='lines',
                             line_shape='spline',
                             name='Smoothed'))
    fig.update_layout(title='International Sunspot Number',
                      xaxis_title='Time (Years)',
                      yaxis_title='Number of Sunspots',
                      xaxis_range=year_range)

    return fig


@app.callback(
    Output("cycle", "figure"),
    Input("slider_2", "value")
)
def make_cycle_graph(cycle):

    """ Visualize variability of sunspot cycle over a
    user tuned cycle period
    cycle: number
    output: figure"""

    sun['mod_year'] = sun['date_fraction'] % cycle

    fig = px.scatter(sun, x='mod_year', y='mm_total')
    fig.update_layout(title='Sunspot Cycles',
                      xaxis_title='Years',
                      yaxis_title='Number of Sunspots',
                      xaxis_range=[0, cycle])

    return fig


@app.callback(
    Output("image", "src"),
    Input("slider_3", "value")
)
def display_image(mark):

    """ Display real-time image of the sun
    with a user selected imaging filter.
    mark: number in [0, 5]
    output: string"""

    if mark == 0:
        return 'https://soho.nascom.nasa.gov/data/realtime/eit_304/1024/latest.jpg'
    elif mark == 1:
        return 'https://soho.nascom.nasa.gov/data/realtime/eit_171/1024/latest.jpg'
    elif mark == 2:
        return 'https://soho.nascom.nasa.gov/data/realtime/eit_195/1024/latest.jpg'
    elif mark == 3:
        return 'https://soho.nascom.nasa.gov/data/realtime/eit_284/1024/latest.jpg'
    elif mark == 4:
        return 'https://soho.nascom.nasa.gov/data/realtime/hmi_igr/1024/latest.jpg'
    elif mark == 5:
        return 'https://soho.nascom.nasa.gov/data/realtime/hmi_mag/1024/latest.jpg'


def main():
    app.run_server(debug=True)


main()
