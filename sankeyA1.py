import pandas as pd
import sankey_wrapper as sk
import Assignment1 as aO


def main():
    """ Calls make_sankey function with proper inputs """

    artists = pd.DataFrame(aO.df)
    # Grouped by Nationality and Decade
    # sk.make_sankey(artists, 'Nationality', 'Decade')

    # Grouped by Nationality and Gender
    # sk.make_sankey(artists, 'Nationality', 'Gender')

    # Grouped by Gender and Decade
    # sk.make_sankey(artists, ['Gender', 'Decade'])

    # Grouped by all 3
    # sk.make_sankey(artists, ['Nationality', 'Gender', 'Decade'])
    sk.make_sankey()


if __name__ == '__main__':
    main()

