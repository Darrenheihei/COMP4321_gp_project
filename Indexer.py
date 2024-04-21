from collections import Counter
import sqlite3
import uuid

class Indexer:
    def __init__(self):
        self.keyword2id = {}
        self.id2keyword = {}
        self.ForwardIndex = {}
        self.TitleInvertedIndex = {}
        self.BodyInvertedIndex = {}
        self.con = None # for SQLite
        self.cur = None # for SQLite

        self.prepareSQL()

    def prepareSQL(self) -> None:
        self.con = sqlite3.connect('project.db')
        self.cur = self.con.cursor()

        # create table 'keyword2id' if not exist
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='keyword2id'").fetchone() is None:
            self.cur.execute("CREATE TABLE keyword2id(keyword, keywordId)")
        else: # extract previous data if already exist
            result = self.cur.execute(f"SELECT * FROM keyword2id").fetchall()
            self.keyword2id = {i[0]: i[1] for i in result}

        # create table 'id2keyword' if not exist
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='id2keyword'").fetchone() is None:
            self.cur.execute("CREATE TABLE id2keyword(keywordId, keyword)")
        else:  # extract previous data if already exist
            result = self.cur.execute(f"SELECT * FROM id2keyword").fetchall()
            self.id2keyword = {i[0]: i[1] for i in result}


        # create table 'ForwardIndex' if not exist
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='ForwardIndex'").fetchone() is None:
            self.cur.execute("CREATE TABLE ForwardIndex(urlId, value)") # value is in the form of "keywordId1 keywordId2 ..."
        else:  # extract previous data if already exist
            result = self.cur.execute(f"SELECT * FROM ForwardIndex").fetchall()
            self.ForwardIndex = {i[0]: i[1].split(' ') for i in result}

        # create table 'TitleInvertedIndex' if not exist
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='TitleInvertedIndex'").fetchone() is None:
            self.cur.execute("CREATE TABLE TitleInvertedIndex(keywordId, value)") # value is in the form of "urlId1:freq1:pos1,pos2 urlId2:freq2:pos1,pos2 ..."
        else:  # extract previous data if already exist
            result = self.cur.execute(f"SELECT * FROM TitleInvertedIndex").fetchall()
            # print([[(urlId,) for i in value.split(' ') for urlId in i.split(':')] for keywordId, value in result])
            self.TitleInvertedIndex = {keywordId:
                                           {i.split(':')[0]: {'freq': i.split(':')[1], 'pos': map(int, i.split(':')[2].split(','))} for i in value.split(' ')}
                                       for keywordId, value in result}

        # create table 'BodyInvertedIndex' if not exist
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='BodyInvertedIndex'").fetchone() is None:
            self.cur.execute("CREATE TABLE BodyInvertedIndex(keywordId, value)")  # value is in the form of "urlId1:freq1:pos1,pos2 urlId2:freq2,pos1,pos2 ..."
        else:  # extract previous data if already exist
            result = self.cur.execute(f"SELECT * FROM BodyInvertedIndex").fetchall()
            self.BodyInvertedIndex = {keywordId:
                                          {i.split(':')[0]: {'freq': i.split(':')[1], 'pos': map(int, i.split(':')[2].split(','))} for i in value.split(' ')}
                                      for keywordId, value in result}


        self.con.commit()

    def updateDatabase(self):
        # update keyword2id database
        # clear the keyword2id table
        self.cur.execute("DROP TABLE keyword2id")
        self.cur.execute("CREATE TABLE keyword2id(keyword, keywordId)")
        # update the entries inside the table
        self.cur.executemany("INSERT INTO keyword2id VALUES(?, ?)", list(self.keyword2id.items()))
        self.con.commit()

        # update id2keyword database
        # clear the id2keyword table
        self.cur.execute("DROP TABLE id2keyword")
        self.cur.execute("CREATE TABLE id2keyword(keywordId, keyword)")
        # update the entries inside the table
        self.cur.executemany("INSERT INTO id2keyword VALUES(?, ?)", list(self.id2keyword.items()))
        self.con.commit()

        # update ForwardIndex database
        # clear the ForwardIndex table
        self.cur.execute("DROP TABLE ForwardIndex")
        self.cur.execute("CREATE TABLE ForwardIndex(urlId, value)")
        # construct the value into the specified format
        key_values = ((key, ' '.join(value)) for key, value in self.ForwardIndex.items())
        # update the entries inside the table
        self.cur.executemany("INSERT INTO ForwardIndex VALUES(?, ?)", key_values)
        self.con.commit()

        # update TitleInvertedIndex database
        # clear the TitleInvertedIndex table
        self.cur.execute("DROP TABLE TitleInvertedIndex")
        self.cur.execute("CREATE TABLE TitleInvertedIndex(keywordId, value)")
        # construct the value into the specified format
        key_values = ((keywordId, ' '.join([f"{urlId}:{inner_value['freq']}:{','.join(map(str, inner_value['pos']))}" for urlId, inner_value in value.items()]))
                      for keywordId, value in self.TitleInvertedIndex.items())
        # update the entries inside the table
        self.cur.executemany("INSERT INTO TitleInvertedIndex VALUES(?, ?)", key_values)
        self.con.commit()

        # update BodyInvertedIndex database
        # clear the BodyInvertedIndex table
        self.cur.execute("DROP TABLE BodyInvertedIndex")
        self.cur.execute("CREATE TABLE BodyInvertedIndex(keywordId, value)")
        # construct the value into the specified format
        key_values = ((keywordId, ' '.join([f"{urlId}:{inner_value['freq']}:{','.join(map(str, inner_value['pos']))}" for urlId, inner_value in value.items()]))
                      for keywordId, value in self.BodyInvertedIndex.items())
        # update the entries inside the table
        self.cur.executemany("INSERT INTO BodyInvertedIndex VALUES(?, ?)", key_values)
        self.con.commit()

    def addNewKeyword(self, keywords: list[str]) -> None:
        for keyword in keywords:
            if not self.keyword2id.get(keyword, None) is not None: # the keyword haven't get an ID
                # create ID for the keyword
                keywordId: str = str(uuid.uuid4().int)
                self.keyword2id[keyword] = keywordId
                self.id2keyword[keywordId] = keyword

    def forwardIndex(self, words: list[str], urlId: str, clearOldContent: bool=False) -> None:
        unique_words: list[str] = list(set(words))
        value: list[str] = []
        if not clearOldContent:
            value: list[str] = self.ForwardIndex.get(urlId, [])

        for word in unique_words:
            keywordId = self.keyword2id[word]
            if keywordId not in value: # prevent double counting a keyword that appears in both title and body text
                value.append(keywordId)

        # save the new value
        self.ForwardIndex[urlId] = value


    def titleInvertedIndex(self, words: list[str], urlId: str) -> None:
        word_pos: dict = {} # keyword: [pos1, pos2, ...]
        # loop through the words list to get the position of all the words
        for n, word in enumerate(words):
            word_pos[word] = word_pos.get(word, []) + [n]

        # store the info for every keyword
        for keyword, positions in word_pos.items():
            keywordId: str = self.keyword2id[keyword]

            # save the value
            if keywordId in self.TitleInvertedIndex:
                self.TitleInvertedIndex[keywordId][urlId] = {'freq': len(positions), 'pos': positions}
            else:
                self.TitleInvertedIndex[keywordId] = {urlId: {'freq': len(positions), 'pos': positions}}


    def bodyInvertedIndex(self, words: list[str], urlId: str) -> None:
        word_pos: dict = {}  # keyword: [pos1, pos2, ...]
        # loop through the words list to get the position of all the words
        for n, word in enumerate(words):
            word_pos[word] = word_pos.get(word, []) + [n]

        for keyword, positions in word_pos.items():
            keywordId: str = self.keyword2id[keyword]

            # save the value
            if keywordId in self.BodyInvertedIndex:
                self.BodyInvertedIndex[keywordId][urlId] = {'freq': len(positions), 'pos': positions}
            else:
                self.BodyInvertedIndex[keywordId] = {urlId: {'freq': len(positions), 'pos': positions}}


if __name__ == '__main__':
    con = sqlite3.connect('project.db')
    cur = con.cursor()
    print(len(cur.execute("SELECT url FROM id2url").fetchall()))

