import ssl
import sqlite3
import uuid
import queue
import requests

from ContentExtractor import ContentExtractor
from StopStem import StopStem
from Indexer import Indexer

CURSOR_UP = "\033[1A"
CLEAR = "\x1b[2K"
CLEAR_LINE = CURSOR_UP + CLEAR

class Spider:
    def __init__(self, startUrl: str, n: int):
        self.startUrl: str = startUrl
        self.n: int = n
        self.extractor: ContentExtractor = ContentExtractor()
        self.stemmer: StopStem = StopStem()
        self.indexer: Indexer = Indexer()
        self.link_queue: queue.Queue = queue.Queue()
        self.crawled_pages: list[str] = []
        self.id2url: dict = {}
        self.url2id: dict = {}
        self.child_pages: dict = {}
        self.parent_pages: dict = {}
        self.Title: dict = {}
        self.PageSize: dict = {}
        self.ModDate: dict = {}
        self.con = None # for SQLite
        self.cur = None # for SQLite

        ssl._create_default_https_context = ssl._create_unverified_context
        self.prepareSQL()


    def prepareSQL(self) -> None:
        self.con = sqlite3.connect('project.db')
        self.cur = self.con.cursor()

        # create table 'url2id' if not exist
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='url2id'").fetchone() is None:
            self.cur.execute("CREATE TABLE url2id(url, urlId)")
        else: # extract previous data if already exist
            result = self.cur.execute(f"SELECT * FROM url2id").fetchall()
            self.url2id = {i[0]: i[1] for i in result}


        # create table 'id2url' if not exist
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='id2url'").fetchone() is None:
            self.cur.execute("CREATE TABLE id2url(urlId, url)")
        else: # extract previous data if already exist
            result = self.cur.execute(f"SELECT * FROM id2url").fetchall()
            self.id2url = {i[0]: i[1] for i in result}

        # create table 'ModDate' if not exist
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='ModDate'").fetchone() is None:
            self.cur.execute("CREATE TABLE ModDate(urlId, modDate)")
        else: # extract previous data if already exist
            result = self.cur.execute(f"SELECT * FROM ModDate").fetchall()
            self.ModDate = {i[0]: i[1] for i in result}

        # create table 'Title' if not exist
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='Title'").fetchone() is None:
            self.cur.execute("CREATE TABLE Title(urlId, title)")
        else: # extract previous data if already exist
            result = self.cur.execute(f"SELECT * FROM Title").fetchall()
            self.Title = {i[0]: i[1] for i in result}

        # create table 'PageSize' if not exist, extract previous data if already exist
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='PageSize'").fetchone() is None:
            self.cur.execute("CREATE TABLE PageSize(urlId, size)")
        else: # extract previous data if already exist
            result = self.cur.execute(f"SELECT * FROM PageSize").fetchall()
            self.PageSize = {i[0]: i[1] for i in result}

        # create table 'CrawledPage' if not exist
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='CrawledPage'").fetchone() is None:
            self.cur.execute("CREATE TABLE CrawledPage(url, urlId)")
        else: # extract previous data if already exist
            result = self.cur.execute(f"SELECT * FROM CrawledPage").fetchall()
            self.crawled_pages = {i[0]: i[1] for i in result}

        # create table 'ParentUrl' if not exist
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='ParentUrl'").fetchone() is None:
            self.cur.execute("CREATE TABLE ParentUrl(urlId, value)") # value is in the form "parentURLId1 parentURLId2 ..."
        else:  # extract previous data if already exist
            result = self.cur.execute(f"SELECT * FROM ParentUrl").fetchall()
            self.parent_pages = {i[0]: i[1] for i in result}

        # create table 'ChildUrl' if not exist
        if self.cur.execute("SELECT name FROM sqlite_master WHERE name='ChildUrl'").fetchone() is None:
            self.cur.execute("CREATE TABLE ChildUrl(urlId, value)") # value is in the form "childtURLId1 childtURLId2 ..."
        else: # extract previous data if already exist
            result = self.cur.execute(f"SELECT * FROM ChildUrl").fetchall()
            self.child_pages = {i[0]: i[1] for i in result}

        self.con.commit()

    def updateDatabse(self): # store all the data into database
        pass

    def doCrawl(self, url: str, urlId: str) -> bool:
        '''
        it will decide crawling a page or not, as well as saving the last modification date of a
        page if it should be crawled
        '''
        # get actual last modification date of the page
        modDate: str = self.extractor.getLastModDate(url)
        # get the stored last modification date of the page
        storedModDate: str = self.ModDate.get(urlId, None)

        # compare the mod date, return True if modDate is later than storedModDate
        # the idea here is that, a modification on a page cannot be revert (I think, not 100% sure),
        # therefore if the value of modDate != storedModDate, that means the page must have been changed
        return storedModDate is None or modDate != storedModDate


    def crawlPages(self) -> None:
        print("Start Crawling...")

        ### here is to setup the initial queue by deciding whether or not to add the root url into the queue ###
        urlId: str = ""
        # get id of the root url
        if self.startUrl in self.url2id: # already stored the page
            urlId = self.url2id[self.startUrl]
        else: # give id2 in a to root url and store it in url2id and id2url
            urlId = str(uuid.uuid4().int)
            self.url2id[self.startUrl] = urlId
            self.id2url[urlId] = self.startUrl

        # check if need to start crawling or not
        if self.doCrawl(self.startUrl, urlId):
            self.link_queue.put(self.startUrl) # initialize the queue
        else:
            print("Starting page is not modified. End crawling.")
            return # as the root url is not modified, no need to run the spider


        ### start BFS page crawling ###
        while (not self.link_queue.empty()) and len(self.crawled_pages) < self.n:
            url: str = self.link_queue.get()

            if not self.doCrawl(url, self.url2id.get(url)): # the page is a old and unmodified page
                continue # skip the page

            # crawl the page
            # TODO: here can do multiprocesing? After getting all the child links, the remaining tasks are not in a hurry
            childLinks: list[str] = self.crawlSinglePage(url, urlId)

            # add uncrawled child pages to link_queue
            for childUrl in childLinks:
                if childUrl not in self.crawled_pages:
                    self.link_queue.put(childUrl)
                    self.child_pages[urlId] = self.child_pages.get(urlId, []) + [childUrl]

            # add url to crawled_page
            self.crawled_pages.append(url)
            # add the page to the CrawledPage database
            self.cur.execute(f"INSERT INTO CrawledPage VALUES(?, ?)", (url, urlId))
            self.con.commit()

        ### end of BFS ###
        # first provide ID to pages that are inside link_queue but not being crawled
        while not self.link_queue.empty():
            url = self.link_queue.get()

            # create ID for the url
            urlId: str = str(uuid.uuid4().int)
            # add to conversion tables url <=> urlId
            self.cur.execute("INSERT INTO url2id VALUES(?, ?)", (url, urlId))
            self.cur.execute(f"INSERT INTO id2url VALUES(?, ?)", (urlId, url))
            self.con.commit()

        # then update childURL database
        for parentUrlId, childUrls in self.child_pages.items():
            childUrlIds: list[str] = []
            for childUrl in childUrls:
                childUrlIds.append(self.cur.execute("SELECT urlId FROM url2id WHERE url=?", (childUrl,)).fetchone()[0])
            value = ' '.join(childUrlIds)
            self.cur.execute("INSERT INTO ChildUrl VALUES(?, ?)", (parentUrlId, value))
            self.con.commit()


        print("\nDone!")

        # return self.crawled_pages

    def crawlSinglePage(self, url: str, urlId: str) -> list[str]:
        print("\r", end="")
        print(f"Progress: {len(self.crawled_pages) + 1}/{self.n}", end="")
        # read html page
        page_data = requests.post(url).text
        # print(page_data)

        # get title
        title: str = self.extractor.getTitle(page_data)
        processedTitle: list[str] = self.stemmer.process(self.extractor.splitWords(title))
        # save title to database
        self.cur.execute(f"INSERT INTO Title VALUES(?, ?)", (urlId, title))
        self.con.commit()
        # save title to ForwardIndex
        self.indexer.addNewKeyword(processedTitle)
        self.indexer.forwardIndex(processedTitle, urlId)
        # save title to TitleInvertedIndex
        self.indexer.titleInvertedIndex(processedTitle, urlId)

        # get body text
        bodyText: str = self.extractor.getBodyText(page_data)
        processedBodyText: list[str] = self.stemmer.process(self.extractor.splitWords(bodyText))
        # save body text to ForwardIndex
        self.indexer.addNewKeyword(processedBodyText)
        self.indexer.forwardIndex(processedBodyText, urlId)
        # save body text to BodyInvertedIndex
        self.indexer.bodyInvertedIndex(processedBodyText, urlId)

        # get last modification date
        modDate: str = self.extractor.getLastModDate(url)
        # save last modification date to database
        self.cur.execute(f"INSERT INTO ModDate VALUES(?, ?)", (urlId, modDate))
        self.con.commit()

        # get page size
        pageSize: str = str(self.extractor.getPagesize(url, bodyText))
        # save page size to database
        self.cur.execute(f"INSERT INTO PageSize VALUES(?, ?)", (urlId, pageSize))
        self.con.commit()

        # get child links
        childLinks: list[str] = self.extractor.getLinks(url, page_data)
        return childLinks


if __name__ == '__main__':
    spider = Spider("https://www.cse.ust.hk/~kwtleung/COMP4321/testpage.htm", 30)
    pages = spider.crawlPages()
    # print("Num of pages:", len(pages))
    # print(pages)

    # links = queue.Queue()
    # i = 0
    # while (i < 1000):
    #     links.put(i)
    #     i += 1
    #     print(links.qsize())
    #
    # while(not links.empty()):
    #     print(links.get(), links.qsize())
    # links.put('hello')
    # links.put('world')
    # print(links.get())
    # links.put('!')
    # print(links.empty(), links.get())
    # print(links.empty(), links.get())
    # print(links.empty())
    # print(links.get() is None)

#    [test, page, thi, test, page, crawler, befor, get, admis, cse, depart, hkust, you, read, intern, new, book, here,
#     movi, list, new]
