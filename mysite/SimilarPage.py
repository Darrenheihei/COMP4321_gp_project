from collections import Counter
import sqlite3
import RetrievalFunction # wait for implementation of RetrievalFunction

class SimilarPage:
    def __init__(self, original_query):
        self.original_query:str = original_query
        self.prepareSQL()

    def prepareSQL(self) -> None:
        self.con = sqlite3.connect('project.db')
        self.cur = self.con.cursor()

        # create table 'id2keyword' when necessary
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='id2keyword'").fetchone() is None:
            self.cur.execute("CREATE TABLE id2keyword(keywordId, keyword)")

        # create table 'ForwardIndex' when necessary
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='ForwardIndex'").fetchone() is None:
            self.cur.execute("CREATE TABLE ForwardIndex(urlId, value)") # value is in the form of "keywordId1 keywordId2 ..."

    def __getRevisedQuery(self, top5words: list[str]) -> str:
        query_list = self.original_query.split()

        for word in top5words:
            if word not in query_list:
                query_list.append(word)

        return ' '.join(query_list)

    def getTop5FreqWords(self, urlId: str, ) -> list[str]:
        # get the forward index value for the given urlId
        value: str = self.cur.execute("SELECT value FROM ForwardIndex WHERE urlId=?", (urlId,)).fetchone()[0]

        # split the value into individual keywordIds
        keywordIds: list[str] = value.split() # to be changed because of the change in phase 1 by darren 

        # create a counter to count the frequency of each keyword
        counter = Counter(keywordIds)

        # get the top 5 most frequent keywordIds
        top5keywordIds = [keywordId for keywordId, _ in counter.most_common(5)]

        # convert the keywordIds to actual keywords
        top5words: list[str] = []
        for keywordId in top5keywordIds:
            keyword: str = self.cur.execute("SELECT keyword FROM id2keyword WHERE keywordId=?", (keywordId,)).fetchone()[0]
            top5words.append(keyword)
        
        return top5words

    def getSimilarPages(self, urlId: str) -> list[str]:
        top5words: list[str] = self.getTop5FreqWords(urlId)
        revisedQuery: str = self.__getRevisedQuery(top5words)
        
        # perform a new search using the revised query
        retrievalFunction = RetrievalFunction(revisedQuery)
        similarPages: list[resultItem] = retrievalFunction.retrieve() #wait for implemention of RetrievalFunction
        
        return similarPages
