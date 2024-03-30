from collections import Counter
import sqlite3
import uuid

class Indexer:
    def __init__(self):
        self.con = None # for SQLite
        self.cur = None # for SQLite

        self.prepareSQL()

    def prepareSQL(self) -> None:
        self.con = sqlite3.connect('project.db')
        self.cur = self.con.cursor()

        # create table 'keyword2id' when necessary
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='keyword2id'").fetchone() is None:
            self.cur.execute("CREATE TABLE keyword2id(keyword, keywordId)")

        # create table 'id2keyword' when necessary
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='id2keyword'").fetchone() is None:
            self.cur.execute("CREATE TABLE id2keyword(keywordId, keyword)")

        # create table 'ForwardIndex' when necessary
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='ForwardIndex'").fetchone() is None:
            self.cur.execute("CREATE TABLE ForwardIndex(urlId, value)") # value is in the form of "keywordId1 keywordId2 ..."

        # create table 'TitleInvertedIndex' when necessary
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='TitleInvertedIndex'").fetchone() is None:
            self.cur.execute("CREATE TABLE TitleInvertedIndex(keywordId, value)") # value is in the form of "urlId1:freq1 urlId2:freq2 ..."

        # create table 'BodyInvertedIndex' when necessary
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='BodyInvertedIndex'").fetchone() is None:
            self.cur.execute("CREATE TABLE BodyInvertedIndex(keywordId, value)")  # value is in the form of "urlId1:freq1 urlId2:freq2 ..."

        self.con.commit()

    def addNewKeyword(self, keywords: list[str]) -> None:
        for keyword in keywords:
            if not self.hasId(keyword): # the keyword haven't get an ID
                # create ID for the keyword
                keywordId: str = str(uuid.uuid4().int)
                self.cur.execute(f"INSERT INTO keyword2id VALUES(?, ?)", (keyword, keywordId))
                self.cur.execute(f"INSERT INTO id2keyword VALUES(?, ?)", (keywordId, keyword))
                self.con.commit()

    def hasId(self, keyword: str) -> bool:
        return self.cur.execute("SELECT keywordId FROM keyword2id WHERE keyword=?", (keyword,)).fetchone() is not None

    def forwardIndex(self, words: list[str], urlId: str) -> None:
        unique_words: list[str] = list(set(words))
        wordIds: list[str] = []
        value: str = self.cur.execute(f"SELECT value FROM ForwardIndex WHERE urlId=?", (urlId,)).fetchone()
        if value is None:
            value = ''
        else:
            value = value[0] + ' '

        for word in unique_words:
            keywordId = self.cur.execute("SELECT keywordId FROM keyword2id WHERE keyword=?", (word,)).fetchone()[0]
            if keywordId not in value: # prevent double counting a keyword that appears in both title and body text
                wordIds.append(keywordId)

        value += ' '.join(wordIds)

        # clear the original row in the database if there is
        self.cur.execute("DELETE FROM ForwardIndex WHERE urlId=?", (urlId,))
        self.con.commit()

        # add the new value to the database
        self.cur.execute("INSERT INTO ForwardIndex VALUES(?, ?)", (urlId, value))
        self.con.commit()


    def titleInvertedIndex(self, words: list[str], urlId: str) -> None:
        counter: Counter = Counter(words)
        for keyword, freq in counter.items():
            keywordId: str = self.cur.execute("SELECT keywordId FROM keyword2id WHERE keyword=?", (keyword,)).fetchone()[0]
            value: str = self.cur.execute(f"SELECT value FROM TitleInvertedIndex WHERE keywordId=?", (keywordId,)).fetchone()
            if value is None:
                value = ''
            else:
                value = value[0] + ' '

            value += f"{urlId}:{freq}"

            # clear the original row in the database if there is
            self.cur.execute("DELETE FROM TitleInvertedIndex WHERE keywordId=?", (keywordId,))
            self.con.commit()

            # add the new value to the database
            self.cur.execute("INSERT INTO TitleInvertedIndex VALUES(?, ?)", (keywordId, value))
            self.con.commit()



    def bodyInvertedIndex(self, words: list[str], urlId: str) -> None:
        counter = Counter(words)
        for keyword, freq in counter.items():
            keywordId: str = self.cur.execute("SELECT keywordId FROM keyword2id WHERE keyword=?", (keyword,)).fetchone()[0]
            value: str = self.cur.execute(f"SELECT value FROM BodyInvertedIndex WHERE keywordId=?", (keywordId,)).fetchone()
            if value is None:
                value = ''
            else:
                value = value[0] + ' '

            value += f"{urlId}:{freq}"

            # clear the original row in the database if there is
            self.cur.execute("DELETE FROM BodyInvertedIndex WHERE keywordId=?", (keywordId,))
            self.con.commit()

            # add the new value to the database
            self.cur.execute("INSERT INTO BodyInvertedIndex VALUES(?, ?)", (keywordId, value))
            self.con.commit()


if __name__ == '__main__':
    con = sqlite3.connect('project.db')
    cur = con.cursor()
    print(len(cur.execute("SELECT url FROM id2url").fetchall()))

