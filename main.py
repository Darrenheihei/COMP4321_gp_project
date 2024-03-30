def main():
    pass
    # ssl._create_default_https_context = ssl._create_unverified_context
    #
    # starting_page = "https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm"
    #
    # with req.urlopen(starting_page) as res:
    #     page_data = res.read().decode()
    # soup = BeautifulSoup(page_data, 'html.parser')
    # print(soup.title.string) # get title
    # for link in soup.find_all('a'): # get all child links
    #     print(link.get('href'))
    # print(soup.find('body').get_text()) # get body text
    #
    # con = sqlite3.connect('testing.db')
    # cur = con.cursor()
    # # # create table 'movie' when necessary
    # if cur.execute("SELECT name FROM sqlite_master WHERE name='movie'").fetchone() is None:
    #     print("spider: table not found")
    #     cur.execute("CREATE TABLE movie(title, year, score)")

    # print(cur.execute("SELECT name FROM sqlite_master WHERE name='movie'").fetchone())

    # cur.execute("""
    #     INSERT INTO movie VALUES
    #         ('Monty Python and the Holy Grail', 1975, 8.2),
    #         ('And Now for Something Completely Different', 1971, 7.5)
    # """)
    # cur.execute("DELETE FROM movie WHERE title='Monty Python and the Holy Grail'")
    # con.commit()
    #
    # res = cur.execute("SELECT * FROM movie WHERE title='Monty Python and the Holy Grail'")
    # print(res.fetchall())
    # cur.execute("DELETE FROM movie WHERE title='Monty Python and the Holy Grail'")
    # con.commit()
    # print(uuid.uuid4().int)


    # stemmer = SnowballStemmer("porter", ignore_stopwords=True)
    # plurals = ['caresses', 'flies', 'dies', 'mules', 'denied',
    #            'died', 'agreed', 'owned', 'humbled', 'sized',
    #            'meeting', 'stating', 'siezing', 'itemization',
    #            'sensational', 'traditional', 'reference', 'colonizer',
    #            'plotted', 'a', 'is', '123', 'he\'s']
    # singles = [stemmer.stem(plural) for plural in plurals]
    # print(singles)
    # print(stopwords.words)


processor = StopStem()

# print(processor.process(plurals))

if __name__ == '__main__':
    main()