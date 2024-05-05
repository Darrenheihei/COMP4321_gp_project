import sqlite3
def main():
    con = sqlite3.connect('project.db')
    cur = con.cursor()

    # read URL
    res = cur.execute("SELECT * FROM CrawledPage")
    with open('spider_result.txt', 'w') as f:
        for url, urlId in res.fetchall():
            title: str = cur.execute(f"SELECT title FROM Title WHERE urlId='{urlId}'").fetchone()[0]
            f.write(title)
            f.write('\n')

            f.write(url)
            f.write('\n')

            modDate: str = cur.execute(f"SELECT modDate FROM ModDate WHERE urlId='{urlId}'").fetchone()[0]
            pageSize: str = cur.execute(f"SELECT size FROM PageSize WHERE urlId='{urlId}'").fetchone()[0]
            f.write(f"{modDate}, {pageSize}")
            f.write('\n')

            wordIds: list[str] = cur.execute(f"SELECT value FROM ForwardIndex WHERE urlId='{urlId}'").fetchone()[0].split(' ')
            count = 0
            for keywordId in wordIds:
                titleFreq = []
                bodyFreq = []
                titleValues: list[str] = cur.execute("SELECT value FROM TitleInvertedIndex WHERE keywordId=?", (keywordId,)).fetchone()
                bodyValues: list[str] = cur.execute("SELECT value FROM BodyInvertedIndex WHERE keywordId=?", (keywordId,)).fetchone()
                if titleValues is not None: # the keyword is not one of the keywords in any title
                    titleValues = titleValues[0].split(' ')
                    titleFreq = [i for i in titleValues if i.startswith(urlId)]
                if bodyValues is not None:
                    bodyValues = bodyValues[0].split(' ')
                    bodyFreq = [i for i in bodyValues if i.startswith(urlId)]

                keyword: str = cur.execute("SELECT keyword FROM id2keyword WHERE keywordId=?", (keywordId,)).fetchone()[0]


                if titleFreq != []: # the keyword is really one of the keyword in the title of the current url
                    titleFreq = int(titleFreq[0].split(':')[1])
                else:
                    titleFreq = 0

                if bodyFreq != []: # the keyword is really one of the keyword in the body of the current url
                    bodyFreq = int(bodyFreq[0].split(':')[1])
                else:
                    bodyFreq = 0
                if bodyFreq + titleFreq != 0:# and count < 10:
                    f.write(f"{keyword} {titleFreq + bodyFreq}; ")
                    count += 1
            f.write('\n')

            childUrlIds: list[str] = cur.execute("SELECT value FROM ChildUrl WHERE urlId=?", (urlId,)).fetchone()
            childUrls: list[str] = []
            if childUrlIds is None:
                childUrlIds = []
            else:
                childUrlIds = childUrlIds[0].split(' ')[:10]
                for childUrlId in childUrlIds:
                    childUrls.append(cur.execute(f"SELECT url FROM id2url WHERE urlId=?", (childUrlId,)).fetchone()[0])

            f.write('\n'.join(childUrls))
            f.write('\n')
            f.write('-' * 100)
            f.write('\n')



if __name__ == '__main__':
    main()